package com.starbank.recommendation.service;

import com.starbank.recommendation.dto.RecommendationDto;
import com.starbank.recommendation.dto.RecommendationResponseDto;
import com.starbank.recommendation.rule.RecommendationRuleSet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> rules;

    public RecommendationService(List<RecommendationRuleSet> rules) {
        this.rules = rules;
    }

    public RecommendationResponseDto getRecommendations(UUID userId) {
        List<RecommendationDto> activeRecommendations = rules.stream()
                .map(rule -> rule.check(userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new RecommendationResponseDto(userId, activeRecommendations);
    }
}