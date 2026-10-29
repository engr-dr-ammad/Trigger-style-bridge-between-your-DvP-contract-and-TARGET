package au.technograte.dvp.model;

public enum TradeStatus {
    AGREED("Trade agreed on blockchain"),
    SETTLEMENT_INITIATED("Settlement process initiated"),
    PAYMENT_SENT("Payment message sent via ISO 20022"),
    PAYMENT_RECEIVED("Payment received confirmation"),
    SETTLED("Trade settled and completed"),
    FAILED("Trade failed and bond returned to issuer"),
    TIMEOUT("Trade timed out");

    private final String description;

    TradeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
