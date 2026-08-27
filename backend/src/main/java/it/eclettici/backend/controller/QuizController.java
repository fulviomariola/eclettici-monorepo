package it.eclettici.backend.controller;

import it.eclettici.backend.dto.QuizDto;
import it.eclettici.backend.dto.QuizResultDto;
import it.eclettici.backend.dto.QuizSubmissionDto;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;

    public QuizController(QuizService quizService, UserRepository userRepository) {
        this.quizService = quizService;
        this.userRepository = userRepository;
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<QuizDto> getQuizByCourse(@PathVariable Long courseId) {
        return quizService.getQuizByCourseId(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{quizId}/submit")
    @PreAuthorize("hasAnyRole('STUDENT', 'STORE', 'ADMIN')")
    public ResponseEntity<QuizResultDto> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizSubmissionDto submission,
            Authentication authentication) {

        UUID authenticatedUserId;
        if (authentication.getPrincipal() instanceof User principale) {
            authenticatedUserId = principale.getId();
        } else {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato con email/username: " + authentication.getName()));
            authenticatedUserId = user.getId();
        }

        QuizResultDto result = quizService.submitQuiz(quizId, submission, authenticatedUserId);
        return ResponseEntity.ok(result);
    }
}