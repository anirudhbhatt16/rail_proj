import math
from typing import Any, Mapping

import pandas as pd

from ml.preprocess import FEATURE_COLUMNS


def normalize_record(raw: Mapping[str, Any]) -> dict[str, Any]:
    record = dict(raw)
    if "train_type" in record:
        record["train_type"] = str(record["train_type"]).strip().lower()

    if "track_condition" in record:
        record["track_condition"] = str(record["track_condition"]).strip().lower()

    numeric_fields = [
        "priority",
        "route_length_km",
        "scheduled_hour",
        "day_of_week",
        "congestion_level",
        "weather_code",
        "previous_delay_minutes",
    ]
    for field in numeric_fields:
        if field not in record:
            raise ValueError(f"Missing required feature: {field}")
        value = record[field]
        if isinstance(value, str):
            value = float(value)
        if not isinstance(value, (int, float)) or not math.isfinite(float(value)):
            raise ValueError(f"Feature {field} must be a finite numeric value")
        record[field] = float(value)

    if record["priority"] < 1 or record["priority"] > 5:
        raise ValueError("priority must be between 1 and 5")
    if record["route_length_km"] <= 0:
        raise ValueError("route_length_km must be greater than zero")
    if record["scheduled_hour"] < 0 or record["scheduled_hour"] > 23:
        raise ValueError("scheduled_hour must be between 0 and 23")
    if record["day_of_week"] < 0 or record["day_of_week"] > 6:
        raise ValueError("day_of_week must be between 0 and 6")
    if record["congestion_level"] < 0 or record["congestion_level"] > 4:
        raise ValueError("congestion_level must be between 0 and 4")
    if record["weather_code"] < 0 or record["weather_code"] > 4:
        raise ValueError("weather_code must be between 0 and 4")
    if record["previous_delay_minutes"] < 0:
        raise ValueError("previous_delay_minutes must be non-negative")

    if record["train_type"] not in {"passenger", "freight", "commuter"}:
        raise ValueError("train_type must be passenger, freight, or commuter")
    if record["track_condition"] not in {"good", "fair", "poor"}:
        raise ValueError("track_condition must be good, fair, or poor")

    return record


def prepare_dataframe(raw: Mapping[str, Any]) -> pd.DataFrame:
    prepared = normalize_record(raw)
    frame = pd.DataFrame([prepared], columns=FEATURE_COLUMNS)

    missing = [column for column in FEATURE_COLUMNS if column not in frame.columns]
    if missing:
        raise ValueError(f"Missing required feature columns: {missing}")

    for column in frame.columns:
        if frame[column].isnull().any():
            raise ValueError(f"Feature '{column}' contains null values")

    return frame


def prepare_congestion_dataframe(raw: Mapping[str, Any]) -> pd.DataFrame:
    record = dict(raw)
    required = [
        "train_density",
        "track_occupancy",
        "pending_requests",
        "available_capacity",
        "scheduled_hour",
        "day_of_week",
    ]
    for field in required:
        if field not in record:
            raise ValueError(f"Missing required feature: {field}")
        value = float(record[field]) if field in {"train_density", "track_occupancy"} else record[field]
        if not math.isfinite(float(value)):
            raise ValueError(f"Feature {field} must be a finite numeric value")
        record[field] = float(value) if field in {"train_density", "track_occupancy"} else int(value)

    if record["train_density"] < 0:
        raise ValueError("train_density must be non-negative")
    if record["track_occupancy"] < 0.0 or record["track_occupancy"] > 1.0:
        raise ValueError("track_occupancy must be between 0 and 1")
    if record["pending_requests"] < 0:
        raise ValueError("pending_requests must be non-negative")
    if record["available_capacity"] < 0:
        raise ValueError("available_capacity must be non-negative")
    if record["scheduled_hour"] < 0 or record["scheduled_hour"] > 23:
        raise ValueError("scheduled_hour must be between 0 and 23")
    if record["day_of_week"] < 0 or record["day_of_week"] > 6:
        raise ValueError("day_of_week must be between 0 and 6")

    columns = [
        "train_density",
        "track_occupancy",
        "pending_requests",
        "available_capacity",
        "scheduled_hour",
        "day_of_week",
    ]
    return pd.DataFrame([record], columns=columns)
