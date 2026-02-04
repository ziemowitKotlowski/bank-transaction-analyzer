package pl.zkotlowski.banktransactionanalyzer.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.commons.csv.CSVRecord;
import pl.zkotlowski.banktransactionanalyzer.model.document.TransactionDocument;

// todo: spotless configuration update to fix this class' formatting
public record Transaction(
        @NotNull UUID id,
        @NotNull
                @Pattern(
                        regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$",
                        message = "Invalid IBAN format")
                String iban,
        @NotNull @PastOrPresent(message = "Transaction date cannot be in the future")
                LocalDate date,
        @NotNull @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3-letter ISO code")
                String currency,
        @Size(min = 1, max = 100) String category,
        @NotNull
                @Digits(
                        integer = 12,
                        fraction = 2,
                        message = "Amount must have at most 12 integer digits and 2 decimal places")
                BigDecimal amount) {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Transaction fromEntity(TransactionDocument transactionDocument) {
        return new Transaction(
                transactionDocument.getId(),
                transactionDocument.getIban(),
                transactionDocument.getDate(),
                transactionDocument.getCurrency(),
                transactionDocument.getCategory(),
                transactionDocument.getAmount());
    }

    public static Transaction fromCsvRow(CSVRecord csvRow) {
        var rawIban = csvRow.get("iban");
        var rawCategory = csvRow.get("category");

        // remove all whitespaces from IBAN and convert to uppercase
        var iban = rawIban == null ? null : rawIban.replaceAll("\\s", "").toUpperCase();
        // remove all leading/trailing whitespaces from category and convert to uppercase
        var category = rawCategory == null ? null : rawCategory.strip().toUpperCase();
        return new Transaction(
                UUID.randomUUID(),
                iban,
                LocalDate.parse(csvRow.get("date"), DATE_FORMATTER),
                csvRow.get("currency"),
                category,
                new BigDecimal(csvRow.get("amount")));
    }
}
