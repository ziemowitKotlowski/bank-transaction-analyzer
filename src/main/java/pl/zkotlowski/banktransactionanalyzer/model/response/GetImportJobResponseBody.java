package pl.zkotlowski.banktransactionanalyzer.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import pl.zkotlowski.banktransactionanalyzer.model.ImportJob;

/**
 * GetImportJobResponseBody that returns ImportJob details and an optional message for failed
 * imports
 *
 * @param importJob
 * @param message only present for failed imports
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetImportJobResponseBody(ImportJob importJob, String message) {
    public static GetImportJobResponseBody ok(ImportJob importJob) {
        return new GetImportJobResponseBody(importJob, null);
    }

    public static GetImportJobResponseBody failed(String message) {
        return new GetImportJobResponseBody(null, message);
    }
}
