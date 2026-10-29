package au.technograte.dvp.controller;

import au.technograte.dvp.dto.TradeInitiationRequest;
import au.technograte.dvp.dto.TradeResponse;
import au.technograte.dvp.model.Bond;
import au.technograte.dvp.model.BondStatus;
import au.technograte.dvp.model.Trade;
import au.technograte.dvp.repository.BondRepository;
import au.technograte.dvp.service.DvpSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for Trade Management
 */
@RestController
@RequestMapping("/trades")
@CrossOrigin(origins = "*")
public class TradeController {

    @Autowired
    private DvpSettlementService dvpSettlementService;

    @Autowired
    private BondRepository bondRepository;

    @PostMapping("/initiate")
    public ResponseEntity<TradeResponse> initiateTrade(@RequestBody TradeInitiationRequest request) {
        try {
            Bond bond = bondRepository.findByTokenId(request.getBondTokenId())
                    .orElseThrow(() -> new IllegalArgumentException("Bond not found: " + request.getBondTokenId()));

            Trade trade = dvpSettlementService.initiateTrade(
                    request.getTradeReference(),
                    request.getSeller(),
                    request.getBuyer(),
                    request.getAmount(),
                    request.getCurrency(),
                    bond
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(new TradeResponse(trade));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{tradeId}/settle")
    public ResponseEntity<TradeResponse> initiateSettlement(@PathVariable String tradeId) {
        try {
            dvpSettlementService.initiateSettlement(tradeId);
            Trade trade = dvpSettlementService.getTradeStatus(tradeId);
            return ResponseEntity.ok(new TradeResponse(trade));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{tradeId}/payment")
    public ResponseEntity<?> createPayment(@PathVariable String tradeId) {
        try {
            var payment = dvpSettlementService.createAndSendPayment(tradeId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating payment: " + e.getMessage());
        }
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<TradeResponse> getTradeStatus(@PathVariable String tradeId) {
        try {
            Trade trade = dvpSettlementService.getTradeStatus(tradeId);
            return ResponseEntity.ok(new TradeResponse(trade));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Trade>> getAllTrades() {
        // Implementation for listing all trades
        return ResponseEntity.ok(List.of());
    }
}
