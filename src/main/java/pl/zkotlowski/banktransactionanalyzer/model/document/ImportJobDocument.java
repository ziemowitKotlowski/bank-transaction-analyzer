package pl.zkotlowski.banktransactionanalyzer.model.document;

import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/** Mongo document representing one transaction import job */
@Document(collection = "import_jobs")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class ImportJobDocument {

    @Id private UUID id;

    @Field(name = "started_at")
    private Instant startedAt;

    // store enum as string in DB for readability and future proofing (adding/modifying statuses)
    @Field(name = "status", targetType = FieldType.STRING)
    private Status status;

    @Field(name = "error_message")
    private String errorMessage;

    public static ImportJobDocument createNew() {
        return new ImportJobDocument(
                UUID.randomUUID(), Instant.now(), Status.IMPORT_IN_PROGRESS, null);
    }

    public enum Status {
        IMPORT_IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
