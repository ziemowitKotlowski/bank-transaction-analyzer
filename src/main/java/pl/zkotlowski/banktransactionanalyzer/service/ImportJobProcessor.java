package pl.zkotlowski.banktransactionanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.zkotlowski.banktransactionanalyzer.model.ImportJob;
import pl.zkotlowski.banktransactionanalyzer.model.document.ImportJobDocument;

@Slf4j
@RequiredArgsConstructor
@Component
public class ImportJobProcessor {

    private final ImportJobService importJobService;
    private final TransactionImporter transactionImporter;

    @Async
    public void processImportAsync(ImportJob importJob, MultipartFile multipartFile) {
        var importJobId = importJob.id();

        log.info("Starting async import processing for job id: {}", importJobId);
        try {
            transactionImporter.importTransactionsFromCsv(multipartFile);
            importJobService.updateJobStatus(importJob.id(), ImportJobDocument.Status.COMPLETED);
        } catch (Exception e) {
            log.error("Error processing import job id: {}", importJobId, e);
            String errorMessage =
                    (e.getCause() != null && e.getCause().getMessage() != null)
                            ? e.getCause().getMessage()
                            : e.getMessage();
            importJobService.failJob(
                    importJobId,
                    errorMessage); // todo: consider better error message extraction to avoid
            // potential NPE if cause is null
        }
    }
}
