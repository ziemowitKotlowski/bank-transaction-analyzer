package pl.zkotlowski.banktransactionanalyzer.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${mongodb.uri}")
    private String connectionString;

    @Override
    protected String getDatabaseName() {
        return "transaction-analyzer";
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.applyConnectionString(new ConnectionString(connectionString))
                .uuidRepresentation(UuidRepresentation.STANDARD);
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }

    @Bean
    protected MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
