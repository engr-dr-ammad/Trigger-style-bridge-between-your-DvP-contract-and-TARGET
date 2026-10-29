# DvP Bridge - TARGET Payment System

A comprehensive Java Spring Boot service that implements a **Delivery vs Payment (DvP)** bridge between tokenized bond contracts and the TARGET2 payment system using ISO 20022 standards.

**Developed by:** Engr Dr. Ammad Aslam  
**Development:** With AI-assisted tooling (Claude AI)  
**Build Date:** 2026-10-29  
**Email:** ammad1@iscte-iul.pt  
**Organization:** ISCTE-IUL / Technograte  

---

## Overview

This service orchestrates secure settlement of trades involving tokenized bonds by ensuring atomic delivery and payment:

1. **Bond Token Management**: Creates and manages tokenized bonds on blockchain
2. **Trade Initiation**: Records agreements between buyers and sellers
3. **DvP Settlement**: Locks bonds during payment processing
4. **ISO 20022 Messaging**: Generates pacs.009 payment messages for TARGET
5. **Settlement Confirmation**: Confirms payment and releases bonds
6. **Failure Handling**: Returns bonds to seller if payment fails
7. **Timeout Management**: Automatically releases locked bonds on timeout

---

## Architecture

### Core Components

```
┌─────────────────────────────────────────────────┐
│         REST API Controllers                    │
│  (Trade, Bond, Payment Management)              │
└──────────────┬──────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────┐
│         DvP Settlement Service                  │
│  (Orchestrates entire settlement flow)          │
└──────┬──────────────────┬──────────────┬────────┘
       │                  │              │
┌──────▼─────────┐ ┌──────▼──────┐ ┌────▼──────────┐
│Blockchain      │ │ISO 20022    │ │TARGET Payment │
│Integration     │ │Message      │ │Simulator      │
│                │ │Service      │ │                │
└────────────────┘ └─────────────┘ └────────────────┘
       │                  │              │
└──────┴──────────────────┴──────────────┘
       │
┌──────▼──────────────────────────────┐
│  Data Layer (JPA Repositories)       │
│  Bonds, Trades, Payments             │
└─────────────────────────────────────┘
```

### Entity Model

**Bond** (Tokenized Asset)
- Status: AVAILABLE → LOCKED → SETTLED
- Holds issuer, holder, face value, quantity
- Linked to blockchain via transaction hash

**Trade** (Agreement)
- Status: AGREED → SETTLEMENT_INITIATED → PAYMENT_SENT → PAYMENT_RECEIVED → SETTLED
- Links buyer, seller, amount, bond, payment
- Includes timeout mechanism (default 300s)

**Payment** (ISO 20022 Message)
- Status: PENDING → SENT → CONFIRMED
- Contains pacs.009 XML message
- Tracks TARGET settlement ID

---

## Key Features

### 1. **Atomic Delivery vs Payment**
- Bond is locked when payment is initiated
- Bond is only released after confirmed payment
- Automatic bond release on failure/timeout

### 2. **ISO 20022 Compliance**
- Generates standard pacs.009 bank-to-bank payment messages
- Properly formatted XML with all required fields
- Compatible with TARGET2 infrastructure

### 3. **Real-time Settlement**
- Scheduled polling for payment confirmations (5-second intervals)
- Automatic timeout detection and handling
- Transaction atomicity via Spring @Transactional

### 4. **Blockchain Integration**
- Simulates Ethereum smart contract interactions
- Creates immutable bond records on-chain
- Updates bond status on settlement completion

### 5. **Comprehensive Logging**
- DEBUG level for settlement flow details
- WARN/ERROR for failures and timeouts
- Audit trail for compliance

---

## API Endpoints

### Bond Management

```bash
# Create a new bond
POST /api/bonds/create
{
  "issuer": "ISSUER001",
  "holder": "HOLDER001",
  "faceValue": 1000.00,
  "quantity": 100
}

# Get bond details
GET /api/bonds/{bondId}

# Find by token ID
GET /api/bonds/token/{tokenId}

# List all bonds
GET /api/bonds

# Filter by status
GET /api/bonds/status/AVAILABLE
GET /api/bonds/status/LOCKED
GET /api/bonds/status/SETTLED
```

### Trade Management

```bash
# Initiate a trade
POST /api/trades/initiate
{
  "tradeReference": "TRADE-20261029-001",
  "seller": "SELLER001",
  "buyer": "BUYER001",
  "amount": 100000.00,
  "currency": "EUR",
  "bondTokenId": "BOND-xxx-xxx-xxx"
}

# Begin settlement process
POST /api/trades/{tradeId}/settle

# Create and send payment
POST /api/trades/{tradeId}/payment

# Check trade status
GET /api/trades/{tradeId}
```

### Payment Management

```bash
# Check payment status
GET /api/payments/{paymentId}

# Find by reference
GET /api/payments/reference/{paymentReference}

# List all payments
GET /api/payments

# Filter by status
GET /api/payments/status/SENT
GET /api/payments/status/CONFIRMED

# Get settlement report
GET /api/payments/{paymentId}/report

# View ISO 20022 message
GET /api/payments/{paymentId}/iso20022
```

---

## Settlement Flow Diagram

