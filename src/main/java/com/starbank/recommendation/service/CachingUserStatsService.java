package com.starbank.recommendation.service;

import com.starbank.recommendation.cache.key.TransactionSumKey;
import com.starbank.recommendation.cache.key.UserProductKey;
import com.starbank.recommendation.repository.readonly.UserStatsRepository;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CachingUserStatsService {

    private final UserStatsRepository userStatsRepository;
    private final Cache<UserProductKey, Long> userTransactionCountCache;
    private final Cache<TransactionSumKey, Long> transactionSumCache;
    private final Cache<UserProductKey, Long> depositWithdrawDeltaCache;

    public CachingUserStatsService(
            UserStatsRepository userStatsRepository,
            Cache<UserProductKey, Long> userTransactionCountCache,
            Cache<TransactionSumKey, Long> transactionSumCache,
            Cache<UserProductKey, Long> depositWithdrawDeltaCache) {
        this.userStatsRepository = userStatsRepository;
        this.userTransactionCountCache = userTransactionCountCache;
        this.transactionSumCache = transactionSumCache;
        this.depositWithdrawDeltaCache = depositWithdrawDeltaCache;
    }

    public long getTransactionCount(UUID userId, String productType) {
        UserProductKey key = new UserProductKey(userId, productType);
        return userTransactionCountCache.get(key, k ->
                userStatsRepository.countTransactionsByProduct(k.userId(), k.productType())
        );
    }

    public long getTransactionSum(UUID userId, String productType, String transactionType) {
        TransactionSumKey key = new TransactionSumKey(userId, productType, transactionType);
        return transactionSumCache.get(key, k ->
                userStatsRepository.sumTransactionsAmount(k.userId(), k.productType(), k.transactionType())
        );
    }

    public long getDepositWithdrawDelta(UUID userId, String productType) {
        UserProductKey key = new UserProductKey(userId, productType);
        return depositWithdrawDeltaCache.get(key, k -> {
            long deposits = userStatsRepository.sumTransactionsAmount(k.userId(), k.productType(), "DEPOSIT");
            long withdraws = userStatsRepository.sumTransactionsAmount(k.userId(), k.productType(), "WITHDRAW");
            return deposits - withdraws;
        });
    }
}
