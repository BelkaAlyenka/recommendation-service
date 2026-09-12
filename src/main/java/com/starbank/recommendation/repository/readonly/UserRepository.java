package com.starbank.recommendation.repository.readonly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserReadOnlyEntity, UUID> {
    List<UserReadOnlyEntity> findByUsername(String username);
}
