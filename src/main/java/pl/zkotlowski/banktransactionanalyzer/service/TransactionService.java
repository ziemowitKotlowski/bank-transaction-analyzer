package pl.zkotlowski.banktransactionanalyzer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.zkotlowski.banktransactionanalyzer.model.Transaction;
import pl.zkotlowski.banktransactionanalyzer.model.document.TransactionDocument;
import pl.zkotlowski.banktransactionanalyzer.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void saveAllTransactions(List<TransactionDocument> transactionDocuments) {
        transactionRepository.saveAll(transactionDocuments);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll().stream().map(Transaction::fromEntity).toList();
    }
}
