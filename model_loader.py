import json
from pathlib import Path

import joblib

from app.config import settings


class ModelLoader:
    def __init__(self, model_path: str | None = None):
        self.model_path = Path(model_path or settings.model_path)
        self.model = None
        self.metadata = None
        self.model_version = "unavailable"
        self.feature_order = []

    def load(self):
        if not self.model_path.exists():
            raise FileNotFoundError(f"Model artifact not found at {self.model_path}")

        payload = joblib.load(self.model_path)
        if not isinstance(payload, dict):
            raise ValueError("Model artifact must be a dictionary payload containing a trained model")
        if "model" not in payload:
            raise ValueError("Model artifact missing the trained model object")

        model = payload.get("model")
        if model is None:
            raise ValueError("Model artifact does not contain a valid model object")

        self.model = model
        self.metadata = payload
        self.model_version = str(payload.get("model_version", "unknown"))
        self.feature_order = payload.get("feature_order", [])

        metadata_candidates = [
            self.model_path.with_name(f"{self.model_path.stem}_metadata.json"),
            self.model_path.with_suffix(".json"),
        ]
        for metadata_path in metadata_candidates:
            if metadata_path.exists():
                try:
                    self.metadata = json.loads(metadata_path.read_text())
                except (TypeError, ValueError):
                    self.metadata = payload
                break

        return self.model

    def is_ready(self) -> bool:
        return self.model is not None

    def get_model(self):
        if self.model is None:
            self.load()
        return self.model

    def get_metadata(self):
        if self.metadata is None:
            self.load()
        return self.metadata
