package it.eclettici.backend.controller;

import it.eclettici.backend.dto.CourseRatingSummaryDto;
import it.eclettici.backend.dto.CourseReviewDto;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.service.CourseReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseReviewController {

    private final CourseReviewService reviewService;
    private final UserRepository userRepository;

    public CourseReviewController(CourseReviewService reviewService, UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{courseId}/reviews")
    public ResponseEntity<CourseRatingSummaryDto> getReviews(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewService.getCourseRatingSummary(courseId));
    }

    @PostMapping("/{courseId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseReviewDto> submitReview(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        UUID userId;
        if (authentication.getPrincipal() instanceof User u) {
            userId = u.getId();
        } else {
            userId = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato")).getId();
        }

        int rating = Integer.parseInt(body.get("rating").toString());
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";

        return ResponseEntity.ok(reviewService.addOrUpdateReview(courseId, userId, rating, comment));
    }
}