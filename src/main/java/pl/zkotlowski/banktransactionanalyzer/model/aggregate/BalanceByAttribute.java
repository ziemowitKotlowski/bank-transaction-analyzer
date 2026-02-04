package pl.zkotlowski.banktransactionanalyzer.model.aggregate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record BalanceByAttribute(BigDecimal expenses, BigDecimal income) {

    @JsonProperty("balance")
    public BigDecimal balance() {
        return income.add(expenses);
    }
}
