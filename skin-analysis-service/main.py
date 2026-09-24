from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import cv2
import numpy as np
import traceback

from analyzer import run_face_and_skin_analysis

app = FastAPI(
    title="Dermatological Diagnostic Analysis API",
    description="Microservice providing MediaPipe facial mesh mapping, ROI extraction, and computer vision skin diagnostics.",
    version="1.0.0"
)

# Enable CORS for React frontend direct calls or Spring Boot proxy
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "skin-analysis-service"}

@app.post("/api/v1/analyze-photo")
async def analyze_photo(file: UploadFile = File(...)):
    # Validate content type
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=400,
            detail="File uploaded must be an image (JPEG, PNG, WEBP)."
        )

    try:
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            raise HTTPException(
                status_code=400,
                detail="Could not decode image. Please provide a valid, uncorrupted image file."
            )

        # Run complete CV + MediaPipe pipeline
        result = run_face_and_skin_analysis(image)
        return result

    except Exception as e:
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Error executing image analysis: {str(e)}"
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8001, reload=True)
