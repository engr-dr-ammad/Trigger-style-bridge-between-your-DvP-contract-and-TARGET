package au.technograte.dvp.controller;

import au.technograte.dvp.dto.BondCreationRequest;
import au.technograte.dvp.model.Bond;
import au.technograte.dvp.model.BondStatus;
import au.technograte.dvp.repository.BondRepository;
import au.technograte.dvp.service.BlockchainIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for Bond Management
 */
@RestController
@RequestMapping("/bonds")
@CrossOrigin(origins = "*")
public class BondController {

    @Autowired
    private BondRepository bondRepository;

    @Autowired
    private BlockchainIntegrationService blockchainService;

    @PostMapping("/create")
    public ResponseEntity<?> createBond(@RequestBody BondCreationRequest request) {
        try {
            if (request.getIssuer() == null || request.getHolder() == null) {
                return ResponseEntity.badRequest().body("Issuer and Holder are required");
            }

            String tokenId = blockchainService.createBondToken(
                    request.getIssuer(),
                    request.getHolder(),
                    request.getFaceValue().toPlainString(),
                    request.getQuantity().toPlainString()
            );

            Bond bond = new Bond(
                    tokenId,
                    request.getIssuer(),
                    request.getHolder(),
                    request.getFaceValue(),
                    request.getQuantity()
            );
            bond.setBlockchainHash("0x" + UUID.randomUUID().toString().replace("-", "").substring(0, 40));
            bond = bondRepository.save(bond);

            return ResponseEntity.status(HttpStatus.CREATED).body(bond);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating bond: " + e.getMessage());
        }
    }

    @GetMapping("/{bondId}")
    public ResponseEntity<Bond> getBondStatus(@PathVariable String bondId) {
        try {
            Bond bond = bondRepository.findById(bondId)
                    .orElseThrow(() -> new IllegalArgumentException("Bond not found"));
            return ResponseEntity.ok(bond);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/token/{tokenId}")
    public ResponseEntity<Bond> getBondByToken(@PathVariable String tokenId) {
        try {
            Bond bond = bondRepository.findByTokenId(tokenId)
                    .orElseThrow(() -> new IllegalArgumentException("Bond not found"));
            return ResponseEntity.ok(bond);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Bond>> getAllBonds() {
        return ResponseEntity.ok(bondRepository.findAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Bond>> getBondsByStatus(@PathVariable BondStatus status) {
        return ResponseEntity.ok(bondRepository.findByStatus(status));
    }

    @GetMapping("/holder/{holder}")
    public ResponseEntity<List<Bond>> getBondsByHolder(@PathVariable String holder) {
        return ResponseEntity.ok(bondRepository.findByHolder(holder));
    }
}
