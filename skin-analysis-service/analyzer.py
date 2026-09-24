import cv2
import numpy as np
import mediapipe as mp

from utils.face_zones import extract_facial_zones
from utils.preprocessing import apply_white_balance_grayworld, extract_lab_channels
from utils.metrics import (
    compute_glcm_texture,
    detect_pores_and_spots,
    detect_erythema_acne,
    analyze_under_eye_dark_circles
)
from utils.scoring import compute_diagnostic_scores

# Initialize MediaPipe Face Mesh
mp_face_mesh = mp.solutions.face_mesh
face_mesh = mp_face_mesh.FaceMesh(
    static_image_mode=True,
    max_num_faces=1,
    refine_landmarks=True,
    min_detection_confidence=0.5
)

def run_face_and_skin_analysis(image: np.ndarray) -> dict:
    """
    Executes full pipeline:
    1. Landmark detection
    2. Color calibration (Gray-World)
    3. ROI extraction for 7 facial zones
    4. Morphological and color space CV metrics
    5. Scoring aggregation
    """
    h, w, _ = image.shape
    rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    results = face_mesh.process(rgb_image)
    if not results.multi_face_landmarks:
        return {
            "success": False,
            "message": "No face detected in the photo. Please upload a clear front-facing image."
        }

    raw_landmarks = results.multi_face_landmarks[0].landmark
    lm_coords = [(int(lm.x * w), int(lm.y * h)) for lm in raw_landmarks]

    # Normalized landmark output for React Canvas rendering
    normalized_landmarks = [
        {"x": round(lm.x, 4), "y": round(lm.y, 4), "z": round(lm.z, 4)}
        for lm in raw_landmarks
    ]

    # Preprocess image to cancel lighting bias
    balanced_img = apply_white_balance_grayworld(image)
    gray_full = cv2.cvtColor(balanced_img, cv2.COLOR_BGR2GRAY)
    l_full, a_full, b_full = extract_lab_channels(balanced_img)

    # Extract ROI zones
    extracted_zones = extract_facial_zones(balanced_img, lm_coords)

    zone_metrics = {}
    bounding_boxes = {}

    for zone_name, zone_data in extracted_zones.items():
        crop = zone_data["crop"]
        mask = zone_data["mask"]
        bbox = zone_data["bbox"]
        bounding_boxes[zone_name] = bbox

        gray_crop = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY)
        l_crop, a_crop, _ = extract_lab_channels(crop)

        # GLCM Texture analysis
        glcm_res = compute_glcm_texture(gray_crop, mask)

        # Pores and spots analysis
        pore_res = detect_pores_and_spots(gray_crop, mask)

        # Acne / Erythema analysis via 'a*' channel
        acne_res = detect_erythema_acne(a_crop, mask)

        # Lightness variance (pigmentation)
        valid_l = l_crop[mask > 0] if mask is not None and np.count_nonzero(mask) > 0 else l_crop.ravel()
        lightness_std = float(np.std(valid_l)) if len(valid_l) > 0 else 5.0

        zone_metrics[zone_name] = {
            "glcm": glcm_res,
            "pores": pore_res,
            "acne": acne_res,
            "lightness_std": lightness_std
        }

    # Under-eye comparison against cheek baseline
    left_eye_data = extracted_zones.get("under_eye_left")
    left_cheek_data = extracted_zones.get("left_cheek")

    if left_eye_data and left_cheek_data:
        eye_l, _, _ = extract_lab_channels(left_eye_data["crop"])
        cheek_l, _, _ = extract_lab_channels(left_cheek_data["crop"])
        under_eye_metric = analyze_under_eye_dark_circles(eye_l, cheek_l)
    else:
        under_eye_metric = {"delta_l": 0.0, "severity": "None"}

    # Generate diagnostic scores
    scores = compute_diagnostic_scores(zone_metrics, under_eye_metric)

    # Formulate targeted AM/PM skincare routine
    from utils.recommendations import generate_skincare_routine
    routine = generate_skincare_routine(scores)

    return {
        "success": True,
        "image_dimensions": {"width": w, "height": h},
        "landmarks": normalized_landmarks,
        "bounding_boxes": bounding_boxes,
        "analysis": scores,
        "routine": routine,
        "callout_nodes": [
            {"label": "Dark Circles (Left)", "landmark_index": 374},
            {"label": "Dark Circles (Right)", "landmark_index": 145},
            {"label": "Cheek Texture / Pores", "landmark_index": 205},
            {"label": "Forehead Zone", "landmark_index": 10},
            {"label": "Chin Zone", "landmark_index": 152}
        ]
    }
