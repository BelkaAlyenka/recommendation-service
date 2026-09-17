package com.starbank.recommendation.controller;

import com.starbank.recommendation.service.CachingUserStatsService;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST-контроллер для выполнения задач управления.
 * Используется внутренними автоматизированными системами мониторинга и автоматизации (Cron/АБС).
 */
@RestController
@RequestMapping("/management")
public class ManagementController {

    private final CachingUserStatsService cachingUserStatsService;
    private final BuildProperties buildProperties;

    public ManagementController(CachingUserStatsService cachingUserStatsService, BuildProperties buildProperties) {
        this.cachingUserStatsService = cachingUserStatsService;
        this.buildProperties = buildProperties;
    }

    /**
     * Инициирует принудительную очистку всех кэшей агрегированных транзакционных данных клиентов.
     * Вызывается при обновлении банковских витрин данных.
     *
     * @return ResponseEntity со статусом 200 OK при успешном сбросе памяти
     */
    @PostMapping("/clear-caches")
    public ResponseEntity<Void> clearCaches() {
        cachingUserStatsService.clearAllCaches();
        return ResponseEntity.ok().build();
    }

    /**
     * Предоставляет данные о текущей работающей сборке приложения.
     * Используется контуром мониторинга для контроля версий в среде эксплуатации.
     *
     * @return Map, содержащая наименование артефакта и его текущую версию
     */
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
                "name", buildProperties.getArtifact(),
                "version", buildProperties.getVersion()
        );
    }
}
