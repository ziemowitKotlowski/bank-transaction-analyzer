package pl.zkotlowski.banktransactionanalyzer.exception;

public class TransactionImportFailedException extends RuntimeException {
    public TransactionImportFailedException(Throwable cause) {
        super("Transaction import failed", cause);
    }
}
