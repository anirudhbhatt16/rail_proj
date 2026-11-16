import numpy as np
import pytest
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


class DummyClassifier:
    def predict(self, frame):
        return np.array(["moderate"])

    def predict_proba(self, frame):
        return np.array([[0.2, 0.5, 0.2, 0.1]])


class DummyCongestionModel:
    def predict(self, frame):
        return np.array([2])


class DummyAnomalyModel:
    def predict(self, frame):
        return np.array([0])

    def score_samples(self, frame):
        return np.array([0.87])


@pytest.fixture
def install_extended_models():
    from app.models.model_loader import ModelLoader

    app.state.delay_classifier_loader = ModelLoader("/tmp/delay_classifier.joblib")
    app.state.delay_classifier_loader.model = DummyClassifier()
    app.state.delay_classifier_loader.model_version = "v-classifier-test"

    app.state.congestion_loader = ModelLoader("/tmp/congestion.joblib")
    app.state.congestion_loader.model = DummyCongestionModel()
    app.state.congestion_loader.model_version = "v-congestion-test"

    app.state.anomaly_loader = ModelLoader("/tmp/anomaly.joblib")
    app.state.anomaly_loader.model = DummyAnomalyModel()
    app.state.anomaly_loader.model_version = "v-anomaly-test"


def test_delay_classification_prediction_endpoint(install_extended_models):
    payload = {
        "train_type": "passenger",
        "priority": 3,
        "route_length_km": 180,
        "scheduled_hour": 15,
        "day_of_week": 2,
        "congestion_level": 2,
        "weather_code": 1,
        "track_condition": "fair",
        "previous_delay_minutes": 6.5,
    }

    response = client.post("/predict/delay-classification", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert body["predicted_category"] in {"minor", "moderate", "major", "severe"}
    assert body["model_version"] == "v-classifier-test"


def test_congestion_prediction_endpoint(install_extended_models):
    payload = {
        "track_id": "T-101",
        "train_density": 3.5,
        "track_occupancy": 0.72,
        "pending_requests": 4,
        "available_capacity": 1,
        "scheduled_hour": 17,
        "day_of_week": 2,
    }

    response = client.post("/predict/congestion", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert body["predicted_congestion_level"] in {0, 1, 2, 3, 4}
    assert body["model_version"] == "v-congestion-test"


def test_anomaly_prediction_endpoint(install_extended_models):
    payload = {
        "track_id": "T-101",
        "train_density": 3.5,
        "track_occupancy": 0.9,
        "pending_requests": 8,
        "available_capacity": 0,
        "scheduled_hour": 18,
        "day_of_week": 5,
    }

    response = client.post("/predict/anomaly", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert isinstance(body["is_anomaly"], bool)
    assert isinstance(body["anomaly_score"], float)
    assert body["model_version"] == "v-anomaly-test"
