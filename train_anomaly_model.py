from __future__ import annotations

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
from sklearn.metrics import accuracy_score, confusion_matrix, f1_score, precision_score, recall_score
from sklearn.model_selection import train_test_split

ANOMALY_FEATURE_COLUMNS = [
    "train_density",
    "track_occupancy",
    "pending_requests",
    "available_capacity",
    "scheduled_hour",
    "day_of_week",
]


def build_anomaly_dataset(rows: int = 4000, seed: int = 42) -> pd.DataFrame:
    rng = np.random.default_rng(seed)

    normal_count = int(rows * 0.9)
    anomaly_count = rows - normal_count

    normal_track_occupancy = np.clip(rng.normal(0.35, 0.12, size=normal_count), 0.0, 0.9)
    anomaly_track_occupancy = np.clip(rng.normal(0.9, 0.08, size=anomaly_count), 0.7, 1.0)
    normal_train_density = rng.normal(2.2, 1.0, size=normal_count)
    anomaly_train_density = rng.normal(7.0, 1.8, size=anomaly_count)
    normal_pending = rng.integers(0, 4, size=normal_count)
    anomaly_pending = rng.integers(4, 15, size=anomaly_count)
    normal_capacity = rng.integers(1, 4, size=normal_count)
    anomaly_capacity = rng.integers(0, 2, size=anomaly_count)
    scheduled_hour = np.concatenate([rng.integers(0, 24, size=normal_count), rng.integers(12, 24, size=anomaly_count)])
    day_of_week = np.concatenate([rng.integers(0, 7, size=normal_count), rng.integers(0, 7, size=anomaly_count)])

    track_occupancy = np.concatenate([normal_track_occupancy, anomaly_track_occupancy])
    train_density = np.concatenate([normal_train_density, anomaly_train_density])
    pending_requests = np.concatenate([normal_pending, anomaly_pending])
    available_capacity = np.concatenate([normal_capacity, anomaly_capacity])

    label = np.concatenate([
        np.zeros(normal_count, dtype=int),
        np.ones(anomaly_count, dtype=int),
    ])

    dataset = pd.DataFrame(
        {
            "train_density": train_density,
            "track_occupancy": track_occupancy,
            "pending_requests": pending_requests,
            "available_capacity": available_capacity,
            "scheduled_hour": scheduled_hour,
            "day_of_week": day_of_week,
            "is_anomaly": label,
        }
    )
    return dataset


def compute_metrics(y_true: pd.Series, y_pred: np.ndarray) -> dict:
    return {
        "accuracy": float(accuracy_score(y_true, y_pred)),
        "precision": float(precision_score(y_true, y_pred, average="binary", zero_division=0)),
        "recall": float(recall_score(y_true, y_pred, average="binary", zero_division=0)),
        "f1": float(f1_score(y_true, y_pred, average="binary", zero_division=0)),
        "confusion_matrix": confusion_matrix(y_true, y_pred, labels=[0, 1]).tolist(),
    }


def fit_baseline_model(frame: pd.DataFrame, y_true: pd.Series) -> dict:
    rule = (
        (frame["track_occupancy"] > 0.8)
        & (frame["pending_requests"] >= 6)
    ) | (frame["available_capacity"] == 0)
    baseline_predictions = rule.astype(int).to_numpy()
    return compute_metrics(y_true, baseline_predictions)


def train_and_save_model(dataset_path: str | Path, output_path: str | Path, metadata_output: str | Path) -> dict:
    dataset = pd.read_csv(dataset_path)
    X = dataset[ANOMALY_FEATURE_COLUMNS]
    y = dataset["is_anomaly"].astype(int)

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    estimator = IsolationForest(
        n_estimators=300,
        contamination=0.1,
        random_state=42,
    )
    estimator.fit(X_train)

    model_predictions = (estimator.predict(X_test) == -1).astype(int)
    metrics = compute_metrics(y_test, model_predictions)
    baseline_metrics = fit_baseline_model(X_test, y_test)

    artifact = {
        "model": estimator,
        "model_version": "v2.0.0",
        "feature_order": ANOMALY_FEATURE_COLUMNS,
        "feature_schema": {
            "numeric_features": ANOMALY_FEATURE_COLUMNS,
        },
        "target": "is_anomaly",
        "training_seed": 42,
        "baseline_metrics": baseline_metrics,
        "metrics": metrics,
        "dataset_provenance": "synthetic operating-data generation with deliberately abnormal occupancy and queue patterns",
        "trained_at": datetime.now(timezone.utc).isoformat(),
    }

    output_path = Path(output_path)
    metadata_output = Path(metadata_output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    metadata_output.parent.mkdir(parents=True, exist_ok=True)

    joblib.dump(artifact, output_path)
    metadata_output.write_text(json.dumps(artifact, default=str, indent=2))
    return metrics


def main() -> None:
    parser = argparse.ArgumentParser(description="Train the RAILOPS anomaly-detection model.")
    parser.add_argument("--dataset", type=str, default="data/anomaly_dataset.csv", help="Dataset path")
    parser.add_argument("--output", type=str, default="artifacts/anomaly_model.joblib", help="Artifact output path")
    parser.add_argument(
        "--metadata-output",
        type=str,
        default="artifacts/anomaly_model_metadata.json",
        help="Metadata JSON output path",
    )
    args = parser.parse_args()

    dataset_path = Path(args.dataset)
    if not dataset_path.exists():
        dataset = build_anomaly_dataset(rows=4000, seed=42)
        dataset_path.parent.mkdir(parents=True, exist_ok=True)
        dataset.to_csv(dataset_path, index=False)

    metrics = train_and_save_model(dataset_path, args.output, args.metadata_output)
    artifact = joblib.load(args.output)
    print(json.dumps({"training_metrics": metrics, "baseline_metrics": artifact.get("baseline_metrics")}, indent=2))


if __name__ == "__main__":
    main()
