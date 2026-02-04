package pl.zkotlowski.banktransactionanalyzer.exception;

import org.apache.commons.csv.CSVRecord;

public class InvalidTransactionRowException extends RuntimeException {
    public InvalidTransactionRowException(CSVRecord csvRecord) {
        super(
                String.format(
                        "Invalid transaction row: {%s}, rolling back all transactions imported.",
                        csvRecord));
    }
}
