package it.eclettici.backend.repository;

import it.eclettici.backend.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    Optional<CourseReview> findByCourseIdAndUserId(Long courseId, UUID userId);
    long countByCourseId(Long courseId);
}