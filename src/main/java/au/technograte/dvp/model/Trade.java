package au.technograte.dvp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String tradeReference;

    @Column(nullable = false)
    private String seller;

    @Column(nullable = false)
    private String buyer;

    @Column(nullable = false)
    private BigDecimal tradeAmount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    @OneToOne(mappedBy = "trade", cascade = CascadeType.ALL)
    private Payment payment;

    @Column(nullable = false)
    private LocalDateTime agreedAt;

    private LocalDateTime settledAt;

    private LocalDateTime failedAt;

    private String failureReason;

    private LocalDateTime timeoutAt;

    public Trade() {}

    public Trade(String tradeReference, String seller, String buyer, BigDecimal tradeAmount, String currency, Bond bond) {
        this.tradeReference = tradeReference;
        this.seller = seller;
        this.buyer = buyer;
        this.tradeAmount = tradeAmount;
        this.currency = currency;
        this.bond = bond;
        this.status = TradeStatus.AGREED;
        this.agreedAt = LocalDateTime.now();
        this.timeoutAt = LocalDateTime.now().plusSeconds(300);
    }

    public void initiate() {
        if (this.status != TradeStatus.AGREED) {
            throw new IllegalStateException("Trade must be AGREED to initiate");
        }
        this.status = TradeStatus.SETTLEMENT_INITIATED;
    }

    public void settle() {
        if (this.status != TradeStatus.SETTLEMENT_INITIATED) {
            throw new IllegalStateException("Trade must have settlement initiated");
        }
        this.status = TradeStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = TradeStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.timeoutAt);
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

    public Bond getBond() { return bond; }
    public void setBond(Bond bond) { this.bond = bond; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public LocalDateTime getAgreedAt() { return agreedAt; }
    public void setAgreedAt(LocalDateTime agreedAt) { this.agreedAt = agreedAt; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getTimeoutAt() { return timeoutAt; }
    public void setTimeoutAt(LocalDateTime timeoutAt) { this.timeoutAt = timeoutAt; }
}
