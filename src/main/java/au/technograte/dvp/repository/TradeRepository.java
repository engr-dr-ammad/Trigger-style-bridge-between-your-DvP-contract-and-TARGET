package au.technograte.dvp.repository;

import au.technograte.dvp.model.Trade;
import au.technograte.dvp.model.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, String> {
    Optional<Trade> findByTradeReference(String tradeReference);
    List<Trade> findByStatus(TradeStatus status);
    List<Trade> findBySeller(String seller);
    List<Trade> findByBuyer(String buyer);
}
