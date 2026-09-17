package com.starbank.recommendation.controller;

import com.starbank.recommendation.dto.RecommendationResponseDto;
import com.starbank.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST-контроллер для предоставления персональных рекомендаций конечным пользователям.
 * Является основной точкой интеграции для фронтенд-приложений и мобильного банка.
 */
@RestController
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Возвращает приоритезированный список персональных предложений для конкретного пользователя.
     *
     * @param userIdStr строковое представление уникального идентификатора пользователя (UUID)
     * @return ResponseEntity, содержащий RecommendationResponseDto со списком доступных продуктов
     */
    @GetMapping("/recommendation/{user_id}")
    public ResponseEntity<RecommendationResponseDto> getRecommendation(@PathVariable("user_id") String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        RecommendationResponseDto response = recommendationService.getRecommendations(userId);
        return ResponseEntity.ok(response);
    }
}