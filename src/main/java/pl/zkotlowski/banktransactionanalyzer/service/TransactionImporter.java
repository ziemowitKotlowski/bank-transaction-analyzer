package pl.zkotlowski.banktransactionanalyzer.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.zkotlowski.banktransactionanalyzer.exception.InvalidTransactionRowException;
import pl.zkotlowski.banktransactionanalyzer.exception.TransactionImportFailedException;
import pl.zkotlowski.banktransactionanalyzer.model.Transaction;
import pl.zkotlowski.banktransactionanalyzer.model.document.TransactionDocument;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionImporter {

    private final TransactionService transactionService;
    private final Validator validator;

    private static final int BATCH_SIZE = 100;

    @Transactional
    public void importTransactionsFromCsv(MultipartFile multipartFile) {
        try {
            List<Transaction> transactionBatch = new ArrayList<>();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(multipartFile.getInputStream()));
            CSVParser csvParser =
                    CSVFormat.Builder.create()
                            // overwrite the csv header to ensure correct mapping
                            .setSkipHeaderRecord(true)
                            .setHeader("iban", "date", "currency", "category", "amount")
                            .get()
                            .parse(reader);

            csvParser.stream()
                    .iterator()
                    .forEachRemaining(
                            row -> {
                                var transaction = Transaction.fromCsvRow(row);
                                var violations = validator.validate(transaction);
                                if (!violations.isEmpty()) {
                                    log.warn(
                                            "Invalid transaction row detected during import: {}. Violations: {}. Rolling back entire import.",
                                            row,
                                            violations.stream()
                                                    .map(ConstraintViolation::getMessage)
                                                    .toList());

                                    throw new InvalidTransactionRowException(row);
                                }

                                transactionBatch.add(transaction);

                                if (transactionBatch.size() >= BATCH_SIZE) {
                                    transactionService.saveAllTransactions(
                                            transactionBatch.stream()
                                                    .map(TransactionDocument::fromDto)
                                                    .toList());
                                    transactionBatch.clear();
                                }
                            });

            // save any remaining transactions in the batch
            if (!transactionBatch.isEmpty()) {
                transactionService.saveAllTransactions(
                        transactionBatch.stream().map(TransactionDocument::fromDto).toList());
            }
        } catch (Exception e) {
            throw new TransactionImportFailedException(e);
        }
    }
}
