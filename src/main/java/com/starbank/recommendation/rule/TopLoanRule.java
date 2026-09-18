package com.starbank.recommendation.rule;

import com.starbank.recommendation.dto.RecommendationDto;
import com.starbank.recommendation.repository.RecommendationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TopLoanRule implements RecommendationRuleSet {

    private final RecommendationRepository repository;

    public TopLoanRule(RecommendationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        boolean hasDebit = repository.hasProductType(userId, "DEBIT");

        double sumDebitDeposit = repository.getSumByTransactionAndProduct(userId, "DEBIT", "DEPOSIT");
        double sumSavingDeposit = repository.getSumByTransactionAndProduct(userId, "SAVING", "DEPOSIT");
        double sumDebitWithdrawal = repository.getSumByTransactionAndProduct(userId, "DEBIT", "WITHDRAWAL");

        boolean isDebitDepositGreaterThanWithdrawal = sumDebitDeposit > sumDebitWithdrawal;

        if (hasDebit && (sumDebitDeposit >= 50000 || sumSavingDeposit >= 50000) && isDebitDepositGreaterThanWithdrawal) {
            return Optional.of(new RecommendationDto(
                    UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925"),
                    "Top Saving",
                    "Откройте свою собственную «Копилку» с нашим банком! «Копилка» — это уникальный банковский инструмент, который поможет вам легко и удобно накапливать деньги на важные цели. Больше никаких забытых чеков и потерянных квитанций — всё под контролем!\n" +
                            "\n" +
                            "Преимущества «Копилки»:\n" +
                            "\n" +
                            "Накопление средств на конкретные цели. Установите лимит и срок накопления, и банк будет автоматически переводить определенную сумму на ваш счет.\n" +
                            "\n" +
                            "Прозрачность и контроль. Отслеживайте свои доходы и расходы, контролируйте процесс накопления и корректируйте стратегию при необходимости.\n" +
                            "\n" +
                            "Безопасность и надежность. Ваши средства находятся под защитой банка, а доступ к ним возможен только через мобильное приложение или интернет-банкинг.\n" +
                            "\n" +
                            "Начните использовать «Копилку» уже сегодня и станьте ближе к своим финансовым целям!"
            ));
        }
        return Optional.empty();
    }
}
