package it.eclettici.backend.repository;

import it.eclettici.backend.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {
    // Eredita automaticamente metodi come save(), findById(), findAll() e deleteById()
}