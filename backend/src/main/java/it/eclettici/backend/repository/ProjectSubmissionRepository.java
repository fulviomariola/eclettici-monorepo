package it.eclettici.backend.repository;

import it.eclettici.backend.entity.ProjectSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectSubmissionRepository extends JpaRepository<ProjectSubmission, Long> {
    List<ProjectSubmission> findByCourseIdAndUserIdOrderBySubmittedAtDesc(Long courseId, UUID userId);
    List<ProjectSubmission> findAllByOrderBySubmittedAtDesc();
    Optional<ProjectSubmission> findFirstByCourseIdAndUserIdOrderBySubmittedAtDesc(Long courseId, UUID userId);
}