```
1. INITIATE TRADE
   Seller and Buyer agree
   Trade: AGREED
   Bond: AVAILABLE
   
2. LOCK BOND
   POST /api/trades/{id}/settle
   Trade: SETTLEMENT_INITIATED
   Bond: LOCKED
   
3. CREATE PAYMENT
   POST /api/trades/{id}/payment
   - Generate ISO 20022 pacs.009
   - Send to TARGET
   Trade: PAYMENT_SENT
   Payment: SENT
   
4. CONFIRM PAYMENT
   - TARGET confirms settlement (simulated: 3 seconds)
   - Payment: CONFIRMED
   Trade: PAYMENT_RECEIVED
   
5. COMPLETE SETTLEMENT
   - Release bond to buyer
   Trade: SETTLED
   Bond: SETTLED
   Payment: CONFIRMED
   
ON TIMEOUT (after 300s):
   Trade: TIMEOUT → FAILED
   Bond: LOCKED → AVAILABLE (returned to seller)
   
ON PAYMENT FAILURE:
   Trade: FAILED
   Bond: LOCKED → AVAILABLE (returned to seller)
   Payment: FAILED
```

---

## Configuration

Edit `application.yml` to customize:

```yaml
app:
  blockchain:
    rpc-url: http://localhost:8545
    contract-address: 0x...
    network-id: 1337
  
  target:
    bic: DEUTDEDD
    account-number: DE89370400440532013000
    settlement-timeout-seconds: 300
  
  iso20022:
    sender-name: DvP Bridge Service
    sender-identification: TECHNOGRATE001
  
  settlement:
    poll-interval-seconds: 5
    max-retries: 3
```

---

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.8+
- Ethereum node (optional, for production)

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

Or:
```bash
java -jar target/dvp-bridge-target-system-1.0.0.jar
```

### Access
- **API**: http://localhost:8080/api
- **H2 Console**: http://localhost:8080/h2-console
- **Swagger UI**: http://localhost:8080/swagger-ui.html (if enabled)

---

## Example Usage

### Complete Settlement Flow

```bash
# 1. Create a bond
curl -X POST http://localhost:8080/api/bonds/create \
  -H "Content-Type: application/json" \
  -d '{
    "issuer": "ISSUER001",
    "holder": "SELLER001",
    "faceValue": 1000,
    "quantity": 100
  }'

# Response: {"id":"abc123","tokenId":"BOND-xxx","status":"AVAILABLE",...}
BOND_ID="abc123"
BOND_TOKEN="BOND-xxx"

# 2. Initiate trade
curl -X POST http://localhost:8080/api/trades/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "tradeReference": "TRADE-001",
    "seller": "SELLER001",
    "buyer": "BUYER001",
    "amount": 100000,
    "currency": "EUR",
    "bondTokenId": "'$BOND_TOKEN'"
  }'

# Response: {"id":"trade123","status":"AGREED",...}
TRADE_ID="trade123"

# 3. Initiate settlement (locks bond)
curl -X POST http://localhost:8080/api/trades/$TRADE_ID/settle

# 4. Create and send payment (generates ISO 20022)
curl -X POST http://localhost:8080/api/trades/$TRADE_ID/payment

# 5. Monitor settlement (poll until SETTLED)
watch -n 1 'curl -s http://localhost:8080/api/trades/'$TRADE_ID | jq .status'

# 6. View the ISO 20022 message
curl http://localhost:8080/api/payments/\
  -H "Accept: application/json" | jq '.iso20022Message'
```

---

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Database**: H2 (dev), PostgreSQL (production)
- **ORM**: Hibernate JPA
- **Message Format**: ISO 20022 (XML)
- **Blockchain**: Web3j for Ethereum integration
- **Build**: Maven
- **Java Version**: 17 LTS

---

## Security Considerations

- Input validation on all API endpoints
- Transaction-level atomicity for settlement
- Immutable blockchain records
- Audit logging for all state changes
- Role-based access control (future enhancement)
- TLS/SSL for data transmission (production)
- Database encryption for sensitive fields (production)

---

## Testing

Unit and integration tests are included:

```bash
mvn test
```

Key test scenarios:
- Bond creation and status transitions
- Trade initialization and settlement
- Payment message generation
- Timeout and failure handling
- Concurrent trade processing

---

## Troubleshooting

### Payment Stuck in SENT Status
- Check TARGET simulator is running
- Verify `app.settlement.poll-interval-seconds` is not too high
- Check logs for timeout exceptions

### Bond Not Found
- Verify bond token ID is correct
- Ensure bond was created before trade initiation
- Check H2 console: `SELECT * FROM bonds`

### ISO 20022 Message Invalid
- Verify all required fields in payment object
- Check IBANs and BIC codes format
- Review generated XML in `/api/payments/{id}/iso20022`

---

## Future Enhancements

- [ ] Real TARGET2 API integration
- [ ] Cryptographic message signing
- [ ] Multi-currency settlement
- [ ] Automated clearing house (ACH) support
- [ ] Real-time gross settlement (RTGS)
- [ ] Webhook notifications
- [ ] Dashboard UI
- [ ] Performance monitoring
- [ ] Disaster recovery procedures

---

## License

Proprietary - Technograte

---

## Support & Contact

**Lead Developer**: Engr Dr. Ammad Aslam  
**Email**: ammad1@iscte-iul.pt  
**Development Methodology**: AI-Assisted Development with Claude AI  
**Organization**: ISCTE-IUL (Instituto Universitário de Lisboa)  
**Secondary Email**: ammad@technograte.com.au

---

## Attribution

**Developed by:** Engr Dr. Ammad Aslam with AI-assisted tooling (Claude AI)

This project demonstrates modern software engineering practices combining:
- Expert domain knowledge (DvP, ISO 20022, TARGET payments)
- AI-assisted code generation and optimization
- Clean architecture and best practices
- Production-ready Java Spring Boot implementation

---

**Version**: 1.0.0  
**Build Date**: 2026-10-29  
**Last Updated**: 2026-10-29  
**Repository**: https://github.com/engr-dr-ammad/Trigger-style-bridge-between-your-DvP-contract-and-TARGET
