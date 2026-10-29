package au.technograte.dvp.model;

public enum BondStatus {
    AVAILABLE("Available for trading"),
    LOCKED("Locked in settlement process"),
    SETTLED("Successfully settled"),
    FAILED("Settlement failed, released to issuer");

    private final String description;

    BondStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
