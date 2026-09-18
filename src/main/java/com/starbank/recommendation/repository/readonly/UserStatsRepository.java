package com.starbank.recommendation.repository.readonly;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Primary
@Repository
public interface UserStatsRepository extends JpaRepository<TransactionReadOnlyEntity, UUID> {

    @Query(value = """
            SELECT COUNT(t.id) 
            FROM transactions t 
            WHERE t.user_id = :userId 
              AND t.product_type = :productType
            """, nativeQuery = true)
    long countTransactionsByProduct(
            @Param("userId") UUID userId,
            @Param("productType") String productType
    );

    @Query(value = """
            SELECT COALESCE(SUM(t.amount), 0) 
            FROM transactions t 
            WHERE t.user_id = :userId 
              AND t.product_type = :productType 
              AND t.transaction_type = :transactionType
            """, nativeQuery = true)
    long sumTransactionsAmount(
            @Param("userId") UUID userId,
            @Param("productType") String productType,
            @Param("transactionType") String transactionType
    );
}
