package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // Trova il progresso di un utente specifico per un determinato video
    Optional<Progress> findByUserIdAndVideoId(UUID userId, Long videoId);

    // Recupera tutta la lista dei progressi di un utente
    List<Progress> findByUserId(UUID userId);

    // Recupera gli youtubeId delle lezioni completate per un determinato corso e utente
    @Query("SELECT p.video.youtubeId FROM Progress p WHERE p.user.id = :userId AND p.video.course.id = :courseId AND p.isCompleted = true")
    List<String> findCompletedVideoIdsByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") Long courseId);
}