from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

import joblib
import pandas as pd
from sklearn.dummy import DummyRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split

from ml.preprocess import FEATURE_COLUMNS, validate_dataset


def evaluate_model(dataset_path: str | Path, model_path: str | Path) -> dict:
    dataset = validate_dataset(pd.read_csv(dataset_path))
    X = dataset[FEATURE_COLUMNS]
    y = dataset["delay_minutes"]
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    artifact = joblib.load(model_path)
    if not isinstance(artifact, dict) or "model" not in artifact:
        raise ValueError("Model artifact is invalid or incomplete")

    predictions = artifact["model"].predict(X_test)
    metrics = {
        "mae": float(mean_absolute_error(y_test, predictions)),
        "rmse": float((mean_squared_error(y_test, predictions)) ** 0.5),
        "r2": float(r2_score(y_test, predictions)),
    }

    baseline = DummyRegressor(strategy="median")
    baseline.fit(X_train, y_train)
    baseline_predictions = baseline.predict(X_test)
    baseline_metrics = {
        "mae": float(mean_absolute_error(y_test, baseline_predictions)),
        "rmse": float((mean_squared_error(y_test, baseline_predictions)) ** 0.5),
        "r2": float(r2_score(y_test, baseline_predictions)),
    }

    return {"model": metrics, "baseline": baseline_metrics}


def main() -> None:
    parser = argparse.ArgumentParser(description="Evaluate the RAILOPS delay-prediction model.")
    parser.add_argument("--dataset", type=str, default="data/delay_dataset.csv", help="Dataset location")
    parser.add_argument("--model", type=str, default="artifacts/delay_model.joblib", help="Artifact location")
    args = parser.parse_args()

    metrics = evaluate_model(args.dataset, args.model)
    print(json.dumps({"evaluation_metrics": metrics}, indent=2))


if __name__ == "__main__":
    main()
