package pl.zkotlowski.banktransactionanalyzer.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.zkotlowski.banktransactionanalyzer.exception.ImportJobNotFoundException;
import pl.zkotlowski.banktransactionanalyzer.model.ImportJob;
import pl.zkotlowski.banktransactionanalyzer.model.document.ImportJobDocument;
import pl.zkotlowski.banktransactionanalyzer.repository.ImportJobRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportJobService {

    private final ImportJobRepository importJobRepository;

    public ImportJob createNewJob() {
        var importJobDto = ImportJob.from(importJobRepository.save(ImportJobDocument.createNew()));
        log.info("Created new import job: {}", importJobDto);
        return importJobDto;
    }

    public ImportJob updateJobStatus(UUID importJobId, ImportJobDocument.Status status) {
        log.info("Updating status to {} of import job with id: {}", status, importJobId);
        var importJob =
                importJobRepository
                        .findById(importJobId)
                        .orElseThrow(() -> new ImportJobNotFoundException(importJobId));
        importJob.setStatus(status);

        return ImportJob.from(save(importJob));
    }

    public ImportJob failJob(UUID importJobId, String errorMessage) {
        log.info(
                "Marking import job with id: {} as FAILED with cause: {}",
                importJobId,
                errorMessage);
        var importJob =
                importJobRepository
                        .findById(importJobId)
                        .orElseThrow(() -> new ImportJobNotFoundException(importJobId));
        importJob.setStatus(ImportJobDocument.Status.FAILED);
        importJob.setErrorMessage(errorMessage);

        return ImportJob.from(save(importJob));
    }

    public ImportJob getJobDtoById(UUID importJobId) {
        return importJobRepository
                .findById(importJobId)
                .map(ImportJob::from)
                .orElseThrow(() -> new ImportJobNotFoundException(importJobId));
    }

    private ImportJobDocument save(ImportJobDocument importJobDocument) {
        return importJobRepository.save(importJobDocument);
    }
}
