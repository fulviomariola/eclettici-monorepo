package it.eclettici.backend.repository;

import it.eclettici.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/*
* Questa interfaccia estende JpaRepository. Esattamente come hai fatto per i post e i commenti,
* non richiede l'implementazione di metodi manuali. Ereditando da JpaRepository, Spring Data
* JPA genera automaticamente a runtime tutte le query SQL per la tabella users. Nel nostro CommentService,
* questo ci permetterà di invocare il metodo .findById(authorId) per verificare istantaneamente se
* l'utente che sta tentando di scrivere un commento esiste nel database.
* */

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}