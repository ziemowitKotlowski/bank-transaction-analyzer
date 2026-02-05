package pl.zkotlowski.banktransactionanalyzer.service;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import pl.zkotlowski.banktransactionanalyzer.exception.TransactionImportFailedException;
import pl.zkotlowski.banktransactionanalyzer.model.Transaction;

@ExtendWith(MockitoExtension.class)
class TransactionImporterTest {

    @Mock private TransactionService transactionService;

    @Mock private Validator validator;

    @Mock private MultipartFile multipartFile;

    @Mock private ConstraintViolation<Transaction> constraintViolation;

    @InjectMocks private TransactionImporter transactionImporter;

    private static final String CSV_HEADER = "iban,date,currency,category,amount\n";

    @Nested
    @DisplayName("importTransactionsFromCsv tests")
    class ImportTransactionsFromCsvTests {

        @Test
        @DisplayName("should successfully import valid CSV with single transaction")
        void givenValidCsvWithSingleTransaction_whenImport_thenSaveTransaction()
                throws IOException {
            // Given
            String csvContent =
                    CSV_HEADER + "DE89370400440532013000,2024-01-15,EUR,GROCERIES,100.50\n";
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, times(1)).saveAllTransactions(anyList());
        }

        @Test
        @DisplayName(
                "should successfully import valid CSV with multiple transactions below batch size")
        void givenValidCsvWithMultipleTransactions_whenImport_thenSaveAllTransactions()
                throws IOException {
            // Given
            StringBuilder csvContent = new StringBuilder(CSV_HEADER);
            csvContent.append(
                    "DE89370400440532013000,2024-01-15,EUR,GROCERIES,100.50\n".repeat(50));
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(
                                    csvContent.toString().getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, times(1))
                    .saveAllTransactions(argThat(list -> list.size() == 50));
        }

