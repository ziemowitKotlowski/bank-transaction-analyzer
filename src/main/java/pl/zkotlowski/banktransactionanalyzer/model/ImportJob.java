package pl.zkotlowski.banktransactionanalyzer.model;

import java.time.Instant;
import java.util.UUID;
import pl.zkotlowski.banktransactionanalyzer.model.document.ImportJobDocument;

/**
 * Immutable record for thread safety representing an import job.
 *
 * @param id
 * @param startedAt
 * @param status
 * @param errorMessage
 */
public record ImportJob(
        UUID id, Instant startedAt, ImportJobDocument.Status status, String errorMessage) {
    public static ImportJob from(ImportJobDocument importJobDocument) {
        return new ImportJob(
                importJobDocument.getId(),
                importJobDocument.getStartedAt(),
                importJobDocument.getStatus(),
                importJobDocument.getErrorMessage());
    }
}
