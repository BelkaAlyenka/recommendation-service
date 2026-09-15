package com.starbank.recommendation;

import com.starbank.recommendation.repository.readonly.UserReadOnlyEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.UUID;

@Component
public class TestDataInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(u) FROM UserReadOnlyEntity u WHERE u.username = :username", Long.class)
                .setParameter("username", "test_user")
                .getSingleResult();

        if (count == 0) {
            entityManager.createNativeQuery(
                            "INSERT INTO users (id, username, first_name, last_name) VALUES (?, ?, ?, ?)")
                    .setParameter(1, UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    .setParameter(2, "test_user")
                    .setParameter(3, "Иван")
                    .setParameter(4, "Иванов")
                    .executeUpdate();

            System.out.println(">>> [УСПЕХ] Тестовый пользователь test_user успешно добавлен в БД!");
        }
    }
}
