package it.eclettici.backend.repository;

import it.eclettici.backend.entity.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, UUID> {

    // Spring Data JPA genera automaticamente la query: SELECT * FROM service_offers WHERE active = true
    List<ServiceOffer> findByActiveTrue();
}