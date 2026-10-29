package au.technograte.dvp.repository;

import au.technograte.dvp.model.Bond;
import au.technograte.dvp.model.BondStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BondRepository extends JpaRepository<Bond, String> {
    Optional<Bond> findByTokenId(String tokenId);
    List<Bond> findByStatus(BondStatus status);
    List<Bond> findByHolder(String holder);
    List<Bond> findByIssuer(String issuer);
}
