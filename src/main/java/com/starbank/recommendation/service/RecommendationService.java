package com.starbank.recommendation.service;

import com.starbank.recommendation.domain.QueryEntity;
import com.starbank.recommendation.domain.RuleEntity;
import com.starbank.recommendation.dto.RecommendationDto;
import com.starbank.recommendation.dto.RecommendationResponseDto;
import com.starbank.recommendation.repository.RuleRepository;
import com.starbank.recommendation.repository.RuleStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Бизнес-сервис (оркестратор) рекомендательного движка StarBank.
 * Осуществляет динамическую загрузку правил из базы данных, итерируется по цепочкам
 * маркетинговых условий (Queries), вычисляет их применимость к конкретному пользователю
 * с использованием оптимизированного кэш-слоя и фиксирует статистику показов.
 */
@Service
public class RecommendationService {

    private final RuleRepository ruleRepository;
    private final CachingUserStatsService cachingUserStatsService;
    private final RuleStatsRepository ruleStatsRepository;

    public RecommendationService(
            RuleRepository ruleRepository,
            CachingUserStatsService cachingUserStatsService,
            RuleStatsRepository ruleStatsRepository) {
        this.ruleRepository = ruleRepository;
        this.cachingUserStatsService = cachingUserStatsService;
        this.ruleStatsRepository = ruleStatsRepository;
    }

    /**
     * Формирует итоговый список доступных и валидных персональных предложений для пользователя.
     * Процесс защищен транзакцией для консистентного инкремента бизнес-метрик.
     *
     * @param userId уникальный идентификатор пользователя (UUID)
     * @return RecommendationResponseDto, содержащий массив подходящих продуктов
     */
    @Transactional
    public RecommendationResponseDto getRecommendations(UUID userId) {
        // Извлекаем все активные маркетинговые правила из базы данных правил
        List<RuleEntity> rules = ruleRepository.findAll();
        List<RecommendationDto> validRecommendations = new ArrayList<>();

        // Перебираем каждое правило и проверяем его применимость к текущему клиенту
        for (RuleEntity rule : rules) {
            boolean isRuleApplicable = true;

            // Если у правила задан массив условий (Queries), последовательно валидируем их
            if (rule.getQueries() != null) {
                for (QueryEntity query : rule.getQueries()) {
                    if (!checkQueryCondition(userId, query)) {
                        // Если хотя бы одно условие не выполнено, правило полностью отбраковывается
                        isRuleApplicable = false;
                        break;
                    }
                }
            }

            // Если профиль пользователя успешно прошел все валидаторы правила
            if (isRuleApplicable) {
                // Атомарно увеличиваем счетчик показов (срабатываний) данного правила в БД
                ruleStatsRepository.incrementCount(rule.getId());

                // Добавляем продукт в итоговый массив рекомендаций
                validRecommendations.add(new RecommendationDto(
                        rule.getProductId(),
                        rule.getProductName(),
                        rule.getProductText()
                ));
            }
        }

        return new RecommendationResponseDto(userId, validRecommendations);
    }

    /**
     * Внутренний интерпретатор конкретного атомарного условия (Query) правила.
     * Поддерживает инверсию результата (логическое НЕ) на основе флага negate.
     *
     * @param userId уникальный идентификатор пользователя
     * @param query  сущность аналитического запроса/условия
     * @return true, если условие выполнено (с учетом флага инверсии), иначе false
     */
    private boolean checkQueryCondition(UUID userId, QueryEntity query) {
        boolean result = false;
        List<String> args = query.getArguments();

        // Если у запроса нет аргументов, возвращаем базовый результат с учетом флага инверсии
        if (args == null || args.isEmpty()) {
            return query.isNegate() ? !result : result;
        }

        // Маршрутизация проверок по типам поддерживаемых системных запросов
        switch (query.getQuery()) {
            case "USER_OF": {
                // Проверка: является ли пользователь владельцем продукта (хотя бы 1 транзакция)
                String productType = args.get(0);
                result = checkUserOf(userId, productType);
                break;
            }
            case "ACTIVE_USER_OF": {
                // Проверка: является ли пользователь активным владельцем (от 5 транзакций)
                String productType = args.get(0);
                result = checkActiveUserOf(userId, productType);
                break;
            }
            case "TRANSACTION_SUM_COMPARE": {
                // Проверка: сравнение суммарного оборота по конкретному типу транзакции с константой
                if (args.size() >= 4) {
                    String productType = args.get(0);
                    String transactionType = args.get(1);
                    String operator = args.get(2);
                    int value = Integer.parseInt(args.get(3));
                    result = checkTransactionSumCompare(userId, productType, transactionType, operator, value);
                }
                break;
            }
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW": {
                // Проверка: преобладание притока средств (Deposit) над оттоком (Withdraw) по продукту
                if (args.size() >= 2) {
                    String productType = args.get(0);
                    String operator = args.get(1);
                    result = checkDepositWithdrawCompare(userId, productType, operator);
                }
                break;
            }
            default:
                result = false;
        }

        // Применяем логическое отрицание, если в правиле взведен флаг negate
        return query.isNegate() ? !result : result;
    }

    /**
     * Проверяет, пользовался ли клиент продуктом (наличие >= 1 транзакции).
     */
    private boolean checkUserOf(UUID userId, String productType) {
        long count = cachingUserStatsService.getTransactionCount(userId, productType);
        return count >= 1;
    }

    /**
     * Проверяет, является ли клиент активным пользователем продукта (наличие >= 5 транзакций).
     */
    private boolean checkActiveUserOf(UUID userId, String productType) {
        long count = cachingUserStatsService.getTransactionCount(userId, productType);
        return count >= 5;
    }

    /**
     * Выполняет математическое сравнение общей суммы транзакций по заданным фильтрам.
     */
    private boolean checkTransactionSumCompare(UUID userId, String productType, String transactionType, String operator, int value) {
        long sum = cachingUserStatsService.getTransactionSum(userId, productType, transactionType);
        return compareValues(sum, operator, value);
    }

    /**
     * Вычисляет разницу (дельта) между пополнениями и списаниями и сравнивает её с нулем.
     */
    private boolean checkDepositWithdrawCompare(UUID userId, String productType, String operator) {
        long delta = cachingUserStatsService.getDepositWithdrawDelta(userId, productType);
        return compareValues(delta, operator, 0);
    }

    /**
     * Универсальный утилитарный метод для обработки математических операторов сравнения.
     *
     * @param actual   фактическое вычисленное значение из кэша/БД
     * @param operator строковый оператор (>, <, =, >=, <=)
     * @param expected ожидаемое граничное значение
     * @return результат логического сравнения
     */
    private boolean compareValues(long actual, String operator, long expected) {
        if (operator == null) return false;
        switch (operator) {
            case ">":
                return actual > expected;
            case "<":
                return actual < expected;
            case "=":
                return actual == expected;
            case ">=":
                return actual >= expected;
            case "<=":
                return actual <= expected;
            default:
                return false;
        }
    }
}