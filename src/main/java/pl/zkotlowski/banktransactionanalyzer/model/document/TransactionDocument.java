package pl.zkotlowski.banktransactionanalyzer.model.document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pl.zkotlowski.banktransactionanalyzer.model.Transaction;

/** Mongo document representing one transaction */
@Document(collection = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(setterPrefix = "with")
public class TransactionDocument {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Id private UUID id;

    @Field(name = "iban")
    private String iban;

    @Field(name = "date")
    @Indexed(sparse = true) // ignore null values for indexing
    private LocalDate date;

    @Field(name = "category")
    @Indexed(sparse = false) // explicitly include null values for indexing
    private String category;

    @Field(name = "currency")
    private String currency;

    @Field(name = "amount")
    private BigDecimal amount;

    public static TransactionDocument fromDto(Transaction transaction) {
        return TransactionDocument.builder()
                .withId(transaction.id())
                .withIban(transaction.iban())
                .withDate(transaction.date())
                .withCategory(transaction.category())
                .withCurrency(transaction.currency())
                .withAmount(transaction.amount())
                .build();
    }
}
