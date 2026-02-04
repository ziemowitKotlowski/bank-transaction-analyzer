package pl.zkotlowski.banktransactionanalyzer.exception;

public class FeatureNotImplementedException extends RuntimeException {
    public FeatureNotImplementedException() {
        super("Feature not implemented");
    }
}
