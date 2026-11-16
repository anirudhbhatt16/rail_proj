from __future__ import annotations

import math
from typing import Sequence

import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestRegressor
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

FEATURE_COLUMNS = [
    "train_type",
    "priority",
    "route_length_km",
    "scheduled_hour",
    "day_of_week",
    "congestion_level",
    "weather_code",
    "track_condition",
    "previous_delay_minutes",
]

NUMERIC_FEATURES = [
    "priority",
    "route_length_km",
    "scheduled_hour",
    "day_of_week",
    "congestion_level",
    "weather_code",
    "previous_delay_minutes",
]

CATEGORICAL_FEATURES = ["train_type", "track_condition"]
VALID_TRAIN_TYPES = {"passenger", "freight", "commuter"}
VALID_TRACK_CONDITIONS = {"good", "fair", "poor"}


def _ensure_numeric(frame: pd.DataFrame, column: str) -> None:
    if column not in frame.columns:
        raise ValueError(f"Missing required feature: {column}")
    numeric = pd.to_numeric(frame[column], errors="coerce")
    if numeric.isna().any():
        raise ValueError(f"Feature '{column}' contains non-numeric values")
    if not np.isfinite(numeric.to_numpy()).all():
        raise ValueError(f"Feature '{column}' contains non-finite values")
    frame[column] = numeric.astype(float)


def validate_dataset(frame: pd.DataFrame) -> pd.DataFrame:
    if frame is None or frame.empty:
        raise ValueError("Dataset is empty")

    required = FEATURE_COLUMNS + ["delay_minutes"]
    missing = [column for column in required if column not in frame.columns]
    if missing:
        raise ValueError(f"Dataset missing required columns: {missing}")

    for column in NUMERIC_FEATURES:
        _ensure_numeric(frame, column)

    if frame["route_length_km"].le(0).any():
        raise ValueError("route_length_km must be positive")
    if frame["previous_delay_minutes"].lt(0).any():
        raise ValueError("previous_delay_minutes must be non-negative")
    if (frame["scheduled_hour"].lt(0) | frame["scheduled_hour"].gt(23)).any():
        raise ValueError("scheduled_hour must be between 0 and 23")
    if (frame["day_of_week"].lt(0) | frame["day_of_week"].gt(6)).any():
        raise ValueError("day_of_week must be between 0 and 6")
    if (frame["priority"].lt(1) | frame["priority"].gt(5)).any():
        raise ValueError("priority must be between 1 and 5")
    if (frame["congestion_level"].lt(0) | frame["congestion_level"].gt(4)).any():
        raise ValueError("congestion_level must be between 0 and 4")
    if (frame["weather_code"].lt(0) | frame["weather_code"].gt(4)).any():
        raise ValueError("weather_code must be between 0 and 4")

    train_types = frame["train_type"].astype(str).str.strip().str.lower()
    if not train_types.isin(VALID_TRAIN_TYPES).all():
        raise ValueError("train_type values must be passenger, freight, or commuter")
    track_conditions = frame["track_condition"].astype(str).str.strip().str.lower()
    if not track_conditions.isin(VALID_TRACK_CONDITIONS).all():
        raise ValueError("track_condition values must be good, fair, or poor")

    delay_values = pd.to_numeric(frame["delay_minutes"], errors="coerce")
    if delay_values.isna().any() or not np.isfinite(delay_values.to_numpy()).all():
        raise ValueError("delay_minutes contains invalid values")
    if delay_values.lt(0).any():
        raise ValueError("delay_minutes must be non-negative")

    frame["train_type"] = train_types
    frame["track_condition"] = track_conditions
    frame["delay_minutes"] = delay_values.astype(float)
    return frame


def build_pipeline() -> Pipeline:
    preprocessor = ColumnTransformer(
        transformers=[
            ("numeric", StandardScaler(), NUMERIC_FEATURES),
            ("categorical", OneHotEncoder(handle_unknown="ignore", sparse_output=False), CATEGORICAL_FEATURES),
        ],
        remainder="drop",
    )
    estimator = RandomForestRegressor(
        n_estimators=300,
        random_state=42,
        max_depth=12,
        min_samples_leaf=2,
    )
    return Pipeline([("preprocessor", preprocessor), ("model", estimator)])
