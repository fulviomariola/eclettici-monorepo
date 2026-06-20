package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

// Ovvero che è collegato direttamente al DB

/*
 * Questa interfaccia estende JpaRepository. Esattamente come hai fatto per i post e i commenti,
 * non richiede l'implementazione di metodi manuali. Ereditando da JpaRepository, Spring Data
 * JPA genera automaticamente a runtime tutte le query SQL per la tabella users. Nel nostro CommentService,
 * questo ci permetterà di invocare il metodo .findById(authorId) per verificare istantaneamente se
 * l'utente che sta tentando di scrivere un commento esiste nel database.
 * */


@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    /**
     * Recupera tutti i post ordinati dal più recente, caricando contemporaneamente
     * i commenti associati (evita il Lazy Loading) ed eliminando i duplicati a monte.
     */
    /*
    @EntityGraph(attributePaths = {"comments"})
    @Query("SELECT DISTINCT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllWithComments();
    */
}