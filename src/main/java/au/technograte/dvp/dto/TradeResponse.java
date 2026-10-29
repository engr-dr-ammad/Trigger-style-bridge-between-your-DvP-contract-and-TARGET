package au.technograte.dvp.dto;

import au.technograte.dvp.model.Trade;
import au.technograte.dvp.model.TradeStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeResponse {
    private String id;
    private String tradeReference;
    private String seller;
    private String buyer;
    private BigDecimal tradeAmount;
    private String currency;
    private TradeStatus status;
    private LocalDateTime agreedAt;
    private LocalDateTime settledAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private String bondId;
    private String bondTokenId;

    public TradeResponse(Trade trade) {
        this.id = trade.getId();
        this.tradeReference = trade.getTradeReference();
        this.seller = trade.getSeller();
        this.buyer = trade.getBuyer();
        this.tradeAmount = trade.getTradeAmount();
        this.currency = trade.getCurrency();
        this.status = trade.getStatus();
        this.agreedAt = trade.getAgreedAt();
        this.settledAt = trade.getSettledAt();
        this.failedAt = trade.getFailedAt();
        this.failureReason = trade.getFailureReason();
        if (trade.getBond() != null) {
            this.bondId = trade.getBond().getId();
            this.bondTokenId = trade.getBond().getTokenId();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTradeReference() { return tradeReference; }
    public void setTradeReference(String tradeReference) { this.tradeReference = tradeReference; }

    public String getSeller() { return seller; }
    public void setSeller(String seller) { this.seller = seller; }

    public String getBuyer() { return buyer; }
    public void setBuyer(String buyer) { this.buyer = buyer; }

    public BigDecimal getTradeAmount() { return tradeAmount; }
    public void setTradeAmount(BigDecimal tradeAmount) { this.tradeAmount = tradeAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public TradeStatus getStatus() { return status; }
    public void setStatus(TradeStatus status) { this.status = status; }

    public LocalDateTime getAgreedAt() { return agreedAt; }
    public void setAgreedAt(LocalDateTime agreedAt) { this.agreedAt = agreedAt; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getBondId() { return bondId; }
    public void setBondId(String bondId) { this.bondId = bondId; }

    public String getBondTokenId() { return bondTokenId; }
    public void setBondTokenId(String bondTokenId) { this.bondTokenId = bondTokenId; }
}
