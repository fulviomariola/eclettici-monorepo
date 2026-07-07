package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // Trova il progresso di un utente specifico per un determinato video
    Optional<Progress> findByUserIdAndVideoId(UUID userId, Long videoId);

    // Recupera tutta la lista dei progressi di un utente (utile per la Dashboard)
    List<Progress> findByUserId(UUID userId);
}