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
import pandas as pd
from sklearn.dummy import DummyRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split

from ml.preprocess import (
    CATEGORICAL_FEATURES,
    FEATURE_COLUMNS,
    NUMERIC_FEATURES,
    build_pipeline,
    validate_dataset,
)


def compute_metrics(actual, predicted):
    return {
        "mae": float(mean_absolute_error(actual, predicted)),
        "rmse": float((mean_squared_error(actual, predicted)) ** 0.5),
        "r2": float(r2_score(actual, predicted)),
    }


def fit_baseline_model(X_train, y_train, X_test, y_test):
    baseline = DummyRegressor(strategy="median")
    baseline.fit(X_train, y_train)
    baseline_predictions = baseline.predict(X_test)
    return compute_metrics(y_test, baseline_predictions)


def train_and_save_model(
    dataset_path: str | Path,
    output_path: str | Path,
    metadata_output: str | Path,
) -> dict:
    dataset = pd.read_csv(dataset_path)
    dataset = validate_dataset(dataset)

    X = dataset[FEATURE_COLUMNS]
    y = dataset["delay_minutes"]
    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
    )

    pipeline = build_pipeline()
    pipeline.fit(X_train, y_train)
    predictions = pipeline.predict(X_test)
    metrics = compute_metrics(y_test, predictions)
    baseline_metrics = fit_baseline_model(X_train, y_train, X_test, y_test)

    model_version = "v1.0.0"
    artifact = {
        "model": pipeline,
        "model_version": model_version,
        "feature_order": FEATURE_COLUMNS,
        "feature_schema": {
            "categorical_features": CATEGORICAL_FEATURES,
            "numeric_features": NUMERIC_FEATURES,
        },
        "training_seed": 42,
        "baseline_metrics": baseline_metrics,
        "metrics": metrics,
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
    parser = argparse.ArgumentParser(description="Train the RAILOPS delay-prediction model.")
    parser.add_argument("--dataset", type=str, default="data/delay_dataset.csv", help="Synthetic dataset path")
    parser.add_argument("--output", type=str, default="artifacts/delay_model.joblib", help="Artifact output path")
    parser.add_argument(
        "--metadata-output",
        type=str,
        default="artifacts/delay_model_metadata.json",
        help="Metadata JSON output path",
    )
    args = parser.parse_args()

    metrics = train_and_save_model(args.dataset, args.output, args.metadata_output)
    artifact = joblib.load(args.output)
    print(
        json.dumps(
            {
                "training_metrics": metrics,
                "baseline_metrics": artifact.get("baseline_metrics"),
            },
            indent=2,
        )
    )


if __name__ == "__main__":
    main()
