package com.starbank.recommendation.repository;

import com.starbank.recommendation.domain.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, UUID> {
    void deleteByProductId(UUID productId);

    Optional<RuleEntity> findByProductId(UUID productId);
}