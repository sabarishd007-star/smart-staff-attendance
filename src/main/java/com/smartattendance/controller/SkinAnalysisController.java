package com.smartattendance.controller;

import com.smartattendance.dto.SkinAnalysisHistoryDto;
import com.smartattendance.service.SkinAnalysisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/skin")
@CrossOrigin(origins = "*")
public class SkinAnalysisController {

    @Value("${skin.analysis.service.url:http://localhost:8001/api/v1/analyze-photo}")
    private String pythonServiceUrl;

    private final RestTemplate restTemplate;
    private final SkinAnalysisService skinAnalysisService;

    public SkinAnalysisController(SkinAnalysisService skinAnalysisService) {
        this.restTemplate = new RestTemplate();
        this.skinAnalysisService = skinAnalysisService;
    }

    @PostMapping(value = "/analyze-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeSkinPhoto(@RequestParam("file") MultipartFile file,
                                              Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Uploaded photo cannot be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("File must be an image (JPEG, PNG, WEBP).");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileAsResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Forward request to Python FastAPI microservice
            ResponseEntity<String> response = restTemplate.postForEntity(
                    pythonServiceUrl,
                    requestEntity,
                    String.class
            );

            // If analysis succeeded, persist scan record for the authenticated staff user
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String staffEmail = authentication != null ? authentication.getName() : null;
                skinAnalysisService.saveAnalysisRecord(staffEmail, response.getBody());
            }

            return ResponseEntity.status(response.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());

        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read image payload: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Skin analysis AI service error: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<SkinAnalysisHistoryDto>> getMyHistory(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<SkinAnalysisHistoryDto> history = skinAnalysisService.getHistoryForStaff(authentication.getName());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/staff/{staffId}")
    public ResponseEntity<List<SkinAnalysisHistoryDto>> getStaffHistory(@PathVariable Long staffId) {
        List<SkinAnalysisHistoryDto> history = skinAnalysisService.getHistoryByStaffId(staffId);
        return ResponseEntity.ok(history);
    }
}
