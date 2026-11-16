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
from sklearn.compose import ColumnTransformer
from sklearn.dummy import DummyClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, confusion_matrix, f1_score, precision_score, recall_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

from ml.generate_dataset import build_synthetic_dataset
from ml.preprocess import CATEGORICAL_FEATURES, FEATURE_COLUMNS, NUMERIC_FEATURES, validate_dataset

DELAY_CATEGORY_THRESHOLDS = {
    "minor": 10.0,
    "moderate": 25.0,
    "major": 45.0,
}
LABEL_ORDER = ["minor", "moderate", "major", "severe"]


def classify_delay_minutes(minutes: float) -> str:
    if minutes < DELAY_CATEGORY_THRESHOLDS["minor"]:
        return "minor"
    if minutes < DELAY_CATEGORY_THRESHOLDS["moderate"]:
        return "moderate"
    if minutes < DELAY_CATEGORY_THRESHOLDS["major"]:
        return "major"
    return "severe"


def build_classification_dataset(rows: int = 4000, seed: int = 42) -> pd.DataFrame:
    dataset = build_synthetic_dataset(rows=rows, seed=seed)
    dataset = dataset.copy()
    dataset["delay_category"] = dataset["delay_minutes"].map(classify_delay_minutes)
    return validate_dataset(dataset).copy()


def build_pipeline() -> Pipeline:
    preprocessor = ColumnTransformer(
        transformers=[
            ("numeric", StandardScaler(), NUMERIC_FEATURES),
            ("categorical", OneHotEncoder(handle_unknown="ignore", sparse_output=False), CATEGORICAL_FEATURES),
        ],
        remainder="drop",
    )
    model = RandomForestClassifier(
        n_estimators=300,
        random_state=42,
        class_weight="balanced",
        min_samples_leaf=2,
    )
    return Pipeline([("preprocessor", preprocessor), ("model", model)])


def compute_metrics(y_true: pd.Series, y_pred: np.ndarray) -> dict:
    return {
        "accuracy": float(accuracy_score(y_true, y_pred)),
        "precision": float(precision_score(y_true, y_pred, average="weighted", zero_division=0)),
        "recall": float(recall_score(y_true, y_pred, average="weighted", zero_division=0)),
        "f1": float(f1_score(y_true, y_pred, average="weighted", zero_division=0)),
        "confusion_matrix": confusion_matrix(y_true, y_pred, labels=LABEL_ORDER).tolist(),
    }


def fit_baseline_model(X_train, y_train, X_test, y_test) -> dict:
    baseline = DummyClassifier(strategy="most_frequent")
    baseline.fit(X_train, y_train)
    baseline_predictions = baseline.predict(X_test)
    return compute_metrics(y_test, baseline_predictions)


def train_and_save_model(dataset_path: str | Path, output_path: str | Path, metadata_output: str | Path) -> dict:
    dataset = pd.read_csv(dataset_path)
    if "delay_category" not in dataset.columns:
        dataset["delay_category"] = dataset["delay_minutes"].map(classify_delay_minutes)
    dataset = validate_dataset(dataset)

    X = dataset[FEATURE_COLUMNS]
    y = dataset["delay_category"].astype(str).str.lower()
    y = pd.Categorical(y, categories=LABEL_ORDER, ordered=True)
    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    pipeline = build_pipeline()
    pipeline.fit(X_train, y_train)
    predictions = pipeline.predict(X_test)
    metrics = compute_metrics(y_test, predictions)
    baseline_metrics = fit_baseline_model(X_train, y_train, X_test, y_test)

    artifact = {
        "model": pipeline,
        "model_version": "v2.0.0",
        "feature_order": FEATURE_COLUMNS,
        "feature_schema": {
            "categorical_features": CATEGORICAL_FEATURES,
            "numeric_features": NUMERIC_FEATURES,
        },
        "weights": {"minor": 10.0, "moderate": 25.0, "major": 45.0},
        "training_seed": 42,
        "baseline_metrics": baseline_metrics,
        "metrics": metrics,
        "target": "delay_category",
        "thresholds": DELAY_CATEGORY_THRESHOLDS,
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
    parser = argparse.ArgumentParser(description="Train the RAILOPS delay-severity classification model.")
    parser.add_argument("--dataset", type=str, default="data/delay_dataset.csv", help="Dataset path")
    parser.add_argument(
        "--output",
        type=str,
        default="artifacts/delay_classification_model.joblib",
        help="Artifact output path",
    )
    parser.add_argument(
        "--metadata-output",
        type=str,
        default="artifacts/delay_classification_model_metadata.json",
        help="Metadata JSON output path",
    )
    args = parser.parse_args()

    dataset_path = Path(args.dataset)
    if not dataset_path.exists():
        dataset = build_classification_dataset(rows=4000, seed=42)
        dataset_path.parent.mkdir(parents=True, exist_ok=True)
        dataset.to_csv(dataset_path, index=False)

    metrics = train_and_save_model(dataset_path, args.output, args.metadata_output)
    artifact = joblib.load(args.output)
    print(json.dumps({"training_metrics": metrics, "baseline_metrics": artifact.get("baseline_metrics")}, indent=2))


if __name__ == "__main__":
    main()
