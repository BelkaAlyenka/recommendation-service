package com.starbank.recommendation.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class RecommendationRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasProductType(UUID userId, String productType) {
        String sql = """
                SELECT COUNT(*) 
                FROM transactions t 
                JOIN products p ON t.product_id = p.id 
                WHERE t.user_id = ? AND p.type = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId.toString(), productType);
        return count != null && count > 0;
    }

    public double getSumByTransactionAndProduct(UUID userId, String productType, String transactionType) {
        String sql = """
                SELECT COALESCE(SUM(t.amount), 0) 
                FROM transactions t 
                JOIN products p ON t.product_id = p.id 
                WHERE t.user_id = ? AND p.type = ? AND t.type = ?
                """;
        Double sum = jdbcTemplate.queryForObject(sql, Double.class, userId.toString(), productType, transactionType);
        return sum != null ? sum : 0.0;
    }

    public double getTotalSumByType(UUID userId, String transactionType) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = ?";
        Double sum = jdbcTemplate.queryForObject(sql, Double.class, userId.toString(), transactionType);
        return sum != null ? sum : 0.0;
    }
}
