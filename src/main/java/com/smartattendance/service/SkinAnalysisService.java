package com.smartattendance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartattendance.dto.SkinAnalysisHistoryDto;
import com.smartattendance.model.SkinAnalysisRecord;
import com.smartattendance.model.Staff;
import com.smartattendance.repository.SkinAnalysisRepository;
import com.smartattendance.repository.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SkinAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(SkinAnalysisService.class);

    private final SkinAnalysisRepository skinAnalysisRepository;
    private final StaffRepository staffRepository;
    private final ObjectMapper objectMapper;

    public SkinAnalysisService(SkinAnalysisRepository skinAnalysisRepository,
                               StaffRepository staffRepository,
                               ObjectMapper objectMapper) {
        this.skinAnalysisRepository = skinAnalysisRepository;
        this.staffRepository = staffRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveAnalysisRecord(String staffEmail, String rawAnalysisJson) {
        if (staffEmail == null || rawAnalysisJson == null) {
            return;
        }

        Optional<Staff> staffOpt = staffRepository.findByEmail(staffEmail);
        if (staffOpt.isEmpty()) {
            log.warn("Cannot persist skin analysis: staff not found for email {}", staffEmail);
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(rawAnalysisJson);
            if (!rootNode.path("success").asBoolean(false)) {
                return;
            }

            JsonNode analysis = rootNode.path("analysis");
            SkinAnalysisRecord record = new SkinAnalysisRecord();
            record.setStaff(staffOpt.get());
            record.setRawAnalysisJson(rawAnalysisJson);

            record.setAcneScore(extractScore(analysis, "acne_and_blemishes"));
            record.setPoreScore(extractScore(analysis, "pore_visibility"));
            record.setTextureScore(extractScore(analysis, "texture_smoothness"));
            record.setDarkCircleScore(extractScore(analysis, "dark_circles"));
            record.setPigmentationScore(extractScore(analysis, "pigmentation_evenness"));
            record.setHydrationScore(extractScore(analysis, "hydration_radiance"));

            skinAnalysisRepository.save(record);
            log.info("Persisted skin diagnostic record for staff {}", staffEmail);

        } catch (Exception e) {
            log.error("Failed to parse and persist skin analysis result", e);
        }
    }

    private Double extractScore(JsonNode analysis, String metricKey) {
        JsonNode node = analysis.path(metricKey).path("score");
        return node.isNumber() ? node.asDouble() : null;
    }

    @Transactional(readOnly = true)
    public List<SkinAnalysisHistoryDto> getHistoryForStaff(String staffEmail) {
        return skinAnalysisRepository.findByStaffEmailOrderByCreatedAtDesc(staffEmail)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SkinAnalysisHistoryDto> getHistoryByStaffId(Long staffId) {
        return skinAnalysisRepository.findByStaffIdOrderByCreatedAtDesc(staffId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private SkinAnalysisHistoryDto mapToDto(SkinAnalysisRecord record) {
        Staff staff = record.getStaff();
        return new SkinAnalysisHistoryDto(
                record.getId(),
                staff != null ? staff.getId() : null,
                staff != null ? staff.getFullName() : "Unknown",
                record.getAcneScore(),
                record.getPoreScore(),
                record.getTextureScore(),
                record.getDarkCircleScore(),
                record.getPigmentationScore(),
                record.getHydrationScore(),
                record.getCreatedAt()
        );
    }
}
