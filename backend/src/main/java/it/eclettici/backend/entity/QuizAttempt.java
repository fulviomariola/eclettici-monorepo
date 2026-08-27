package it.eclettici.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private boolean passed;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();

    public QuizAttempt() {}

    public QuizAttempt(User user, Quiz quiz, int score, boolean passed) {
        this.user = user;
        this.quiz = quiz;
        this.score = score;
        this.passed = passed;
        this.attemptedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Quiz getQuiz() { return quiz; }
    public int getScore() { return score; }
    public boolean isPassed() { return passed; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
}