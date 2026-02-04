package pl.zkotlowski.banktransactionanalyzer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.zkotlowski.banktransactionanalyzer.exception.FeatureNotImplementedException;
import pl.zkotlowski.banktransactionanalyzer.model.aggregate.BalanceByAttribute;
import pl.zkotlowski.banktransactionanalyzer.model.aggregate.TopSpentBy;
import pl.zkotlowski.banktransactionanalyzer.model.aggregate.TransactionFilterByAttribute;
import pl.zkotlowski.banktransactionanalyzer.repository.TransactionAggregationRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final TransactionAggregationRepository transactionAggregationRepository;

    public List<TopSpentBy> getMostSpentByAttribute(
            TransactionFilterByAttribute attribute, int topN, String currency) {
        switch (attribute) {
            case IBAN, CURRENCY, DATE -> throw new FeatureNotImplementedException();
            case CATEGORY -> {
                return getTopCategoriesSpendAggregation(topN, currency);
            }
            case YEAR_MONTH -> {
                return getTopYearMonthSpendAggregation(topN, currency);
            }
        }
        return List.of();
    }

    public BalanceByAttribute getBalanceByAttribute(
            TransactionFilterByAttribute attribute, String value, String currency) {
        switch (attribute) {
            case IBAN -> {
                return getBalanceByIban(value, currency);
            }
            case CATEGORY, YEAR_MONTH, DATE, CURRENCY -> throw new FeatureNotImplementedException();
            default -> throw new IllegalArgumentException(
                    String.format("Invalid attribute to get balance by: %s", attribute));
        }
    }

    private List<TopSpentBy> getTopCategoriesSpendAggregation(int topN, String currency) {
        return transactionAggregationRepository.aggregateTopSpentCategories(topN, currency);
    }

    private List<TopSpentBy> getTopYearMonthSpendAggregation(int topN, String currency) {
        return transactionAggregationRepository.aggregateTopSpentYearMonth(topN, currency);
    }

    private BalanceByAttribute getBalanceByIban(String iban, String currency) {
        iban =
                iban.replaceAll("\\s+", "")
                        .toUpperCase(); // clean up input to match the one in database
        return transactionAggregationRepository.aggregateBalanceByIban(iban, currency);
    }
}
