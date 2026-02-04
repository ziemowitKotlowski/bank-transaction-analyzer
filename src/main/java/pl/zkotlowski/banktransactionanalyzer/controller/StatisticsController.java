package pl.zkotlowski.banktransactionanalyzer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.zkotlowski.banktransactionanalyzer.model.aggregate.TransactionFilterByAttribute;
import pl.zkotlowski.banktransactionanalyzer.service.StatisticsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
@Slf4j
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/most-spent")
    public ResponseEntity<?> getMostSpentBy(
            @RequestParam("filterBy") TransactionFilterByAttribute attributeToFilterBy,
            @RequestParam("resultSize") int resultSize,
            @RequestParam("currency") String currency) {

        var stats =
                statisticsService.getMostSpentByAttribute(
                        attributeToFilterBy, resultSize, currency);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalanceBy(
            @RequestParam("filterBy") TransactionFilterByAttribute attributeToFilterBy,
            @RequestParam("value") String value,
            @RequestParam("currency") String currency) {
        var balance = statisticsService.getBalanceByAttribute(attributeToFilterBy, value, currency);
        return ResponseEntity.ok(balance);
    }
}