        @Test
        @DisplayName("should process transactions in batches when exceeding batch size")
        void givenCsvExceedingBatchSize_whenImport_thenProcessInBatches() throws IOException {
            // Given
            StringBuilder csvContent = new StringBuilder(CSV_HEADER);
            csvContent.append(
                    "DE89370400440532013000,2024-01-15,EUR,GROCERIES,100.50\n".repeat(250));
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(
                                    csvContent.toString().getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            // 250 transactions = 2 full batches of 100 + 1 remaining batch of 50
            verify(transactionService, times(3)).saveAllTransactions(anyList());
        }

        @Test
        @DisplayName("should process exact batch size correctly")
        void givenCsvWithExactBatchSize_whenImport_thenProcessSingleBatch() throws IOException {
            // Given
            StringBuilder csvContent = new StringBuilder(CSV_HEADER);
            csvContent.append(
                    "DE89370400440532013000,2024-01-15,EUR,GROCERIES,100.50\n".repeat(100));
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(
                                    csvContent.toString().getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, times(1))
                    .saveAllTransactions(argThat(list -> list.size() == 100));
        }

        @Test
        @DisplayName("should handle empty CSV file with only header")
        void givenEmptyCsvWithOnlyHeader_whenImport_thenNoTransactionsSaved() throws IOException {
            // Given
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(CSV_HEADER.getBytes(StandardCharsets.UTF_8)));

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, never()).saveAllTransactions(anyList());
        }

        @Test
        @DisplayName("should handle CSV with whitespaces in IBAN")
        void givenCsvWithWhitespacesInIban_whenImport_thenProcessSuccessfully() throws IOException {
            // Given
            String csvContent =
                    CSV_HEADER + "DE89 3704 0044 0532 0130 00,2024-01-15,EUR,GROCERIES,100.50\n";
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, times(1)).saveAllTransactions(anyList());
        }

        @Test
        @DisplayName("should handle CSV with category containing whitespaces")
        void givenCsvWithCategoryWhitespaces_whenImport_thenProcessSuccessfully()
                throws IOException {
            // Given
            String csvContent =
                    CSV_HEADER + "DE89370400440532013000,2024-01-15,EUR,  groceries  ,100.50\n";
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, times(1)).saveAllTransactions(anyList());
        }
    }

    @Nested
    @DisplayName("Validation failure tests")
    class ValidationFailureTests {

        @Test
        @DisplayName("should throw TransactionImportFailedException when validation fails")
        void givenInvalidTransaction_whenImport_thenThrowException() throws IOException {
            // Given
            String csvContent = CSV_HEADER + "INVALID_IBAN,2024-01-15,EUR,GROCERIES,100.50\n";
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
            when(constraintViolation.getMessage()).thenReturn("Invalid IBAN format");
            when(validator.validate(any(Transaction.class)))
                    .thenReturn(Set.of(constraintViolation));

            // When & Then
            assertThrows(
                    TransactionImportFailedException.class,
                    () -> transactionImporter.importTransactionsFromCsv(multipartFile));
        }

        @Test
        @DisplayName("should not save any transactions when first row is invalid")
        void givenFirstRowInvalid_whenImport_thenNoTransactionsSaved() throws IOException {
            // Given
            String csvContent =
                    CSV_HEADER
                            + "INVALID_IBAN,2024-01-15,EUR,GROCERIES,100.50\n"
                            + "DE89370400440532013000,2024-01-15,EUR,GROCERIES,100.50\n";
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
            when(constraintViolation.getMessage()).thenReturn("Invalid IBAN format");
            when(validator.validate(any(Transaction.class)))
                    .thenReturn(Set.of(constraintViolation));

            // When
            try {
                transactionImporter.importTransactionsFromCsv(multipartFile);
            } catch (TransactionImportFailedException e) {
                // Expected
            }

            // Then
            verify(transactionService, never()).saveAllTransactions(anyList());
        }

        @Test
        @DisplayName("should rollback when validation fails in the middle of import")
        void givenInvalidRowInMiddle_whenImport_thenThrowException() throws IOException {
            // Given
            StringBuilder csvContent = new StringBuilder(CSV_HEADER);
            csvContent.append(
                    "DE89370400440532013000,2024-01-15,EUR,GROCERIES,100.50\n".repeat(50));
            csvContent.append("INVALID_IBAN,2024-01-15,EUR,GROCERIES,100.50\n");

            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(
                                    csvContent.toString().getBytes(StandardCharsets.UTF_8)));
            when(constraintViolation.getMessage()).thenReturn("Invalid IBAN format");
            when(validator.validate(any(Transaction.class)))
                    .thenAnswer(
                            invocation -> {
                                Transaction transaction = invocation.getArgument(0);
                                if (transaction.iban() == null
                                        || !transaction
                                                .iban()
                                                .matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$")) {
                                    return Set.of(constraintViolation);
                                }
                                return Collections.emptySet();
                            });

            // When & Then
            assertThrows(
                    TransactionImportFailedException.class,
                    () -> transactionImporter.importTransactionsFromCsv(multipartFile));
        }
    }

    @Nested
    @DisplayName("IOException handling tests")
    class IOExceptionHandlingTests {

        @Test
        @DisplayName("should throw TransactionImportFailedException when IOException occurs")
        void givenIOException_whenImport_thenThrowTransactionImportFailedException()
                throws IOException {
            // Given
            when(multipartFile.getInputStream()).thenThrow(new IOException("File read error"));

            // When & Then
            TransactionImportFailedException exception =
                    assertThrows(
                            TransactionImportFailedException.class,
                            () -> transactionImporter.importTransactionsFromCsv(multipartFile));

            assertInstanceOf(IOException.class, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Edge cases tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle negative amounts")
        void givenNegativeAmount_whenImport_thenProcessSuccessfully() throws IOException {
            // Given
            String csvContent =
                    CSV_HEADER + "DE89370400440532013000,2024-01-15,EUR,GROCERIES,-100.50\n";
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, times(1)).saveAllTransactions(anyList());
        }

        @Test
        @DisplayName("should handle decimal amounts with two decimal places")
        void givenDecimalAmount_whenImport_thenProcessSuccessfully() throws IOException {
            // Given
            String csvContent =
                    CSV_HEADER + "DE89370400440532013000,2024-01-15,EUR,GROCERIES,1234567890.99\n";
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, times(1)).saveAllTransactions(anyList());
        }

        @Test
        @DisplayName("should handle multiple batches plus remainder correctly")
        void givenMultipleBatchesPlusRemainder_whenImport_thenAllBatchesSaved() throws IOException {
            // Given - 350 transactions = 3 full batches of 100 + 1 remaining batch of 50
            StringBuilder csvContent = new StringBuilder(CSV_HEADER);
            csvContent.append(
                    "DE89370400440532013000,2024-01-15,EUR,GROCERIES,100.50\n".repeat(350));
            when(multipartFile.getInputStream())
                    .thenReturn(
                            new ByteArrayInputStream(
                                    csvContent.toString().getBytes(StandardCharsets.UTF_8)));
            when(validator.validate(any(Transaction.class))).thenReturn(Collections.emptySet());

            // When
            transactionImporter.importTransactionsFromCsv(multipartFile);

            // Then
            verify(transactionService, times(4)).saveAllTransactions(anyList());
        }
    }
}
