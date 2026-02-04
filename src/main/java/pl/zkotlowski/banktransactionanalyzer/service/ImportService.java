package pl.zkotlowski.banktransactionanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.zkotlowski.banktransactionanalyzer.model.ImportJob;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ImportJobService importJobService;
    private final ImportJobProcessor importJobProcessor;

    public ImportJob processImport(MultipartFile multipartFile) {
        var importJob = importJobService.createNewJob();
        importJobProcessor.processImportAsync(importJob, multipartFile);
        return importJob;
    }
}
