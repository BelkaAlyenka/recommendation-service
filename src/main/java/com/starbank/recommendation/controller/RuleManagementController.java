package com.starbank.recommendation.controller;

import com.starbank.recommendation.dto.RuleDto;
import com.starbank.recommendation.dto.RuleListContainerDto;
import com.starbank.recommendation.service.RuleManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST-контроллер для динамического управления маркетинговыми правилами рекомендаций.
 * Предоставляет административный интерфейс для добавления, просмотра и удаления
 * динамических сценариев сотрудниками (менеджерами) банка в реальном времени.
 */
@RestController
@RequestMapping("/rule")
public class RuleManagementController {

    private final RuleManagementService ruleService;

    public RuleManagementController(RuleManagementService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * Создает новое динамическое правило рекомендаций в системе.
     * Позволяет определить цепочку условий (queries) и аргументов для таргетинга продукта.
     *
     * @param ruleDto объект переноса данных (DTO) с описанием структуры нового правила
     * @return ResponseEntity, содержащий созданное правило с присвоенным идентификатором
     */
    @PostMapping
    public ResponseEntity<RuleDto> createRule(@RequestBody RuleDto ruleDto) {
        RuleDto created = ruleService.createRule(ruleDto);
        return ResponseEntity.ok(created);
    }

    /**
     * Возвращает полный структурированный список всех динамических правил, зарегистрированных в системе.
     * Используется для проведения маркетингового аудита активных кампаний.
     *
     * @return ResponseEntity, содержащий контейнер со списком всех существующих правил
     */
    @GetMapping
    public ResponseEntity<RuleListContainerDto> getAllRules() {
        RuleListContainerDto rulesContainer = ruleService.getAllRules();
        return ResponseEntity.ok(rulesContainer);
    }

    /**
     * Удаляет динамическое правило из системы по идентификатору связанного с ним продукта.
     * При успешном удалении возвращает пустой ответ со статусом 204 No Content.
     *
     * @param productId уникальный идентификатор (UUID) продукта, правило для которого необходимо удалить
     * @return ResponseEntity со статусом 204 No Content
     */
    @DeleteMapping("/{product_id}")
    public ResponseEntity<Void> deleteRule(@PathVariable("product_id") UUID productId) {
        ruleService.deleteRuleByProductId(productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}