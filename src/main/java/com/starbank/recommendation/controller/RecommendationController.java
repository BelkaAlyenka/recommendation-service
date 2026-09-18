package com.starbank.recommendation.controller;

import com.starbank.recommendation.dto.RecommendationResponseDto;
import com.starbank.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommendation/{user_id}")
    public ResponseEntity<RecommendationResponseDto> getRecommendation(@PathVariable("user_id") String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        RecommendationResponseDto response = recommendationService.getRecommendations(userId);
        return ResponseEntity.ok(response);
    }
}