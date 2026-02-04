package pl.zkotlowski.banktransactionanalyzer.exception;

import java.util.UUID;

public class ImportJobNotFoundException extends RuntimeException {
    public ImportJobNotFoundException(UUID importJobId) {
        super(String.format("Import job with id %s not found.", importJobId));
    }
}
