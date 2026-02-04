package pl.zkotlowski.banktransactionanalyzer.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.zkotlowski.banktransactionanalyzer.model.response.GetImportJobResponseBody;
import pl.zkotlowski.banktransactionanalyzer.service.ImportJobService;
import pl.zkotlowski.banktransactionanalyzer.service.ImportService;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final ImportJobService importJobService;

    @PostMapping
    public ResponseEntity<GetImportJobResponseBody> importTransactions(
            @RequestParam("file") MultipartFile file) {
        var importJob = importService.processImport(file);
        return ResponseEntity.accepted().body(GetImportJobResponseBody.ok(importJob));
    }

    @GetMapping("/{importJobId}/status")
    public ResponseEntity<GetImportJobResponseBody> getImportStatus(
            @PathVariable(name = "importJobId") String importJobIdRaw) {
        try {
            var importJobId =
                    UUID.fromString(
                            importJobIdRaw); // manually parse UUID to set explicit validation
            // behavior
            var importJob = importJobService.getJobDtoById(importJobId);
            return ResponseEntity.ok(GetImportJobResponseBody.ok(importJob));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(GetImportJobResponseBody.failed("Invalid UUID format for importJobId"));
        }
    }
}
