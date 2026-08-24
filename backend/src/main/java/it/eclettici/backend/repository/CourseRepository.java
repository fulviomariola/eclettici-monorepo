package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByYoutubePlaylistId(String youtubePlaylistId);
}