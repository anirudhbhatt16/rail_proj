# API Documentation

The Python AI service exposes the original regression endpoint and the additional operational models below.

## Health and readiness

- GET `/health` — returns service liveness and whether the primary delay model is loaded.
- GET `/ready` — returns 200 when the required model is ready; 503 when an artifact is invalid, missing, or incompatible.

## Original delay model

- Method: POST
- Path: `/predict/delay`
- Request: `train_type`, `priority`, `route_length_km`, `scheduled_hour`, `day_of_week`, `congestion_level`, `weather_code`, `track_condition`, `previous_delay_minutes`
- Response: `predicted_delay_minutes`, `model_version`, `features`
- Contract compatibility: preserved for the Java backend integration.

## Delay severity classification

- Method: POST
- Path: `/predict/delay-classification`
- Request: same fields as the original delay request.
- Response: `predicted_category`, `confidence`, `model_version`, `features`
- Thresholds: `minor < 10`, `moderate < 25`, `major < 45`, otherwise `severe`

## Congestion prediction

- Method: POST
- Path: `/predict/congestion`
- Request:
  - `track_id`
  - `train_density`
  - `track_occupancy`
  - `pending_requests`
  - `available_capacity`
  - `scheduled_hour`
  - `day_of_week`
- Response: `predicted_congestion_level`, `model_version`, `features`

## Anomaly detection

- Method: POST
- Path: `/predict/anomaly`
- Request: same as the congestion request.
- Response: `is_anomaly`, `anomaly_score`, `threshold`, `model_version`, `features`
- Interpretation: anomaly score indicates deviation from normal operating behavior; it does not explain the cause of an incident.

## Validation rules

- Invalid numeric ranges return HTTP 422 via FastAPI validation.
- Missing model artifacts return HTTP 503.
- The service never loads arbitrary user-supplied serialized model files; only the project-managed artifacts are permitted.
