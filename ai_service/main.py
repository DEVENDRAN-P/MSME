# main.py
# FastAPI Entrypoint for the MSME Financial Health AI Engine

from fastapi import FastAPI, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import sys

app = FastAPI(
    title="MSME Financial Intelligence AI API",
    description="Python FastAPI engine for computing ML health score, explainable SHAP, scenario simulating, and forecasting.",
    version="1.0.0"
)

# CORS setup
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:8080"], # React and Spring Boot services
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from app.services.features import calculate_features
from app.services.scoring import calculate_credit_score
from app.services.forecasting import forecast_cash_flows

class HealthStatus(BaseModel):
    status: str
    python_version: str
    service: str

@app.get("/api/ai/health", response_model=HealthStatus, status_code=status.HTTP_200_OK)
def health_check():
    """
    Service self-check endpoint to verify Python runtime environment.
    """
    return HealthStatus(
        status="UP",
        python_version=sys.version,
        service="MSME-AI-Engine"
    )

@app.post("/api/ai/features", status_code=status.HTTP_200_OK)
def extract_features(data: dict):
    """
    Extract credit indicators from alternate data transaction timelines.
    """
    return calculate_features(data)

@app.post("/api/ai/score", status_code=status.HTTP_200_OK)
def score_business(features: dict):
    """
    Compute credit score and risk grades from calculated features.
    """
    return calculate_credit_score(features)

@app.post("/api/ai/forecast", status_code=status.HTTP_200_OK)
def simulate_forecast(payload: dict):
    """
    Forecast 6 months of future cash flows and debt service coverage.
    """
    return forecast_cash_flows(payload)




if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
