package au.technograte.dvp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String paymentReference;

    @Column(nullable = false)
    private String messageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMessageType messageType;

    @Column(nullable = false)
    private String senderBic;

    @Column(nullable = false)
    private String senderAccount;

    @Column(nullable = false)
    private String receiverBic;

    @Column(nullable = false)
    private String receiverAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "trade_id")
    private Trade trade;

    @Column(columnDefinition = "LONGTEXT")
    private String iso20022Message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private LocalDateTime confirmedAt;

    private String targetSettlementId;

    public Payment() {}

    public Payment(String paymentReference, String messageId, PaymentMessageType messageType,
                   String senderBic, String senderAccount, String receiverBic, String receiverAccount,
                   BigDecimal amount, String currency) {
        this.paymentReference = paymentReference;
        this.messageId = messageId;
        this.messageType = messageType;
        this.senderBic = senderBic;
        this.senderAccount = senderAccount;
        this.receiverBic = receiverBic;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void send() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be sent");
        }
        this.status = PaymentStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != PaymentStatus.SENT) {
            throw new IllegalStateException("Only SENT payments can be confirmed");
        }
        this.status = PaymentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public PaymentMessageType getMessageType() { return messageType; }
    public void setMessageType(PaymentMessageType messageType) { this.messageType = messageType; }

    public String getSenderBic() { return senderBic; }
    public void setSenderBic(String senderBic) { this.senderBic = senderBic; }

    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }

    public String getReceiverBic() { return receiverBic; }
    public void setReceiverBic(String receiverBic) { this.receiverBic = receiverBic; }

    public String getReceiverAccount() { return receiverAccount; }
    public void setReceiverAccount(String receiverAccount) { this.receiverAccount = receiverAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public Trade getTrade() { return trade; }
    public void setTrade(Trade trade) { this.trade = trade; }

    public String getIso20022Message() { return iso20022Message; }
    public void setIso20022Message(String iso20022Message) { this.iso20022Message = iso20022Message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public String getTargetSettlementId() { return targetSettlementId; }
    public void setTargetSettlementId(String targetSettlementId) { this.targetSettlementId = targetSettlementId; }
}
