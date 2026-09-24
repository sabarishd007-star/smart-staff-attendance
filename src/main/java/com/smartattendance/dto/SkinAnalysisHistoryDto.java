package com.smartattendance.dto;

import java.time.LocalDateTime;

public class SkinAnalysisHistoryDto {

    private Long id;
    private Long staffId;
    private String staffName;
    private Double acneScore;
    private Double poreScore;
    private Double textureScore;
    private Double darkCircleScore;
    private Double pigmentationScore;
    private Double hydrationScore;
    private LocalDateTime createdAt;

    public SkinAnalysisHistoryDto() {
    }

    public SkinAnalysisHistoryDto(Long id, Long staffId, String staffName,
                                  Double acneScore, Double poreScore, Double textureScore,
                                  Double darkCircleScore, Double pigmentationScore, Double hydrationScore,
                                  LocalDateTime createdAt) {
        this.id = id;
        this.staffId = staffId;
        this.staffName = staffName;
        this.acneScore = acneScore;
        this.poreScore = poreScore;
        this.textureScore = textureScore;
        this.darkCircleScore = darkCircleScore;
        this.pigmentationScore = pigmentationScore;
        this.hydrationScore = hydrationScore;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public Double getAcneScore() {
        return acneScore;
    }

    public void setAcneScore(Double acneScore) {
        this.acneScore = acneScore;
    }

    public Double getPoreScore() {
        return poreScore;
    }

    public void setPoreScore(Double poreScore) {
        this.poreScore = poreScore;
    }

    public Double getTextureScore() {
        return textureScore;
    }

    public void setTextureScore(Double textureScore) {
        this.textureScore = textureScore;
    }

    public Double getDarkCircleScore() {
        return darkCircleScore;
    }

    public void setDarkCircleScore(Double darkCircleScore) {
        this.darkCircleScore = darkCircleScore;
    }

    public Double getPigmentationScore() {
        return pigmentationScore;
    }

    public void setPigmentationScore(Double pigmentationScore) {
        this.pigmentationScore = pigmentationScore;
    }

    public Double getHydrationScore() {
        return hydrationScore;
    }

    public void setHydrationScore(Double hydrationScore) {
        this.hydrationScore = hydrationScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
