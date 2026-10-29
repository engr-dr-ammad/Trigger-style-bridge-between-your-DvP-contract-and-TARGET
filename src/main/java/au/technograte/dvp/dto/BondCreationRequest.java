package au.technograte.dvp.dto;

import java.math.BigDecimal;

public class BondCreationRequest {
    private String issuer;
    private String holder;
    private BigDecimal faceValue;
    private BigDecimal quantity;

    public BondCreationRequest() {}

    public BondCreationRequest(String issuer, String holder, BigDecimal faceValue, BigDecimal quantity) {
        this.issuer = issuer;
        this.holder = holder;
        this.faceValue = faceValue;
        this.quantity = quantity;
    }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getHolder() { return holder; }
    public void setHolder(String holder) { this.holder = holder; }

    public BigDecimal getFaceValue() { return faceValue; }
    public void setFaceValue(BigDecimal faceValue) { this.faceValue = faceValue; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
