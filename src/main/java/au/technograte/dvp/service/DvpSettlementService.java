package au.technograte.dvp.service;

import au.technograte.dvp.model.*;
import au.technograte.dvp.repository.BondRepository;
import au.technograte.dvp.repository.PaymentRepository;
import au.technograte.dvp.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * DvP Settlement Service - Orchestrates the Delivery vs Payment flow
 * Manages bond locking, payment settlement, and release/failure scenarios
 */
@Service
public class DvpSettlementService {

    private static final Logger logger = LoggerFactory.getLogger(DvpSettlementService.class);

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private BondRepository bondRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private Iso20022MessageService iso20022Service;

    @Autowired
    private BlockchainIntegrationService blockchainService;

    @Autowired
    @Lazy
    private TargetPaymentSimulatorService targetSimulator;

    @Transactional
    public Trade initiateTrade(String tradeReference, String seller, String buyer,
                              java.math.BigDecimal amount, String currency, Bond bond) {
        logger.info("Initiating trade: {} between {} and {}", tradeReference, seller, buyer);

        if (bond.getStatus() != BondStatus.AVAILABLE) {
            throw new IllegalStateException("Bond must be available for trading");
        }

        Trade trade = new Trade(tradeReference, seller, buyer, amount, currency, bond);
        trade = tradeRepository.save(trade);

        logger.info("Trade initiated: {}", trade.getId());
        return trade;
    }

    @Transactional
    public void initiateSettlement(String tradeId) {
        logger.info("Initiating settlement for trade: {}", tradeId);

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));

        if (trade.getStatus() != TradeStatus.AGREED) {
            throw new IllegalStateException("Trade must be in AGREED status");
        }

        Bond bond = trade.getBond();
        logger.info("Locking bond: {}", bond.getTokenId());
        bond.lock();
        bondRepository.save(bond);

        trade.initiate();
        tradeRepository.save(trade);

        logger.info("Settlement initiated, bond locked. Trade status: {}", trade.getStatus());
    }

    @Transactional
    public Payment createAndSendPayment(String tradeId) {
        logger.info("Creating and sending payment for trade: {}", tradeId);

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));

        if (trade.getStatus() != TradeStatus.SETTLEMENT_INITIATED) {
            throw new IllegalStateException("Trade settlement must be initiated first");
        }

        String paymentRef = "PAY-" + UUID.randomUUID();
        String messageId = "MSG-" + UUID.randomUUID();

        Payment payment = new Payment(
            paymentRef,
            messageId,
            PaymentMessageType.PACS_009,
            "DEUTDEDD",
            "DE89370400440532013000",
            "COBADETWXXX",
            "DE89370400440532013000",
            trade.getTradeAmount(),
            trade.getCurrency()
        );

        String iso20022Message = iso20022Service.generatePacs009Message(payment, trade);
        payment.setIso20022Message(iso20022Message);
        payment.setTrade(trade);

        payment = paymentRepository.save(payment);
        logger.info("Payment created: {} with ISO 20022 message", payment.getId());

        payment.send();
        paymentRepository.save(payment);
        logger.info("Payment sent to TARGET: {}", payment.getPaymentReference());

        trade.setPayment(payment);
        trade.setStatus(TradeStatus.PAYMENT_SENT);
        tradeRepository.save(trade);

        targetSimulator.simulatePaymentProcessing(payment);

        return payment;
    }

    @Transactional
    @Scheduled(fixedRateString = "${app.settlement.poll-interval-seconds:5}", timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    public void pollPaymentConfirmations() {
        logger.debug("Polling for payment confirmations");

        List<Payment> sentPayments = paymentRepository.findByStatus(PaymentStatus.SENT);

        for (Payment payment : sentPayments) {
            if (payment.getTargetSettlementId() != null) {
                confirmPayment(payment.getId());
            }
        }
    }

    @Transactional
    public void confirmPayment(String paymentId) {
        logger.info("Confirming payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SENT) {
            logger.warn("Payment {} is not in SENT status, current status: {}", paymentId, payment.getStatus());
            return;
        }

        payment.confirm();
        paymentRepository.save(payment);

        Trade trade = payment.getTrade();
        trade.setStatus(TradeStatus.PAYMENT_RECEIVED);
        tradeRepository.save(trade);

        logger.info("Payment confirmed, completing settlement");
        completeSettlement(trade.getId());
    }

    @Transactional
    public void completeSettlement(String tradeId) {
        logger.info("Completing settlement for trade: {}", tradeId);

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));

        Bond bond = trade.getBond();
        bond.settle();
        bondRepository.save(bond);

        trade.settle();
        tradeRepository.save(trade);

        blockchainService.updateBondStatus(bond.getBlockchainHash(), "SETTLED");

        logger.info("Settlement completed successfully for trade: {}", tradeId);
    }

    @Transactional
    public void failSettlement(String tradeId, String reason) {
        logger.error("Failing settlement for trade: {} - Reason: {}", tradeId, reason);

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));

        trade.fail(reason);
        tradeRepository.save(trade);

        Bond bond = trade.getBond();
        bond.release();
        bondRepository.save(bond);

        if (trade.getPayment() != null) {
            Payment payment = trade.getPayment();
            payment.fail();
            paymentRepository.save(payment);
        }

        blockchainService.updateBondStatus(bond.getBlockchainHash(), "FAILED");

        logger.error("Settlement failed and bond released back to seller");
    }

    @Transactional
    @Scheduled(fixedRateString = "${app.settlement.poll-interval-seconds:5}", timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    public void checkForTimeouts() {
        logger.debug("Checking for expired trades");

        List<Trade> activeTrades = tradeRepository.findByStatus(TradeStatus.SETTLEMENT_INITIATED);
        activeTrades.addAll(tradeRepository.findByStatus(TradeStatus.PAYMENT_SENT));

        for (Trade trade : activeTrades) {
            if (trade.isExpired()) {
                logger.warn("Trade expired: {}", trade.getTradeReference());
                failSettlement(trade.getId(), "Settlement timeout");
            }
        }
    }

    public Trade getTradeStatus(String tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));
    }

    public Bond getBondStatus(String bondId) {
        return bondRepository.findById(bondId)
                .orElseThrow(() -> new IllegalArgumentException("Bond not found: " + bondId));
    }
}
