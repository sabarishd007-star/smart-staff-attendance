import cv2
import numpy as np

def apply_white_balance_grayworld(image: np.ndarray) -> np.ndarray:
    """
    Applies Gray-World White Balance algorithm to remove camera color casting
    which frequently ruins skin redness and pigmentation analysis.
    """
    result = image.astype(np.float32)
    avg_b = np.mean(result[:, :, 0])
    avg_g = np.mean(result[:, :, 1])
    avg_r = np.mean(result[:, :, 2])

    avg_gray = (avg_b + avg_g + avg_r) / 3.0
    if avg_b > 0 and avg_g > 0 and avg_r > 0:
        result[:, :, 0] = np.clip(result[:, :, 0] * (avg_gray / avg_b), 0, 255)
        result[:, :, 1] = np.clip(result[:, :, 1] * (avg_gray / avg_g), 0, 255)
        result[:, :, 2] = np.clip(result[:, :, 2] * (avg_gray / avg_r), 0, 255)

    return result.astype(np.uint8)

def apply_clahe(gray_image: np.ndarray, clip_limit: float = 2.0, tile_grid_size: tuple = (8, 8)) -> np.ndarray:
    """
    Contrast Limited Adaptive Histogram Equalization for enhanced pore & texture edge visibility.
    """
    clahe = cv2.createCLAHE(clipLimit=clip_limit, tileGridSize=tile_grid_size)
    return clahe.apply(gray_image)

def extract_lab_channels(image: np.ndarray):
    """
    Converts BGR image into CIE L*a*b* space.
    L* = Luminance (Lightness: 0=black, 100=white)
    a* = Green to Magenta/Red spectrum (High positive = Erythema/Redness/Acne)
    b* = Blue to Yellow spectrum (High positive = Hyperpigmentation/Melanin/Sallowness)
    """
    lab = cv2.cvtColor(image, cv2.COLOR_BGR2LAB)
    l_channel, a_channel, b_channel = cv2.split(lab)
    return l_channel, a_channel, b_channel
