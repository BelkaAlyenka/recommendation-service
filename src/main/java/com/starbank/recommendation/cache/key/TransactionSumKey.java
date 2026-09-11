package com.starbank.recommendation.cache.key;

import java.util.UUID;

public record TransactionSumKey(UUID userId, String productType, String transactionType) {
}
