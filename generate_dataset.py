from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

import numpy as np
import pandas as pd

from ml.preprocess import FEATURE_COLUMNS, validate_dataset


def build_synthetic_dataset(rows: int = 4000, seed: int = 42) -> pd.DataFrame:
    rng = np.random.default_rng(seed)

    train_type = rng.choice(["passenger", "freight", "commuter"], size=rows)
    priority = rng.integers(1, 6, size=rows)
    route_length_km = np.clip(rng.normal(180.0, 70.0, size=rows), 20.0, 500.0)
    scheduled_hour = rng.integers(0, 24, size=rows)
    day_of_week = rng.integers(0, 7, size=rows)
    congestion_level = rng.integers(0, 5, size=rows)
    weather_code = rng.integers(0, 5, size=rows)
    track_condition = rng.choice(["good", "fair", "poor"], size=rows)
    previous_delay_minutes = np.clip(rng.normal(5.0, 4.0, size=rows), 0.0, 60.0)

    delay_minutes = (
        6.0
        + 4.2 * (priority - 1)
        + 0.08 * route_length_km
        + 1.5 * (congestion_level)
        + 2.2 * weather_code
        + 3.1 * (track_condition == "poor")
        + 2.6 * (track_condition == "fair")
        + 1.5 * previous_delay_minutes
        + 0.7 * (scheduled_hour > 17)
        + 0.9 * (day_of_week >= 5)
        + rng.normal(0.0, 3.0, size=rows)
    )
    delay_minutes = np.clip(delay_minutes, 0.0, None)

    dataset = pd.DataFrame(
        {
            "train_type": train_type,
            "priority": priority,
            "route_length_km": route_length_km,
            "scheduled_hour": scheduled_hour,
            "day_of_week": day_of_week,
            "congestion_level": congestion_level,
            "weather_code": weather_code,
            "track_condition": track_condition,
            "previous_delay_minutes": previous_delay_minutes,
            "delay_minutes": delay_minutes,
        }
    )

    dataset = dataset[FEATURE_COLUMNS + ["delay_minutes"]]
    return validate_dataset(dataset)


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate synthetic delay-prediction data.")
    parser.add_argument("--rows", type=int, default=4000, help="Synthetic row count")
    parser.add_argument("--seed", type=int, default=42, help="Random seed")
    parser.add_argument("--output", type=str, default="data/delay_dataset.csv", help="Output CSV location")
    args = parser.parse_args()

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    dataset = build_synthetic_dataset(rows=args.rows, seed=args.seed)
    dataset.to_csv(output_path, index=False)
    print(f"Wrote {len(dataset)} rows to {output_path}")


if __name__ == "__main__":
    main()
