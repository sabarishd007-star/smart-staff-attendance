from typing import Dict, List, Any

def generate_skincare_routine(analysis_scores: Dict[str, Any]) -> Dict[str, Any]:
    """
    Formulates a targeted, clinical AM/PM skincare routine based on identified skin concerns.
    Flags concerns when score < 7.0 (Needs Attention or Fair).
    """
    am_steps: List[Dict[str, str]] = []
    pm_steps: List[Dict[str, str]] = []
    active_ingredients: List[str] = []
    lifestyle_tips: List[str] = []

    # Baseline Cleanser & Hydration
    am_steps.append({
        "step": "Cleanse",
        "product_type": "Gentle Low-pH Hydrating Cleanser",
        "instruction": "Cleanse skin with lukewarm water to remove overnight sebum without stripping the moisture barrier."
    })

    pm_steps.append({
        "step": "Double Cleanse",
        "product_type": "Micellar / Cleansing Oil followed by Foaming Cleanser",
        "instruction": "Effectively melt away sunscreen, pollution particulates, and micro-debris accumulated through the day."
    })

    # Metric Extractions
    acne_score = analysis_scores.get("acne_and_blemishes", {}).get("score", 10.0)
    pore_score = analysis_scores.get("pore_visibility", {}).get("score", 10.0)
    texture_score = analysis_scores.get("texture_smoothness", {}).get("score", 10.0)
    dark_circles_score = analysis_scores.get("dark_circles", {}).get("score", 10.0)
    pigment_score = analysis_scores.get("pigmentation_evenness", {}).get("score", 10.0)
    hydration_score = analysis_scores.get("hydration_radiance", {}).get("score", 10.0)

    # 1. ACNE & BLEMISHES (Score < 7.5)
    if acne_score < 7.5:
        active_ingredients.append("Salicylic Acid (BHA 2%)")
        active_ingredients.append("Zinc PCA")
        am_steps.append({
            "step": "Targeted Treatment",
            "product_type": "Niacinamide 5% + Zinc 1% Serum",
            "instruction": "Regulates sebum excretion and reduces localized inflammatory erythema."
        })
        pm_steps.append({
            "step": "Exfoliant (2-3x/week)",
            "product_type": "BHA 2% (Salicylic Acid) Liquid Exfoliant",
            "instruction": "Penetrates lipid layers inside pores to clear impactions and micro-comedones."
        })
        lifestyle_tips.append("Disinfect smartphone screens and replace pillowcases twice weekly.")

    # 2. ENLARGED PORES & TEXTURAL IRREGULARITIES (Score < 7.5)
    if pore_score < 7.5 or texture_score < 7.5:
        if "Niacinamide" not in " ".join(active_ingredients):
            active_ingredients.append("Niacinamide (Vitamin B3)")
        active_ingredients.append("Lactic Acid (AHA)")
        pm_steps.append({
            "step": "Cellular Turnover",
            "product_type": "Retinol (0.2% - 0.5%) or Bakuchiol Serum",
            "instruction": "Accelerates keratinocyte turnover, firming pore walls and smoothing textural roughness."
        })
        lifestyle_tips.append("Avoid physical walnut or apricot facial scrubs which produce micro-fissures in epidermal layers.")

    # 3. DARK CIRCLES & PERIORBITAL PIGMENTATION (Score < 7.0)
    if dark_circles_score < 7.0:
        active_ingredients.append("Caffeine Extract")
        active_ingredients.append("Vitamin K / Peptides")
        am_steps.append({
            "step": "Eye Contour",
            "product_type": "Caffeine 5% + EGCG De-puffing Eye Solution",
            "instruction": "Vasoconstricts periorbital micro-capillaries to diminish blue/violet darkness and morning fluid retention."
        })
        pm_steps.append({
            "step": "Eye Nourishment",
            "product_type": "Multi-Peptide + Ceramide Eye Cream",
            "instruction": "Strengthens extremely thin under-eye dermal thickness to mask underlying vascular networks."
        })
        lifestyle_tips.append("Prioritize 7-8 hours of sleep and keep head slightly elevated to prevent orbital lymph accumulation.")

    # 4. UNEVEN PIGMENTATION & SUN SPOTS (Score < 7.5)
    if pigment_score < 7.5:
        active_ingredients.append("Vitamin C (L-Ascorbic Acid 10-15%)")
        active_ingredients.append("Alpha Arbutin 2%")
        am_steps.append({
            "step": "Antioxidant Shield",
            "product_type": "L-Ascorbic Acid (Vitamin C 15%) + Ferulic Acid",
            "instruction": "Inhibits tyrosinase enzymatic activity to suppress melanogenesis and neutralize free radicals."
        })

    # 5. HYDRATION & BARRIER HEALTH
    if hydration_score < 7.5:
        active_ingredients.append("Hyaluronic Acid (Multi-molecular weight)")
        active_ingredients.append("Centella Asiatica / Ceramide Complex")
        am_steps.append({
            "step": "Hydration Infusion",
            "product_type": "Multi-Molecular Hyaluronic Acid Serum",
            "instruction": "Apply to slightly damp skin to draw hydration into outer epidermal layers."
        })
        pm_steps.append({
            "step": "Lipid Barrier Repair",
            "product_type": "Ceramide & Squalane Rich Emollient Cream",
            "instruction": "Locks in hydration overnight, replenishing the stratum corneum lipid barrier."
        })
        lifestyle_tips.append("Maintain minimum 2.5L daily hydration and avoid hot water when cleansing.")
    else:
        # Default moisturizer for balanced skin
        am_steps.append({
            "step": "Moisturize",
            "product_type": "Lightweight Gel-Cream Moisturizer",
            "instruction": "Maintains optimum hydration without congesting skin."
        })
        pm_steps.append({
            "step": "Moisturize",
            "product_type": "Nutritive Night Moisturizer",
            "instruction": "Replenishes skin barrier lipids during overnight cellular regeneration."
        })

    # Non-negotiable Morning Sun Protection
    am_steps.append({
        "step": "UV Defense",
        "product_type": "Broad-Spectrum SPF 50+ PA++++ Sunscreen",
        "instruction": "Apply two finger lengths over face and neck as the final morning step. Prevents exacerbation of all skin concerns."
    })

    return {
        "am_routine": am_steps,
        "pm_routine": pm_steps,
        "key_active_ingredients": list(set(active_ingredients)),
        "lifestyle_tips": lifestyle_tips
    }
