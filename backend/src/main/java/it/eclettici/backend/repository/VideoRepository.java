package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByIsPremiumFalse();

    Optional<Video> findByYoutubeId(String youtubeId);

    boolean existsByYoutubeId(String youtubeId);

    // Metodi per il catalogo corsi a 2 livelli
    List<Video> findByCourseIdOrderByIdAsc(Long courseId);

    List<Video> findByCourseIdAndIsPremiumFalseOrderByIdAsc(Long courseId);

    long countByCourseId(Long courseId);
}