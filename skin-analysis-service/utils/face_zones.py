import cv2
import numpy as np

# MediaPipe canonical landmark indices for key facial skin diagnostic regions
ZONE_LANDMARKS = {
    "forehead": [
        10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288,
        397, 365, 379, 378, 400, 377, 152, 148, 176, 149, 150, 136,
        172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109
    ],
    "left_cheek": [
        234, 93, 132, 58, 172, 136, 150, 149, 176, 148, 152, 214, 212, 192, 210, 187
    ],
    "right_cheek": [
        454, 323, 361, 288, 397, 365, 379, 378, 400, 377, 434, 432, 416, 430, 411
    ],
    "nose": [
        1, 2, 98, 327, 195, 5, 4, 19, 94, 2, 164, 0, 11, 12, 13, 14, 15, 16, 17, 18
    ],
    "under_eye_left": [
        374, 380, 381, 382, 362, 263, 249, 390, 373, 253, 252, 254
    ],
    "under_eye_right": [
        145, 153, 154, 155, 133, 33, 7, 163, 144, 23, 22, 24
    ],
    "chin": [
        152, 148, 176, 149, 150, 136, 172, 377, 400, 378, 379, 365, 397, 175, 199, 208
    ]
}

def extract_facial_zones(image: np.ndarray, lm_list: list) -> dict:
    """
    Extracts bounded ROI crops and polygon masks for key facial zones based on landmark indices.
    Returns:
        dict of {
            zone_name: {
                "crop": np.ndarray (BGR),
                "mask": np.ndarray (binary mask),
                "bbox": {"x": int, "y": int, "width": int, "height": int}
            }
        }
    """
    zones = {}
    h, w = image.shape[:2]

    for zone_name, indices in ZONE_LANDMARKS.items():
        points = [lm_list[i] for i in indices if i < len(lm_list)]
        if len(points) < 3:
            continue

        pts_array = np.array(points, dtype=np.int32)
        x, y, bw, bh = cv2.boundingRect(pts_array)

        # Apply proportional safety padding
        pad_x = int(bw * 0.08)
        pad_y = int(bh * 0.08)

        x1 = max(0, x - pad_x)
        y1 = max(0, y - pad_y)
        x2 = min(w, x + bw + pad_x)
        y2 = min(h, y + bh + pad_y)

        cropped = image[y1:y2, x1:x2]
        if cropped.size == 0 or cropped.shape[0] < 5 or cropped.shape[1] < 5:
            continue

        # Create localized mask for the polygon region within the bounding box
        local_pts = pts_array - np.array([x1, y1])
        mask = np.zeros((y2 - y1, x2 - x1), dtype=np.uint8)
        cv2.fillConvexPoly(mask, local_pts, 255)

        zones[zone_name] = {
            "crop": cropped,
            "mask": mask,
            "bbox": {
                "x": int(x1),
                "y": int(y1),
                "width": int(x2 - x1),
                "height": int(y2 - y1)
            }
        }

    return zones
