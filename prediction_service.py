import math

import numpy as np

from app.models.model_loader import ModelLoader
from app.services.feature_service import prepare_congestion_dataframe, prepare_dataframe


class PredictionService:
    def __init__(self, model_loader: ModelLoader):
        self.model_loader = model_loader

    def predict(self, features: dict) -> dict:
        model = self.model_loader.get_model()
        if not hasattr(model, "predict"):
            raise ValueError("Model does not support prediction")

        dataframe = prepare_dataframe(features)
        prediction = np.asarray(model.predict(dataframe))

        if prediction.size != 1:
            raise ValueError("Prediction output must contain exactly one value")

        value = float(prediction.reshape(-1)[0])
        if not math.isfinite(value):
            raise ValueError("Prediction output is invalid or non-finite")

        return {
            "predicted_delay_minutes": value,
            "model_version": self.model_loader.model_version,
            "features": dict(features),
        }


class DelayClassificationService:
    def __init__(self, model_loader: ModelLoader):
        self.model_loader = model_loader

    def predict(self, features: dict) -> dict:
        model = self.model_loader.get_model()
        dataframe = prepare_dataframe(features)
        prediction = np.asarray(model.predict(dataframe))
        if prediction.size != 1:
            raise ValueError("Classification output must contain exactly one label")

        label = str(prediction.reshape(-1)[0])
        confidence = 0.5
        if hasattr(model, "predict_proba"):
            probabilities = np.asarray(model.predict_proba(dataframe)).reshape(-1)
            if probabilities.size > 0:
                confidence = float(np.max(probabilities))

        return {
            "predicted_category": label,
            "confidence": float(confidence),
            "model_version": self.model_loader.model_version,
            "features": dict(features),
        }


class CongestionPredictionService:
    def __init__(self, model_loader: ModelLoader):
        self.model_loader = model_loader

    def predict(self, features: dict) -> dict:
        model = self.model_loader.get_model()
        dataframe = prepare_congestion_dataframe(features)
        prediction = np.asarray(model.predict(dataframe))
        if prediction.size != 1:
            raise ValueError("Congestion output must contain exactly one level")

        value = int(prediction.reshape(-1)[0])
        if value < 0 or value > 4:
            raise ValueError("Congestion prediction must be an integer between 0 and 4")

        return {
            "predicted_congestion_level": value,
            "model_version": self.model_loader.model_version,
            "features": dict(features),
        }


class AnomalyDetectionService:
    def __init__(self, model_loader: ModelLoader):
        self.model_loader = model_loader

    def predict(self, features: dict) -> dict:
        model = self.model_loader.get_model()
        dataframe = prepare_congestion_dataframe(features)
        if hasattr(model, "score_samples"):
            scores = np.asarray(model.score_samples(dataframe)).reshape(-1)
            raw_score = float(scores[0])
        else:
            raw_score = 0.0

        prediction = np.asarray(model.predict(dataframe)).reshape(-1)
        if prediction.size != 1:
            raise ValueError("Anomaly prediction output must contain exactly one label")

        is_anomaly = bool(prediction[0] == -1)
        threshold = 0.0
        if hasattr(model, "offset_"):
            threshold = float(getattr(model, "offset_"))
        elif hasattr(model, "decision_threshold"):
            threshold = float(getattr(model, "decision_threshold"))

        return {
            "is_anomaly": is_anomaly,
            "anomaly_score": float(raw_score),
            "threshold": float(threshold),
            "model_version": self.model_loader.model_version,
            "features": dict(features),
        }
