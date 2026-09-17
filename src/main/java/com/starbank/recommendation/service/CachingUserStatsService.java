package com.starbank.recommendation.service;

import com.starbank.recommendation.cache.key.TransactionSumKey;
import com.starbank.recommendation.cache.key.UserProductKey;
import com.starbank.recommendation.repository.readonly.UserStatsRepository;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Сервис кэширования транзакционной статистики пользователей.
 * Реализует паттерн Cache-Aside (In-Memory кэширование) на базе локальных кэш-провайдеров.
 * Минимизирует количество «тяжелых» агрегационных запросов к read-only базе данных транзакций,
 * обеспечивая выполнение требований по времени ответа системы (Latency).
 */
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

    /**
     * Возвращает общее количество транзакций пользователя по конкретному типу банковского продукта.
     * Метод сначала обращается к in-memory кэшу, и только при промахе выполняет чтение из репозитория.
     *
     * @param userId      уникальный идентификатор пользователя (UUID)
     * @param productType тип банковского продукта (например, "DEBIT", "CREDIT")
     * @return количество совершенных транзакций
     */
    public long getTransactionCount(UUID userId, String productType) {
        // Формируем составной неизменяемый ключ для поиска в хэш-карте кэша
        UserProductKey key = new UserProductKey(userId, productType);
        return userTransactionCountCache.get(key, k ->
                userStatsRepository.countTransactionsByProduct(k.userId(), k.productType())
        );
    }

    /**
     * Возвращает суммарный денежный оборот по транзакциям конкретного типа для выбранного продукта.
     * Значение извлекается из кэша памяти, оптимизируя операции суммирования.
     *
     * @param userId          уникальный идентификатор пользователя (UUID)
     * @param productType     тип финансового продукта ("DEBIT", "SAVING")
     * @param transactionType направленность операции ("DEPOSIT", "WITHDRAWAL")
     * @return суммарный оборот по выбранным фильтрам
     */
    public long getTransactionSum(UUID userId, String productType, String transactionType) {
        // Формируем уникальный ключ, включающий тип транзакции
        TransactionSumKey key = new TransactionSumKey(userId, productType, transactionType);
        return transactionSumCache.get(key, k ->
                userStatsRepository.sumTransactionsAmount(k.userId(), k.productType(), k.transactionType())
        );
    }

    /**
     * Вычисляет чистую финансовую дельту (разницу между всеми пополнениями и списаниями) по продукту.
     * Результат кэшируется для мгновенного повторного использования в цепочках правил.
     *
     * @param userId      уникальный идентификатор пользователя (UUID)
     * @param productType тип финансового продукта для анализа оборотов
     * @return разность сумм пополнений и списаний (может быть отрицательной)
     */
    public long getDepositWithdrawDelta(UUID userId, String productType) {
        UserProductKey key = new UserProductKey(userId, productType);
        return depositWithdrawDeltaCache.get(key, k -> {
            // Извлекаем общую сумму всех приходных операций (DEPOSIT)
            long deposits = userStatsRepository.sumTransactionsAmount(k.userId(), k.productType(), "DEPOSIT");

            // Извлекаем общую сумму всех расходных операций (WITHDRAW)
            long withdraws = userStatsRepository.sumTransactionsAmount(k.userId(), k.productType(), "WITHDRAW");

            // Вычисляем приток средств клиента
            return deposits - withdraws;
        });
    }

    /**
     * Выполняет полную инвалидацию (очистку) всех внутренних структур кэша приложения.
     * Метод является потокобезопасным и вызывается внешними инфраструктурными триггерами через API.
     */
    public void clearAllCaches() {
        userTransactionCountCache.invalidateAll();
        transactionSumCache.invalidateAll();
        depositWithdrawDeltaCache.invalidateAll();
    }
}