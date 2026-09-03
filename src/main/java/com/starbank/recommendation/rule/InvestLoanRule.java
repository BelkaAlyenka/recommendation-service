package com.starbank.recommendation.rule;

import com.starbank.recommendation.dto.RecommendationDto;
import com.starbank.recommendation.repository.RecommendationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class InvestLoanRule implements RecommendationRuleSet {

    private final RecommendationRepository repository;

    public InvestLoanRule(RecommendationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        boolean hasDebit = repository.hasProductType(userId, "DEBIT");
        boolean hasInvest = repository.hasProductType(userId, "INVEST");
        double sumSaving = repository.getTotalSumByType(userId, "SAVING");

        if (hasDebit && !hasInvest && sumSaving > 1000) {
            return Optional.of(new RecommendationDto(
                    UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
                    "Invest 500",
                    "Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка! Воспользуйтесь налоговыми льготами и начните инвестировать с умом. Пополните счет до конца года и получите выгоду в виде вычета на взнос в следующем налоговом периоде. Не упустите возможность разнообразить свой портфель, снизить риски и следить за актуальными рыночными тенденциями. Откройте ИИС сегодня и станьте ближе к финансовой независимости!"
            ));
        }

        return Optional.empty();
    }
}
