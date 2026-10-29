package au.technograte.dvp.repository;

import au.technograte.dvp.model.Payment;
import au.technograte.dvp.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByPaymentReference(String paymentReference);
    Optional<Payment> findByMessageId(String messageId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByTradeId(String tradeId);
}
