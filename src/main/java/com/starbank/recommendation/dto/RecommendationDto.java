package com.starbank.recommendation.dto;

import java.util.UUID;

public record RecommendationDto(
        UUID id,
        String name,
        String text
) {
}