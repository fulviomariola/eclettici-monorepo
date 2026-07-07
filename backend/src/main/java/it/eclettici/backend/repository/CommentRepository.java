package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

/*
 * Questa interfaccia estende JpaRepository. Esattamente come hai fatto per i post e i commenti,
 * non richiede l'implementazione di metodi manuali.
 * Ereditando da JpaRepository, Spring Data JPA genera automaticamente a runtime tutte le query SQL per la tabella users.
 * Nel nostro CommentService, questo ci permetterà di invocare il metodo .findById(authorId) per verificare istantaneamente se
 * l'utente che sta tentando di scrivere un commento esiste nel database.
 * */

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    /**
     * Recupera tutti i commenti associati a un determinato Post (Dashboard),
     * ordinandoli dal più recente al più vecchio.
     */
    List<Comment> findByPostIdOrderByCreatedAtDesc(UUID postId);

    /**
     * Recupera tutti i commenti associati a una determinata Videolezione (Videolezioni),
     * ordinandoli dal più recente al più vecchio.
     */
    List<Comment> findByVideoIdOrderByCreatedAtDesc(Long videoId);
}