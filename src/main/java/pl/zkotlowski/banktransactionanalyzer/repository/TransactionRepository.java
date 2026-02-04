package pl.zkotlowski.banktransactionanalyzer.repository;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import pl.zkotlowski.banktransactionanalyzer.model.document.TransactionDocument;

public interface TransactionRepository extends MongoRepository<TransactionDocument, UUID> {}
