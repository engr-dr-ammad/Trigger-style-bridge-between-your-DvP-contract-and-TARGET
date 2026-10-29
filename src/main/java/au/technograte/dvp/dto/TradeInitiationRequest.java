package au.technograte.dvp.dto;

import java.math.BigDecimal;

public class TradeInitiationRequest {
    private String tradeReference;
    private String seller;
    private String buyer;
    private BigDecimal amount;
    private String currency;
    private String bondTokenId;

    public TradeInitiationRequest() {}

    public TradeInitiationRequest(String tradeReference, String seller, String buyer,
                                  BigDecimal amount, String currency, String bondTokenId) {
        this.tradeReference = tradeReference;
        this.seller = seller;
        this.buyer = buyer;
        this.amount = amount;
        this.currency = currency;
        this.bondTokenId = bondTokenId;
    }

    public String getTradeReference() { return tradeReference; }
    public void setTradeReference(String tradeReference) { this.tradeReference = tradeReference; }

    public String getSeller() { return seller; }
    public void setSeller(String seller) { this.seller = seller; }

    public String getBuyer() { return buyer; }
    public void setBuyer(String buyer) { this.buyer = buyer; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getBondTokenId() { return bondTokenId; }
    public void setBondTokenId(String bondTokenId) { this.bondTokenId = bondTokenId; }
}
