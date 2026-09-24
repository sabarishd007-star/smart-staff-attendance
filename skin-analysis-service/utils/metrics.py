import cv2
import numpy as np
from skimage.feature import graycomatrix, graycoprops

def compute_glcm_texture(gray_roi: np.ndarray, mask: np.ndarray = None) -> dict:
    """
    Computes Gray-Level Co-occurrence Matrix (GLCM) texture metrics:
    - Contrast: measures local variations
    - Homogeneity: measures uniformity of skin texture
    - Energy: measures orderliness / smoothness
    """
    if gray_roi is None or gray_roi.size == 0:
        return {"contrast": 0.0, "homogeneity": 1.0, "energy": 1.0}

    # Resample to 64 levels for speed and noise reduction
    quantized = (gray_roi // 4).astype(np.uint8)

    distances = [1, 2]
    angles = [0, np.pi/4, np.pi/2, 3*np.pi/4]
    glcm = graycomatrix(quantized, distances=distances, angles=angles, levels=64, symmetric=True, normed=True)

    contrast = float(np.mean(graycoprops(glcm, 'contrast')))
    homogeneity = float(np.mean(graycoprops(glcm, 'homogeneity')))
    energy = float(np.mean(graycoprops(glcm, 'energy')))

    return {
        "contrast": contrast,
        "homogeneity": homogeneity,
        "energy": energy
    }

def detect_pores_and_spots(gray_roi: np.ndarray, mask: np.ndarray = None) -> dict:
    """
    Detects micro-depressions and enlarged pores using Black-Hat morphological transformation
    (Highlights dark regions on lighter backgrounds).
    """
    if gray_roi is None or gray_roi.size == 0:
        return {"pore_density": 0.0, "pore_count": 0}

    # Structuring element suited for typical 2-6px pore sizes
    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    blackhat = cv2.morphologyEx(gray_roi, cv2.MORPH_BLACKHAT, kernel)

    # Threshold dark depressions
    _, thresh = cv2.threshold(blackhat, 10, 255, cv2.THRESH_BINARY)

    if mask is not None and mask.shape == thresh.shape:
        thresh = cv2.bitwise_and(thresh, thresh, mask=mask)
        valid_area = np.count_nonzero(mask)
    else:
        valid_area = thresh.size

    pore_pixels = np.count_nonzero(thresh)
    pore_density = (pore_pixels / max(1, valid_area))

    # Find connected components to estimate pore count
    num_labels, _ = cv2.connectedComponents(thresh)
    pore_count = max(0, num_labels - 1)

    return {
        "pore_density": float(pore_density),
        "pore_count": int(pore_count)
    }

def detect_erythema_acne(a_channel_roi: np.ndarray, mask: np.ndarray = None) -> dict:
    """
    Detects inflammation and acne by assessing localized hyper-redness in the CIE L*a*b* 'a*' channel.
    Values significantly above the zone's median a* indicate erythematous lesions.
    """
    if a_channel_roi is None or a_channel_roi.size == 0:
        return {"redness_index": 0.0, "lesion_density": 0.0}

    if mask is not None and np.count_nonzero(mask) > 0:
        pixels = a_channel_roi[mask > 0]
    else:
        pixels = a_channel_roi.ravel()

    median_a = float(np.median(pixels))
    std_a = float(np.std(pixels))

    # Points with redness > median + 1.8 * std are classified as inflamed lesions/blemishes
    threshold = median_a + max(3.0, 1.8 * std_a)
    lesions = pixels[pixels > threshold]
    lesion_density = len(lesions) / max(1, len(pixels))

    return {
        "redness_index": median_a,
        "lesion_density": float(lesion_density)
    }

def analyze_under_eye_dark_circles(eye_l: np.ndarray, cheek_l: np.ndarray) -> dict:
    """
    Quantifies dark circles by comparing the lightness (L*) of under-eye skin
    against the cheek baseline lightness.
    """
    if eye_l is None or cheek_l is None or eye_l.size == 0 or cheek_l.size == 0:
        return {"delta_l": 0.0, "severity": "None"}

    mean_eye_l = float(np.mean(eye_l))
    mean_cheek_l = float(np.mean(cheek_l))

    # delta_l: How much darker is under-eye compared to adjacent cheek
    delta_l = max(0.0, mean_cheek_l - mean_eye_l)

    return {
        "eye_lightness": mean_eye_l,
        "cheek_lightness": mean_cheek_l,
        "delta_l": delta_l
    }
