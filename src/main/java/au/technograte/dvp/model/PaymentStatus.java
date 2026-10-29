package au.technograte.dvp.model;

public enum PaymentStatus {
    PENDING("Payment message pending"),
    SENT("Payment message sent to TARGET"),
    CONFIRMED("Payment confirmed and settled"),
    FAILED("Payment failed");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
