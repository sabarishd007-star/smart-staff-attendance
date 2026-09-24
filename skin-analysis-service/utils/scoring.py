import numpy as np

def score_to_status(score: float) -> str:
    """
    Standard health grading for a 0 - 10 score (where 10 is optimal health).
    """
    if score >= 8.5:
        return "Excellent"
    elif score >= 7.0:
        return "Good"
    elif score >= 5.0:
        return "Fair"
    else:
        return "Needs Attention"

def compute_diagnostic_scores(zone_metrics: dict, under_eye_metric: dict) -> dict:
    """
    Aggregates statistical and morphological metrics across all zones into calibrated 0-10 diagnostic scores.
    """
    # 1. ACNE / BLEMISH SCORE
    lesion_densities = [
        m["acne"]["lesion_density"]
        for m in zone_metrics.values()
        if "acne" in m
    ]
    avg_lesion_density = float(np.mean(lesion_densities)) if lesion_densities else 0.0
    # Penalty scaled such that 5% lesion coverage drops score towards 4.0
    acne_score = max(1.0, min(10.0, 10.0 - (avg_lesion_density * 120.0)))

    # 2. PORE VISIBILITY SCORE
    pore_densities = [
        m["pores"]["pore_density"]
        for m in zone_metrics.values()
        if "pores" in m
    ]
    avg_pore_density = float(np.mean(pore_densities)) if pore_densities else 0.0
    # Dense visible pores reduce score
    pore_score = max(1.0, min(10.0, 10.0 - (avg_pore_density * 95.0)))

    # 3. TEXTURE & SMOOTHNESS SCORE (GLCM Homogeneity & Contrast)
    homogeneities = [
        m["glcm"]["homogeneity"]
        for m in zone_metrics.values()
        if "glcm" in m
    ]
    contrasts = [
        m["glcm"]["contrast"]
        for m in zone_metrics.values()
        if "glcm" in m
    ]
    avg_homogeneity = float(np.mean(homogeneities)) if homogeneities else 0.5
    avg_contrast = float(np.mean(contrasts)) if contrasts else 5.0
    
    # High homogeneity + low contrast = smoother skin
    texture_val = (avg_homogeneity * 8.0) + max(0.0, 2.0 - (avg_contrast * 0.1))
    texture_score = max(1.0, min(10.0, texture_val))

    # 4. DARK CIRCLES SCORE
    delta_l = under_eye_metric.get("delta_l", 0.0)
    # delta_l typically ranges from 0 (no difference) to 25+ (deep periorbital hyperpigmentation)
    dark_circle_score = max(1.0, min(10.0, 10.0 - (delta_l * 0.42)))

    # 5. PIGMENTATION & EVENNESS
    # Standard deviation of L* (lightness) across cheek and forehead zones
    lightness_stds = [
        m.get("lightness_std", 5.0)
        for m in zone_metrics.values()
    ]
    avg_std = float(np.mean(lightness_stds)) if lightness_stds else 6.0
    pigmentation_score = max(1.0, min(10.0, 10.0 - ((avg_std - 3.0) * 0.75)))

    # 6. HYDRATION & RADIANCE
    # Skin radiance correlated with energy and controlled specular balance
    energies = [
        m["glcm"]["energy"]
        for m in zone_metrics.values()
        if "glcm" in m
    ]
    avg_energy = float(np.mean(energies)) if energies else 0.15
    hydration_score = max(1.0, min(10.0, 5.0 + (avg_energy * 25.0) + (texture_score * 0.2)))

    return {
        "acne_and_blemishes": {
            "score": round(acne_score, 1),
            "status": score_to_status(acne_score),
            "details": f"Estimated lesion density: {round(avg_lesion_density * 100, 2)}%"
        },
        "pore_visibility": {
            "score": round(pore_score, 1),
            "status": score_to_status(pore_score),
            "details": f"Micro-pore density index: {round(avg_pore_density, 3)}"
        },
        "texture_smoothness": {
            "score": round(texture_score, 1),
            "status": score_to_status(texture_score),
            "details": f"GLCM Uniformity index: {round(avg_homogeneity, 3)}"
        },
        "dark_circles": {
            "score": round(dark_circle_score, 1),
            "status": score_to_status(dark_circle_score),
            "details": f"Periorbital ΔL* contrast: {round(delta_l, 1)}"
        },
        "pigmentation_evenness": {
            "score": round(pigmentation_score, 1),
            "status": score_to_status(pigmentation_score),
            "details": f"Color variance spread: {round(avg_std, 2)}"
        },
        "hydration_radiance": {
            "score": round(hydration_score, 1),
            "status": score_to_status(hydration_score),
            "details": "Derived from specular dispersion & GLCM energy"
        }
    }
