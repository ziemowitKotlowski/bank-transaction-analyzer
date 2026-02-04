package pl.zkotlowski.banktransactionanalyzer.repository;

import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import pl.zkotlowski.banktransactionanalyzer.model.aggregate.BalanceByAttribute;
import pl.zkotlowski.banktransactionanalyzer.model.aggregate.TopSpentBy;
import pl.zkotlowski.banktransactionanalyzer.model.document.TransactionDocument;

@Repository
@RequiredArgsConstructor
public class TransactionAggregationRepository {

    private final MongoTemplate mongoTemplate;

    public List<TopSpentBy> aggregateTopSpentCategories(int topN, String currency) {
        var aggregation =
                Aggregation.newAggregation(
                        Aggregation.match(
                                Criteria.where("amount").lt(0).and("currency").is(currency)),
                        Aggregation.group("category").sum("amount").as("totalSpent"),
                        Aggregation.project("totalSpent").and("_id").as("category"),
                        Aggregation.sort(ASC, "totalSpent"),
                        Aggregation.limit(topN));
        return mongoTemplate
                .aggregate(aggregation, TransactionDocument.class, TopSpentBy.class)
                .getMappedResults();
    }

    public List<TopSpentBy> aggregateTopSpentYearMonth(int topN, String currency) {
        var aggregation =
                Aggregation.newAggregation(
                        Aggregation.match(
                                Criteria.where("amount").lt(0).and("currency").is(currency)),
                        Aggregation.project("amount")
                                .and("date")
                                .extractYear()
                                .as("year")
                                .and("date")
                                .extractMonth()
                                .as("month"),
                        Aggregation.group("year", "month").sum("amount").as("totalSpent"),
                        Aggregation.project("totalSpent")
                                .andExpression(
                                        "concat(toString(_id.year), '-', toString(_id.month))")
                                .as("attribute"),
                        Aggregation.sort(ASC, "totalSpent"),
                        Aggregation.limit(topN));
        return mongoTemplate
                .aggregate(aggregation, TransactionDocument.class, TopSpentBy.class)
                .getMappedResults();
    }

    public BalanceByAttribute aggregateBalanceByIban(String iban, String currency) {
        var aggregation =
                Aggregation.newAggregation(
                        Aggregation.match(
                                Criteria.where("iban").is(iban).and("currency").is(currency)),
                        Aggregation.group()
                                .sum(
                                        ConditionalOperators.Cond.when(
                                                        ComparisonOperators.Lt.valueOf("amount")
                                                                .lessThanValue(0))
                                                .then("$amount")
                                                .otherwise(0))
                                .as("expenses")
                                .sum(
                                        ConditionalOperators.when(Criteria.where("amount").gte(0))
                                                .then("$amount")
                                                .otherwise(0))
                                .as("income"));

        return mongoTemplate
                .aggregate(aggregation, TransactionDocument.class, BalanceByAttribute.class)
                .getUniqueMappedResult();
    }
}
