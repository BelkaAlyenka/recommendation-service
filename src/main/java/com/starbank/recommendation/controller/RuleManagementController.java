package com.starbank.recommendation.controller;

import com.starbank.recommendation.dto.RuleDto;
import com.starbank.recommendation.dto.RuleListContainerDto;
import com.starbank.recommendation.service.RuleManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/rule")
public class RuleManagementController {

    private final RuleManagementService ruleService;

    public RuleManagementController(RuleManagementService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    public ResponseEntity<RuleDto> createRule(@RequestBody RuleDto ruleDto) {
        RuleDto created = ruleService.createRule(ruleDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<RuleListContainerDto> getAllRules() {
        RuleListContainerDto rulesContainer = ruleService.getAllRules();
        return ResponseEntity.ok(rulesContainer);
    }

    @DeleteMapping("/{product_id}")
    public ResponseEntity<Void> deleteRule(@PathVariable("product_id") UUID productId) {
        ruleService.deleteRuleByProductId(productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
