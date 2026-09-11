package com.starbank.recommendation.controller;

import com.starbank.recommendation.service.CachingUserStatsService;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/management")
public class ManagementController {

    private final CachingUserStatsService cachingUserStatsService;
    private final BuildProperties buildProperties;

    public ManagementController(CachingUserStatsService cachingUserStatsService, BuildProperties buildProperties) {
        this.cachingUserStatsService = cachingUserStatsService;
        this.buildProperties = buildProperties;
    }

    @PostMapping("/clear-caches")
    public ResponseEntity<Void> clearCaches() {
        cachingUserStatsService.clearAllCaches();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
                "name", buildProperties.getArtifact(),
                "version", buildProperties.getVersion()
        );
    }
}
