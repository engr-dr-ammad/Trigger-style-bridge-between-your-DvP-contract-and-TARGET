package au.technograte.dvp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonds")
public class Bond {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String tokenId;

    @Column(nullable = false)
    private String issuer;

    @Column(nullable = false)
    private String holder;

    @Column(nullable = false)
    private BigDecimal faceValue;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BondStatus status;

    @Column(nullable = false)
    private String blockchainHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lockedAt;

    private LocalDateTime releasedAt;

    @OneToOne(mappedBy = "bond", cascade = CascadeType.ALL)
    private Trade trade;

    public Bond() {}

    public Bond(String tokenId, String issuer, String holder, BigDecimal faceValue, BigDecimal quantity) {
        this.tokenId = tokenId;
        this.issuer = issuer;
        this.holder = holder;
        this.faceValue = faceValue;
        this.quantity = quantity;
        this.status = BondStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    public void lock() {
        if (this.status != BondStatus.AVAILABLE) {
            throw new IllegalStateException("Bond can only be locked when AVAILABLE");
        }
        this.status = BondStatus.LOCKED;
        this.lockedAt = LocalDateTime.now();
    }

    public void release() {
        if (this.status != BondStatus.LOCKED) {
            throw new IllegalStateException("Bond can only be released when LOCKED");
        }
        this.status = BondStatus.AVAILABLE;
        this.releasedAt = LocalDateTime.now();
    }

    public void settle() {
        this.status = BondStatus.SETTLED;
        this.releasedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getHolder() { return holder; }
    public void setHolder(String holder) { this.holder = holder; }

    public BigDecimal getFaceValue() { return faceValue; }
    public void setFaceValue(BigDecimal faceValue) { this.faceValue = faceValue; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BondStatus getStatus() { return status; }
    public void setStatus(BondStatus status) { this.status = status; }

    public String getBlockchainHash() { return blockchainHash; }
    public void setBlockchainHash(String blockchainHash) { this.blockchainHash = blockchainHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }

    public Trade getTrade() { return trade; }
    public void setTrade(Trade trade) { this.trade = trade; }
}
