package it.eclettici.backend.service;

import it.eclettici.backend.dto.CourseRatingSummaryDto;
import it.eclettici.backend.dto.CourseReviewDto;
import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.CourseReview;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.CourseReviewRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseReviewService {

    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseReviewService(CourseReviewRepository reviewRepository,
                               CourseRepository courseRepository,
                               UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CourseRatingSummaryDto getCourseRatingSummary(Long courseId) {
        List<CourseReview> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId);

        double avg = reviews.stream()
                .mapToInt(CourseReview::getRating)
                .average()
                .orElse(0.0);

        // Arrotondamento a 1 cifra decimale
        double roundedAvg = Math.round(avg * 10.0) / 10.0;

        List<CourseReviewDto> dtoList = reviews.stream().map(r -> {
            String nome = r.getUser().getNome() != null ? r.getUser().getNome().trim() : "";
            String cognome = r.getUser().getCognome() != null ? r.getUser().getCognome().trim() : "";
            String fullName = (nome + " " + cognome).trim();
            String author = !fullName.isBlank() ? fullName : r.getUser().getEmail();

            return new CourseReviewDto(
                    r.getId(),
                    r.getUser().getId(),
                    author,
                    r.getRating(),
                    r.getComment(),
                    r.getCreatedAt()
            );
        }).collect(Collectors.toList());

        return new CourseRatingSummaryDto(roundedAvg, reviews.size(), dtoList);
    }

    @Transactional
    public CourseReviewDto addOrUpdateReview(Long courseId, UUID userId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La valutazione deve essere compresa tra 1 e 5 stelle.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Corso non trovato con ID: " + courseId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));

        CourseReview review = reviewRepository.findByCourseIdAndUserId(courseId, userId)
                .orElseGet(() -> new CourseReview(course, user, rating, comment));

        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        review.setCreatedAt(LocalDateTime.now());

        CourseReview saved = reviewRepository.save(review);

        String nome = user.getNome() != null ? user.getNome().trim() : "";
        String cognome = user.getCognome() != null ? user.getCognome().trim() : "";
        String fullName = (nome + " " + cognome).trim();

        return new CourseReviewDto(
                saved.getId(),
                user.getId(),
                !fullName.isBlank() ? fullName : user.getEmail(),
                saved.getRating(),
                saved.getComment(),
                saved.getCreatedAt()
        );
    }
}