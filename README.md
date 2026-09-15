# Recommendation System (StarBank)

**StarBank Recommendation Service** — это сервис внутри нашей банковской экосистемы. Его главная задача — помогать клиентам находить именно те финансовые продукты, которые им сейчас действительно полезны (например, удобный кредит). Сервис бережно анализирует профили пользователей и историю их транзакций, чтобы подбирать персональные и актуальные предложения.

В основе приложения лежит гармоничное сочетание двух подходов:
*   **Статические правила** — надежные базовые алгоритмы, аккуратно вписанные в код проекта (они оценивают обороты по картам).
*   **Динамические правила** — гибкие и послушные настройки, которыми менеджеры банка могут легко управлять в реальном времени через удобное API, без перезагрузки всей системы.

Чтобы клиентам было максимально комфортно общаться с банком, мы интегрировали сервис с Telegram-ботом, который выдает персональные рекомендации прямо в любимом мессенджере.

## Стек технологий проекта

*   **Core Framework:** Java 21 / Spring Boot 4.1.1 (Spring Web, Spring Data JPA)
*   **Data Architecture (Multi-DataSource):**
    *   `Transaction DB` — Файловая база данных H2 (`jdbc:h2:file:./transaction`) в режиме **Read-Only** для анализа транзакционной активности клиентов.
    *   `Rules & Stats DB` — База данных H2 в оперативной памяти (`jdbc:h2:mem:recommendation_rules`) с симуляцией синтаксиса **PostgreSQL** (`MODE=PostgreSQL`) для хранения динамических правил и статистики кликов.
*   **Database Migration:** Liquibase (схема таблиц `recommendation_rules`, `rule_queries`, `query_arguments`, `rule_stats`).
*   **Caching Layer:** Custom Local Cache Engine (`CachingUserStatsService`), оптимизирующий нагрузку на транзакционную базу данных.
*   **External Interfaces:** REST API / Telegram Bot API (Бот: `@StarBankRules2026Bot`).
*   **Build Automation:** Maven (с использованием `mvnw` wrapper).

---

## Навигация по проектной документации (Wiki)

Документация вынесена в **GitHub Wiki** репозитория:

1. [🏠 Главная страница Вики](wiki/Home) — Общий контекст проекта, архитектурные особенности и исполнители.
2. [📋 Требования и Use Case](wiki/Requirements-and-Use-Cases) — Спецификация User Stories для 3 акторов, нефункциональные требования (NFR) и UML Use Case диаграмма.
3. [📊 Матрица отслеживания требований](wiki/Traceability-Matrix) — Таблица трассируемости User Stories к вашим GitHub Issues и Pull Requests.
4. [🏗 Архитектура приложения](wiki/Architecture-and-Algorithms) — Компонентная UML-диаграмма и Activity Diagram (алгоритм совмещения статических и динамических правил).
5. [🔌 Спецификация REST API](wiki/OpenAPI-Specification) — OpenAPI (Swagger) документация для всех эндпоинтов управления.
6. [🚀 Инструкция по развертыванию](wiki/Deployment-Guide) — Перечень необходимых сервисов, команды сборки, запуска JAR и конфигурация переменных среды (`ENV`).

---

## Как запустить проект

**1. Сборка проекта:**
```bash
# Для macOS / Linux:
./mvnw clean package

# Для Windows:
.\mvnw.cmd clean package
```

**2. Запуск приложения:**
```bash
java -jar target/recommendation-0.0.1-SNAPSHOT.jar
```