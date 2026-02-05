package pl.zkotlowski.banktransactionanalyzer.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.zkotlowski.banktransactionanalyzer.model.document.TransactionDocument;

@ExtendWith(MockitoExtension.class)
class TransactionTest {

    private Validator validator;

    @Mock private TransactionDocument transactionDocument;

    @Mock private CSVRecord csvRecord;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("fromEntity tests")
    class FromEntityTests {

        @Test
        @DisplayName("should create Transaction from TransactionDocument")
        void givenValidTransactionDocument_whenFromEntity_thenReturnTransaction() {
            // Given
            UUID expectedId = UUID.randomUUID();
            String expectedIban = "DE89370400440532013000";
            LocalDate expectedDate = LocalDate.of(2025, 1, 1);
            String expectedCurrency = "EUR";
            String expectedCategory = "GROCERIES";
            BigDecimal expectedAmount = new BigDecimal("100.50");

            when(transactionDocument.getId()).thenReturn(expectedId);
            when(transactionDocument.getIban()).thenReturn(expectedIban);
            when(transactionDocument.getDate()).thenReturn(expectedDate);
            when(transactionDocument.getCurrency()).thenReturn(expectedCurrency);
            when(transactionDocument.getCategory()).thenReturn(expectedCategory);
            when(transactionDocument.getAmount()).thenReturn(expectedAmount);

            // When
            Transaction result = Transaction.fromEntity(transactionDocument);

            // Then
            assertAll(
                    () -> assertEquals(expectedId, result.id()),
                    () -> assertEquals(expectedIban, result.iban()),
                    () -> assertEquals(expectedDate, result.date()),
                    () -> assertEquals(expectedCurrency, result.currency()),
                    () -> assertEquals(expectedCategory, result.category()),
                    () -> assertEquals(expectedAmount, result.amount()));
        }
    }

    @Nested
    @DisplayName("fromCsvRow tests")
    class FromCsvRowTests {

        @Test
        @DisplayName("should create Transaction from valid CSV record")
        void givenValidCsvRecord_whenFromCsvRow_thenReturnTransaction() {
            // Given
            when(csvRecord.get("iban")).thenReturn("DE89 3704 0044 0532 0130 00");
            when(csvRecord.get("date")).thenReturn("2024-01-15");
            when(csvRecord.get("currency")).thenReturn("EUR");
            when(csvRecord.get("category")).thenReturn("  groceries  ");
            when(csvRecord.get("amount")).thenReturn("100.50");

            // When
            Transaction result = Transaction.fromCsvRow(csvRecord);

            // Then
            assertAll(
                    () -> assertNotNull(result.id()),
                    () -> assertEquals("DE89370400440532013000", result.iban()),
                    () -> assertEquals(LocalDate.of(2024, 1, 15), result.date()),
                    () -> assertEquals("EUR", result.currency()),
                    () -> assertEquals("GROCERIES", result.category()),
                    () -> assertEquals(new BigDecimal("100.50"), result.amount()));
        }

        @Test
        @DisplayName("should convert lowercase IBAN to uppercase")
        void givenLowercaseIban_whenFromCsvRow_thenReturnUppercaseIban() {
            // Given
            when(csvRecord.get("iban")).thenReturn("de89370400440532013000");
            when(csvRecord.get("date")).thenReturn("2024-01-15");
            when(csvRecord.get("currency")).thenReturn("EUR");
            when(csvRecord.get("category")).thenReturn("FOOD");
            when(csvRecord.get("amount")).thenReturn("50.00");

            // When
            Transaction result = Transaction.fromCsvRow(csvRecord);

            // Then
            assertEquals("DE89370400440532013000", result.iban());
        }

        @Test
        @DisplayName("should handle null category")
        void givenNullCategory_whenFromCsvRow_thenReturnNullCategory() {
            // Given
            when(csvRecord.get("iban")).thenReturn("DE89370400440532013000");
            when(csvRecord.get("date")).thenReturn("2024-01-15");
            when(csvRecord.get("currency")).thenReturn("EUR");
            when(csvRecord.get("category")).thenReturn(null);
            when(csvRecord.get("amount")).thenReturn("50.00");

            // When
            Transaction result = Transaction.fromCsvRow(csvRecord);

            // Then
            assertNull(result.category());
        }

        // null IBAN handling is moved to validation tests, this verifies null handling in
        // fromCsvRow
        @Test
        @DisplayName("should handle null IBAN")
        void givenNullIban_whenFromCsvRow_thenReturnNullIban() {
            // Given
            when(csvRecord.get("iban")).thenReturn(null);
            when(csvRecord.get("date")).thenReturn("2024-01-15");
            when(csvRecord.get("currency")).thenReturn("EUR");
            when(csvRecord.get("category")).thenReturn("FOOD");
            when(csvRecord.get("amount")).thenReturn("50.00");

            // When
            Transaction result = Transaction.fromCsvRow(csvRecord);

            // Then
            assertNull(result.iban());
        }
    }

    @Nested
    @DisplayName("Validation tests")
    class ValidationTests {

        @Test
        @DisplayName("should pass validation for valid transaction")
        void givenValidTransaction_whenValidate_thenNoViolations() {
            // Given
            Transaction transaction =
                    new Transaction(
                            UUID.randomUUID(),
                            "DE89370400440532013000",
                            LocalDate.now().minusDays(1),
                            "EUR",
                            "GROCERIES",
                            new BigDecimal("100.50"));

            // When
            var violations = validator.validate(transaction);

            // Then
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("should fail validation for invalid IBAN format")
        void givenInvalidIban_whenValidate_thenHasViolation() {
            // Given
            Transaction transaction =
                    new Transaction(
                            UUID.randomUUID(),
                            "INVALID_IBAN",
                            LocalDate.now(),
                            "EUR",
                            "GROCERIES",
                            new BigDecimal("100.50"));

            // When
            var violations = validator.validate(transaction);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(
                    violations.stream()
                            .anyMatch(v -> v.getMessage().equals("Invalid IBAN format")));
        }

        @Test
        @DisplayName("should fail validation for future date")
        void givenFutureDate_whenValidate_thenHasViolation() {
            // Given
            Transaction transaction =
                    new Transaction(
                            UUID.randomUUID(),
                            "DE89370400440532013000",
                            LocalDate.now().plusDays(1),
                            "EUR",
                            "GROCERIES",
                            new BigDecimal("100.50"));

            // When
            var violations = validator.validate(transaction);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(
                    violations.stream()
                            .anyMatch(
                                    v ->
                                            v.getMessage()
                                                    .equals(
                                                            "Transaction date cannot be in the future")));
        }

        @Test
        @DisplayName("should fail validation for invalid currency format")
        void givenInvalidCurrency_whenValidate_thenHasViolation() {
            // Given
            Transaction transaction =
                    new Transaction(
                            UUID.randomUUID(),
                            "DE89370400440532013000",
                            LocalDate.now(),
                            "EURO",
                            "GROCERIES",
                            new BigDecimal("100.50"));

            // When
            var violations = validator.validate(transaction);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(
                    violations.stream()
                            .anyMatch(
                                    v ->
                                            v.getMessage()
                                                    .equals("Currency must be 3-letter ISO code")));
        }
    }
}
