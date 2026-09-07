package com.starbank.recommendation.dto;

import java.util.List;

public record QueryDto(
        String query,
        List<String> arguments,
        boolean negate
) {
}