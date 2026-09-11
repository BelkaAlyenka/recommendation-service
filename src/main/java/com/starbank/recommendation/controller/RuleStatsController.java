package com.starbank.recommendation.controller;

import com.starbank.recommendation.dto.RuleStatDto;
import com.starbank.recommendation.dto.RuleStatsResponseDto;
import com.starbank.recommendation.repository.RuleRepository;
import com.starbank.recommendation.repository.RuleStatsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class RuleStatsController {

    private final RuleRepository ruleRepository;
    private final RuleStatsRepository ruleStatsRepository;

    public RuleStatsController(RuleRepository ruleRepository, RuleStatsRepository ruleStatsRepository) {
        this.ruleRepository = ruleRepository;
        this.ruleStatsRepository = ruleStatsRepository;
    }

    @GetMapping("/rule/stats")
    public RuleStatsResponseDto getRuleStats() {
        var allRules = ruleRepository.findAll();

        Map<UUID, Long> statsMap = ruleStatsRepository.findAll().stream()
                .collect(Collectors.toMap(s -> s.getRuleId(), s -> s.getCount()));

        List<RuleStatDto> stats = allRules.stream()
                .map(rule -> new RuleStatDto(
                        rule.getId().toString(),
                        String.valueOf(statsMap.getOrDefault(rule.getId(), 0L))
                ))
                .collect(Collectors.toList());

        return new RuleStatsResponseDto(stats);
    }
}
