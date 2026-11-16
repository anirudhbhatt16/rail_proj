from fastapi import APIRouter, HTTPException, Request

from app.schemas import (
    AnomalyPredictionRequest,
    AnomalyPredictionResponse,
    CongestionPredictionRequest,
    CongestionPredictionResponse,
    DelayClassificationRequest,
    DelayClassificationResponse,
    DelayPredictionRequest,
    DelayPredictionResponse,
)
from app.services.prediction_service import (
    AnomalyDetectionService,
    CongestionPredictionService,
    DelayClassificationService,
    PredictionService,
)

router = APIRouter()


def _require_loader(request: Request, name: str):
    if not hasattr(request.app.state, name):
        raise HTTPException(status_code=503, detail=f"{name} model is not configured")
    loader = getattr(request.app.state, name)
    try:
        loader.get_model()
    except Exception as exc:
        raise HTTPException(status_code=503, detail=f"Prediction unavailable: {exc}") from exc
    return loader


@router.post("/predict/delay", response_model=DelayPredictionResponse)
def predict_delay(request: Request, payload: DelayPredictionRequest):
    loader = _require_loader(request, "model_loader")

    try:
        result = PredictionService(loader).predict(payload.model_dump())
    except Exception as exc:
        raise HTTPException(status_code=503, detail=f"Prediction unavailable: {exc}") from exc

    return DelayPredictionResponse(**result)


@router.post("/predict/delay-classification", response_model=DelayClassificationResponse)
def predict_delay_classification(request: Request, payload: DelayClassificationRequest):
    loader = _require_loader(request, "delay_classifier_loader")

    try:
        result = DelayClassificationService(loader).predict(payload.model_dump())
    except Exception as exc:
        raise HTTPException(status_code=503, detail=f"Prediction unavailable: {exc}") from exc

    return DelayClassificationResponse(**result)


@router.post("/predict/congestion", response_model=CongestionPredictionResponse)
def predict_congestion(request: Request, payload: CongestionPredictionRequest):
    loader = _require_loader(request, "congestion_loader")

    try:
        result = CongestionPredictionService(loader).predict(payload.model_dump())
    except Exception as exc:
        raise HTTPException(status_code=503, detail=f"Prediction unavailable: {exc}") from exc

    return CongestionPredictionResponse(**result)


@router.post("/predict/anomaly", response_model=AnomalyPredictionResponse)
def predict_anomaly(request: Request, payload: AnomalyPredictionRequest):
    loader = _require_loader(request, "anomaly_loader")

    try:
        result = AnomalyDetectionService(loader).predict(payload.model_dump())
    except Exception as exc:
        raise HTTPException(status_code=503, detail=f"Prediction unavailable: {exc}") from exc

    return AnomalyPredictionResponse(**result)
