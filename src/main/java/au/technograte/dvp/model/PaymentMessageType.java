package au.technograte.dvp.model;

public enum PaymentMessageType {
    PACS_009("pacs.009", "Bank-to-bank payments ISO 20022"),
    PACS_008("pacs.008", "Customer credit transfer ISO 20022"),
    PACS_002("pacs.002", "FIToFIPaymentStatusReport ISO 20022");

    private final String code;
    private final String description;

    PaymentMessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
