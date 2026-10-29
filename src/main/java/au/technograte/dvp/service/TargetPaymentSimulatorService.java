package au.technograte.dvp.service;

import au.technograte.dvp.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TARGET Payment Simulator Service - Simulates TARGET system responses
 * In production, this would communicate with actual TARGET infrastructure
 */
@Service
public class TargetPaymentSimulatorService {

    private static final Logger logger = LoggerFactory.getLogger(TargetPaymentSimulatorService.class);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    @Autowired
    private DvpSettlementService dvpSettlementService;

    @Transactional
    public void simulatePaymentProcessing(Payment payment) {
        logger.info("Simulating TARGET payment processing for: {}", payment.getPaymentReference());

        String settlementId = "SETTLEMENT-" + UUID.randomUUID();
        payment.setTargetSettlementId(settlementId);
        logger.info("Assigned TARGET settlement ID: {}", settlementId);

        executor.schedule(() -> {
            simulatePaymentConfirmation(payment);
        }, 3, TimeUnit.SECONDS);
    }

    @Transactional
    private void simulatePaymentConfirmation(Payment payment) {
        logger.info("Simulating payment confirmation for: {}", payment.getPaymentReference());

        boolean success = Math.random() > 0.1;

        if (success) {
            logger.info("Payment CONFIRMED by TARGET simulator: {}", payment.getPaymentReference());
            dvpSettlementService.confirmPayment(payment.getId());
        } else {
            logger.error("Payment REJECTED by TARGET simulator: {}", payment.getPaymentReference());
            dvpSettlementService.failSettlement(payment.getTrade().getId(), "Payment rejected by TARGET");
        }
    }

    public String generateSettlementReport(Payment payment) {
        StringBuilder report = new StringBuilder();
        report.append("=== TARGET Settlement Report ===\n");
        report.append("Settlement ID: ").append(payment.getTargetSettlementId()).append("\n");
        report.append("Payment Reference: ").append(payment.getPaymentReference()).append("\n");
        report.append("Message Type: ").append(payment.getMessageType().getCode()).append("\n");
        report.append("Amount: ").append(payment.getAmount()).append(" ").append(payment.getCurrency()).append("\n");
        report.append("Status: ").append(payment.getStatus()).append("\n");
        report.append("Sent At: ").append(payment.getSentAt()).append("\n");
        report.append("Confirmed At: ").append(payment.getConfirmedAt()).append("\n");
        return report.toString();
    }
}
