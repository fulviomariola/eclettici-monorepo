package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    // Estrae i soli video gratuiti filtrando per isPremium = false
    List<Video> findByIsPremiumFalse();

    // Evita duplicati controllando se l'ID YouTube è già registrato
    Optional<Video> findByYoutubeId(String youtubeId);

    boolean existsByYoutubeId(String youtubeId);
}