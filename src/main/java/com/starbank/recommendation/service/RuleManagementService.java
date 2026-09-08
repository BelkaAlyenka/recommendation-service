package com.starbank.recommendation.service;

import com.starbank.recommendation.domain.RuleEntity;
import com.starbank.recommendation.dto.RuleDto;
import com.starbank.recommendation.dto.RuleListContainerDto;
import com.starbank.recommendation.mapper.RuleMapper;
import com.starbank.recommendation.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RuleManagementService {

    private final RuleRepository ruleRepository;
    private final RuleMapper ruleMapper;

    public RuleManagementService(RuleRepository ruleRepository, RuleMapper ruleMapper) {
        this.ruleRepository = ruleRepository;
        this.ruleMapper = ruleMapper;
    }

    @Transactional
    public RuleDto createRule(RuleDto ruleDto) {
        RuleEntity entity = ruleMapper.toEntity(ruleDto);
        RuleEntity saved = ruleRepository.save(entity);
        return ruleMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public RuleListContainerDto getAllRules() {
        List<RuleDto> rules = ruleRepository.findAll().stream()
                .map(ruleMapper::toDto)
                .collect(Collectors.toList());
        return new RuleListContainerDto(rules);
    }

    @Transactional
    public void deleteRuleByProductId(UUID productId) {
        ruleRepository.deleteByProductId(productId);
    }
}
