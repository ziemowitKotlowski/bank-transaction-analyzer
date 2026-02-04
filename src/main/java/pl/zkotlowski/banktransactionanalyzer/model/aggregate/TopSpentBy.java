package pl.zkotlowski.banktransactionanalyzer.model.aggregate;

import java.math.BigDecimal;

public record TopSpentBy(BigDecimal totalSpent, String attribute) {}
