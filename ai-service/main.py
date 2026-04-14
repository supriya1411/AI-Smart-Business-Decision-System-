"""
AI Microservice — Powered by OpenAI
======================================
FastAPI service exposing two AI endpoints:
  POST /ai/sentiment       → GPT-based sentiment analysis (score 1.0–5.0)
  POST /ai/demand-forecast → GPT-based demand forecasting (integer units)

Configuration:
  Set OPENAI_API_KEY in ai-service/.env before running.

Run locally:
  pip install -r requirements.txt
  uvicorn main:app --reload --port 8000
"""

import os
import re
import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
from openai import OpenAI
from dotenv import load_dotenv

# ── Load environment variables from .env file ──────────────────────────────────
load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
OPENAI_MODEL   = os.getenv("OPENAI_MODEL", "gpt-4o-mini")

# ── Initialise OpenAI client ───────────────────────────────────────────────────
client = OpenAI(api_key=OPENAI_API_KEY) if OPENAI_API_KEY else None

app = FastAPI(
    title="AI Smart Business Decision Microservice",
    description="OpenAI-powered sentiment analysis and demand forecasting",
    version="2.0.0",
)


# ── Request / Response schemas ─────────────────────────────────────────────────

class SentimentRequest(BaseModel):
    text: str
    product_id: int

class SentimentResponse(BaseModel):
    score: float
    label: str

class ForecastRequest(BaseModel):
    product_id: int
    history: List[int]

class ForecastResponse(BaseModel):
    forecast: int
    confidence: float


# ── Helper ─────────────────────────────────────────────────────────────────────

def call_openai(prompt: str, max_tokens: int = 50) -> str:
    """Send a prompt to OpenAI and return the assistant reply as a string."""
    if not client:
        raise HTTPException(
            status_code=503,
            detail="OpenAI API key not configured. Set OPENAI_API_KEY in ai-service/.env",
        )
    response = client.chat.completions.create(
        model=OPENAI_MODEL,
        messages=[{"role": "user", "content": prompt}],
        max_tokens=max_tokens,
        temperature=0.2,  # low temperature → deterministic numeric outputs
    )
    return response.choices[0].message.content.strip()


# ── Endpoints ──────────────────────────────────────────────────────────────────

@app.post("/ai/sentiment", response_model=SentimentResponse)
async def analyze_sentiment(request: SentimentRequest):
    """
    Analyze customer review sentiment using OpenAI GPT.
    Returns a score from 1.0 (very negative) to 5.0 (very positive).
    """
    prompt = f"""You are a sentiment analysis engine for an e-commerce platform.
Analyze the following customer review and return ONLY a single decimal number
between 1.0 (very negative) and 5.0 (very positive).
Do not include any explanation — just the number.

Review: "{request.text}"
"""
    raw = call_openai(prompt, max_tokens=10)

    # Extract first float-like token from the response
    match = re.search(r"\d+(\.\d+)?", raw)
    if not match:
        raise HTTPException(status_code=500, detail=f"Unexpected GPT response: {raw}")

    score = float(match.group())
    score = max(1.0, min(5.0, score))  # clamp to valid range
    label = "POSITIVE" if score >= 3.0 else "NEGATIVE"

    return SentimentResponse(score=round(score, 2), label=label)


@app.post("/ai/demand-forecast", response_model=ForecastResponse)
async def demand_forecast(request: ForecastRequest):
    """
    Forecast next-period demand using OpenAI GPT based on weekly sales history.
    Returns integer units for the next week.
    """
    history_str = str(request.history) if request.history else "no data"

    prompt = f"""You are a demand forecasting model for a retail logistics system.
Given the following weekly sales quantities (oldest to most recent):
{history_str}

Predict the demand (integer units) for the NEXT week.
Consider trends and seasonality. Return ONLY a single integer — no explanation.
"""
    raw = call_openai(prompt, max_tokens=10)

    match = re.search(r"\d+", raw)
    if not match:
        raise HTTPException(status_code=500, detail=f"Unexpected GPT response: {raw}")

    forecast = int(match.group())
    forecast = max(0, forecast)

    # Confidence is derived from history availability (GPT doesn't expose this natively)
    confidence = round(0.90 if len(request.history) >= 4 else 0.70, 2)

    return ForecastResponse(forecast=forecast, confidence=confidence)


@app.get("/health")
async def health():
    """Health check — also verifies OpenAI key is configured."""
    return {
        "status": "ok",
        "openai_configured": bool(OPENAI_API_KEY),
        "model": OPENAI_MODEL,
    }


# ── Entry point ────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
