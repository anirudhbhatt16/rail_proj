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
from sklearn.dummy import DummyClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, confusion_matrix, f1_score, precision_score, recall_score
from sklearn.model_selection import train_test_split

CONGESTION_FEATURE_COLUMNS = [
    "train_density",
    "track_occupancy",
    "pending_requests",
    "available_capacity",
    "scheduled_hour",
    "day_of_week",
]


def build_congestion_dataset(rows: int = 4000, seed: int = 42) -> pd.DataFrame:
    rng = np.random.default_rng(seed)

    train_density = rng.uniform(0.0, 9.0, size=rows)
    track_occupancy = np.clip(rng.beta(2.5, 2.2, size=rows), 0.0, 1.0)
    pending_requests = rng.integers(0, 12, size=rows)
    available_capacity = rng.integers(0, 4, size=rows)
    scheduled_hour = rng.integers(0, 24, size=rows)
    day_of_week = rng.integers(0, 7, size=rows)

    risk_score = (
        0.45 * track_occupancy
        + 0.25 * (train_density / 9.0)
        + 0.20 * (pending_requests / 12.0)
        + 0.10 * (1.0 - (available_capacity / 3.0))
        + 0.05 * (scheduled_hour >= 16).astype(float)
        + 0.05 * (day_of_week >= 5).astype(float)
    )
    congestion_level = np.digitize(risk_score, bins=[0.2, 0.4, 0.6, 0.8], right=False)
    contamination = np.where(risk_score > 0.8, 1, 0)

    dataset = pd.DataFrame(
        {
            "train_density": train_density,
            "track_occupancy": track_occupancy,
            "pending_requests": pending_requests,
            "available_capacity": available_capacity,
            "scheduled_hour": scheduled_hour,
            "day_of_week": day_of_week,
            "congestion_level": congestion_level.astype(int),
            "synthetic_anomaly_flag": contamination.astype(int),
        }
    )
    dataset["congestion_level"] = dataset["congestion_level"].clip(0, 4)
    return dataset


def compute_metrics(y_true: pd.Series, y_pred: np.ndarray) -> dict:
    return {
        "accuracy": float(accuracy_score(y_true, y_pred)),
        "precision": float(precision_score(y_true, y_pred, average="weighted", zero_division=0)),
        "recall": float(recall_score(y_true, y_pred, average="weighted", zero_division=0)),
        "f1": float(f1_score(y_true, y_pred, average="weighted", zero_division=0)),
        "confusion_matrix": confusion_matrix(y_true, y_pred, labels=[0, 1, 2, 3, 4]).tolist(),
    }


def fit_baseline_model(X_train, y_train, X_test, y_test) -> dict:
    baseline = DummyClassifier(strategy="most_frequent")
    baseline.fit(X_train, y_train)
    return compute_metrics(y_test, baseline.predict(X_test))


def train_and_save_model(dataset_path: str | Path, output_path: str | Path, metadata_output: str | Path) -> dict:
    dataset = pd.read_csv(dataset_path)
    X = dataset[CONGESTION_FEATURE_COLUMNS]
    y = dataset["congestion_level"].astype(int)

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    model = RandomForestClassifier(
        n_estimators=300,
        random_state=42,
        class_weight="balanced",
        min_samples_leaf=2,
    )
    model.fit(X_train, y_train)
    predictions = model.predict(X_test)
    metrics = compute_metrics(y_test, predictions)
    baseline_metrics = fit_baseline_model(X_train, y_train, X_test, y_test)

    artifact = {
        "model": model,
        "model_version": "v2.0.0",
        "feature_order": CONGESTION_FEATURE_COLUMNS,
        "feature_schema": {
            "numeric_features": CONGESTION_FEATURE_COLUMNS,
        },
        "target": "congestion_level",
        "training_seed": 42,
        "baseline_metrics": baseline_metrics,
        "metrics": metrics,
        "dataset_provenance": "synthetic simulation-sized queue data generated from internally consistent congestion logic",
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
    parser = argparse.ArgumentParser(description="Train the RAILOPS congestion-prediction model.")
    parser.add_argument("--dataset", type=str, default="data/congestion_dataset.csv", help="Dataset path")
    parser.add_argument("--output", type=str, default="artifacts/congestion_model.joblib", help="Artifact output path")
    parser.add_argument(
        "--metadata-output",
        type=str,
        default="artifacts/congestion_model_metadata.json",
        help="Metadata JSON output path",
    )
    args = parser.parse_args()

    dataset_path = Path(args.dataset)
    if not dataset_path.exists():
        dataset = build_congestion_dataset(rows=4000, seed=42)
        dataset_path.parent.mkdir(parents=True, exist_ok=True)
        dataset.to_csv(dataset_path, index=False)

    metrics = train_and_save_model(dataset_path, args.output, args.metadata_output)
    artifact = joblib.load(args.output)
    print(json.dumps({"training_metrics": metrics, "baseline_metrics": artifact.get("baseline_metrics")}, indent=2))


if __name__ == "__main__":
    main()
