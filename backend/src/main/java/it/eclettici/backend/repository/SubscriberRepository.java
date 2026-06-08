package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, UUID> {
    // Seleziona solo gli utenti che non hanno cliccato su "disiscriviti"
    List<Subscriber> findByActiveTrue();
}