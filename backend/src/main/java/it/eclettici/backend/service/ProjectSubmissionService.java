package it.eclettici.backend.service;

import it.eclettici.backend.dto.ProjectSubmissionDto;
import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.ProjectSubmission;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.ProjectSubmissionRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectSubmissionService {

    private final ProjectSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ProjectSubmissionService(ProjectSubmissionRepository submissionRepository,
                                    CourseRepository courseRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService) {
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ProjectSubmissionDto submitProject(Long courseId, UUID userId, String repoUrl, String notes) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Corso non trovato con ID: " + courseId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));

        ProjectSubmission submission = new ProjectSubmission(course, user, repoUrl.trim(), notes != null ? notes.trim() : "");
        ProjectSubmission saved = submissionRepository.save(submission);

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectSubmissionDto> getLatestSubmission(Long courseId, UUID userId) {
        return submissionRepository.findFirstByCourseIdAndUserIdOrderBySubmittedAtDesc(courseId, userId)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<ProjectSubmissionDto> getAllSubmissions() {
        return submissionRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectSubmissionDto reviewSubmission(Long submissionId, String status, String feedback) {
        ProjectSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Consegna non trovata con ID: " + submissionId));

        submission.setStatus(status);
        submission.setAdminFeedback(feedback != null ? feedback.trim() : "");
        submission.setReviewedAt(LocalDateTime.now());

        ProjectSubmission saved = submissionRepository.save(submission);

        // Notifica in-app allo studente sull'esito della revisione
        String notificationTitle = "APPROVED".equalsIgnoreCase(status)
                ? "Progetto Approvato! 🚀"
                : "Revisione Progetto Richiesta 📝";

        String notificationMsg = "APPROVED".equalsIgnoreCase(status)
                ? "Il tuo progetto per il corso '" + submission.getCourse().getTitle() + "' è stato approvato dal docente."
                : "Il docente ha revisionato il tuo progetto per '" + submission.getCourse().getTitle() + "'. Consulta il feedback.";

        notificationService.createNotification(
                submission.getUser().getId(),
                notificationTitle,
                notificationMsg,
                "COURSE",
                "/videolezioni/" + submission.getCourse().getId()
        );

        return mapToDto(saved);
    }

    private ProjectSubmissionDto mapToDto(ProjectSubmission s) {
        String nome = s.getUser().getNome() != null ? s.getUser().getNome().trim() : "";
        String cognome = s.getUser().getCognome() != null ? s.getUser().getCognome().trim() : "";
        String fullName = (nome + " " + cognome).trim();

        return new ProjectSubmissionDto(
                s.getId(),
                s.getCourse().getId(),
                s.getCourse().getTitle(),
                s.getUser().getId(),
                !fullName.isBlank() ? fullName : s.getUser().getEmail(),
                s.getUser().getEmail(),
                s.getRepoUrl(),
                s.getNotes(),
                s.getStatus(),
                s.getAdminFeedback(),
                s.getSubmittedAt(),
                s.getReviewedAt()
        );
    }
}