package it.eclettici.backend.service;

import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.entity.UserPurchase;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.UserPurchaseRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PurchaseService {

    private final UserPurchaseRepository userPurchaseRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public PurchaseService(UserPurchaseRepository userPurchaseRepository,
                           UserRepository userRepository,
                           CourseRepository courseRepository) {
        this.userPurchaseRepository = userPurchaseRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Verifica se un utente ha acquistato un determinato corso
     */
    public boolean isCoursePurchased(UUID userId, Long courseId) {
        return userPurchaseRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * Registra l'acquisto di un corso per l'utente loggato
     */
    @Transactional
    public void purchaseCourse(String userEmail, Long courseId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con email: " + userEmail));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Corso non trovato con ID: " + courseId));

        if (userPurchaseRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            return; // Acquisto già effettuato in precedenza
        }

        UserPurchase purchase = new UserPurchase(user, course);
        userPurchaseRepository.save(purchase);
    }
}