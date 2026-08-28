package it.eclettici.backend.controller;

import it.eclettici.backend.dto.ProjectReviewRequestDto;
import it.eclettici.backend.dto.ProjectSubmissionDto;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.service.ProjectSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectSubmissionController {

    private final ProjectSubmissionService submissionService;
    private final UserRepository userRepository;

    public ProjectSubmissionController(ProjectSubmissionService submissionService, UserRepository userRepository) {
        this.submissionService = submissionService;
        this.userRepository = userRepository;
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof User u) {
            return u.getId();
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utente non trovato")).getId();
    }

    // --- ENDPOINTS STUDENTE ---

    @PostMapping("/course/{courseId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectSubmissionDto> submitProject(
            @PathVariable Long courseId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String repoUrl = body.get("repoUrl");
        String notes = body.get("notes");

        if (repoUrl == null || repoUrl.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        UUID userId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(submissionService.submitProject(courseId, userId, repoUrl, notes));
    }

    @GetMapping("/course/{courseId}/my-submission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectSubmissionDto> getMySubmission(
            @PathVariable Long courseId,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);
        return submissionService.getLatestSubmission(courseId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // --- ENDPOINTS AMMINISTRATORE ---

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjectSubmissionDto>> getAllSubmissions() {
        return ResponseEntity.ok(submissionService.getAllSubmissions());
    }

    @PatchMapping("/admin/{submissionId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectSubmissionDto> reviewSubmission(
            @PathVariable Long submissionId,
            @RequestBody ProjectReviewRequestDto reviewDto) {

        return ResponseEntity.ok(submissionService.reviewSubmission(
                submissionId,
                reviewDto.getStatus(),
                reviewDto.getFeedback()
        ));
    }
}