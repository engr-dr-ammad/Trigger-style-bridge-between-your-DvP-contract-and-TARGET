package au.technograte.dvp.controller;

import au.technograte.dvp.model.Payment;
import au.technograte.dvp.model.PaymentStatus;
import au.technograte.dvp.repository.PaymentRepository;
import au.technograte.dvp.service.TargetPaymentSimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Payment Management
 */
@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TargetPaymentSimulatorService targetSimulator;

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentStatus(@PathVariable String paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<Payment> getPaymentByReference(@PathVariable String reference) {
        try {
            Payment payment = paymentRepository.findByPaymentReference(reference)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentRepository.findByStatus(status));
    }

    @GetMapping("/{paymentId}/report")
    public ResponseEntity<String> getPaymentSettlementReport(@PathVariable String paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            String report = targetSimulator.generateSettlementReport(payment);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{paymentId}/iso20022")
    public ResponseEntity<String> getIso20022Message(@PathVariable String paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            return ResponseEntity.ok(payment.getIso20022Message());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
