package com.starbank.recommendation.service;

import com.starbank.recommendation.domain.QueryEntity;
import com.starbank.recommendation.domain.RuleEntity;
import com.starbank.recommendation.dto.RecommendationDto;
import com.starbank.recommendation.dto.RecommendationResponseDto;
import com.starbank.recommendation.repository.RuleRepository;
import com.starbank.recommendation.repository.RuleStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecommendationService {

    private final RuleRepository ruleRepository;
    private final CachingUserStatsService cachingUserStatsService;
    private final RuleStatsRepository ruleStatsRepository;

    public RecommendationService(
            RuleRepository ruleRepository,
            CachingUserStatsService cachingUserStatsService,
            RuleStatsRepository ruleStatsRepository) {
        this.ruleRepository = ruleRepository;
        this.cachingUserStatsService = cachingUserStatsService;
        this.ruleStatsRepository = ruleStatsRepository;
    }

    @Transactional
    public RecommendationResponseDto getRecommendations(UUID userId) {
        List<RuleEntity> rules = ruleRepository.findAll();
        List<RecommendationDto> validRecommendations = new ArrayList<>();

        for (RuleEntity rule : rules) {
            boolean isRuleApplicable = true;

            if (rule.getQueries() != null) {
                for (QueryEntity query : rule.getQueries()) {
                    if (!checkQueryCondition(userId, query)) {
                        isRuleApplicable = false;
                        break;
                    }
                }
            }

            if (isRuleApplicable) {
                ruleStatsRepository.incrementCount(rule.getId());

                validRecommendations.add(new RecommendationDto(
                        rule.getProductId(),
                        rule.getProductName(),
                        rule.getProductText()
                ));
            }
        }

        return new RecommendationResponseDto(userId, validRecommendations);
    }

    private boolean checkQueryCondition(UUID userId, QueryEntity query) {
        boolean result = false;
        List<String> args = query.getArguments();

        if (args == null || args.isEmpty()) {
            return query.isNegate() ? !result : result;
        }

        switch (query.getQuery()) {
            case "USER_OF": {
                String productType = args.get(0);
                result = checkUserOf(userId, productType);
                break;
            }
            case "ACTIVE_USER_OF": {
                String productType = args.get(0);
                result = checkActiveUserOf(userId, productType);
                break;
            }
            case "TRANSACTION_SUM_COMPARE": {
                if (args.size() >= 4) {
                    String productType = args.get(0);
                    String transactionType = args.get(1);
                    String operator = args.get(2);
                    int value = Integer.parseInt(args.get(3));
                    result = checkTransactionSumCompare(userId, productType, transactionType, operator, value);
                }
                break;
            }
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW": {
                if (args.size() >= 2) {
                    String productType = args.get(0);
                    String operator = args.get(1);
                    result = checkDepositWithdrawCompare(userId, productType, operator);
                }
                break;
            }
            default:
                result = false;
        }

        return query.isNegate() ? !result : result;
    }

    private boolean checkUserOf(UUID userId, String productType) {
        long count = cachingUserStatsService.getTransactionCount(userId, productType);
        return count >= 1;
    }

    private boolean checkActiveUserOf(UUID userId, String productType) {
        long count = cachingUserStatsService.getTransactionCount(userId, productType);
        return count >= 5;
    }

    private boolean checkTransactionSumCompare(UUID userId, String productType, String transactionType, String operator, int value) {
        long sum = cachingUserStatsService.getTransactionSum(userId, productType, transactionType);
        return compareValues(sum, operator, value);
    }

    private boolean checkDepositWithdrawCompare(UUID userId, String productType, String operator) {
        long delta = cachingUserStatsService.getDepositWithdrawDelta(userId, productType);
        return compareValues(delta, operator, 0);
    }

    private boolean compareValues(long actual, String operator, long expected) {
        if (operator == null) return false;
        switch (operator) {
            case ">":
                return actual > expected;
            case "<":
                return actual < expected;
            case "=":
                return actual == expected;
            case ">=":
                return actual >= expected;
            case "<=":
                return actual <= expected;
            default:
                return false;
        }
    }
}