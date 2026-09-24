package com.smartattendance.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "skin_analysis_record")
public class SkinAnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "acne_score")
    private Double acneScore;

    @Column(name = "pore_score")
    private Double poreScore;

    @Column(name = "texture_score")
    private Double textureScore;

    @Column(name = "dark_circle_score")
    private Double darkCircleScore;

    @Column(name = "pigmentation_score")
    private Double pigmentationScore;

    @Column(name = "hydration_score")
    private Double hydrationScore;

    @Lob
    @Column(name = "raw_analysis_json", columnDefinition = "TEXT")
    private String rawAnalysisJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SkinAnalysisRecord() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
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

    public String getRawAnalysisJson() {
        return rawAnalysisJson;
    }

    public void setRawAnalysisJson(String rawAnalysisJson) {
        this.rawAnalysisJson = rawAnalysisJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
