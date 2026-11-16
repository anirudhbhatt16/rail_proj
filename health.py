from fastapi import APIRouter, HTTPException, Request

router = APIRouter()


@router.get("/health")
def health(request: Request):
    loader = request.app.state.model_loader
    ready = loader.is_ready()
    return {
        "service": "railops-ai-service",
        "status": "UP",
        "ready": ready,
        "model_version": loader.model_version if ready else None,
    }


@router.get("/ready")
def ready(request: Request):
    loader = request.app.state.model_loader
    if not loader.is_ready():
        raise HTTPException(status_code=503, detail="Model not ready")

    return {
        "service": "railops-ai-service",
        "status": "READY",
        "model_version": loader.model_version,
    }
