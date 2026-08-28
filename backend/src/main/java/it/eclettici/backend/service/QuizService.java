package it.eclettici.backend.service;

import it.eclettici.backend.dto.AdminQuizDto;
import it.eclettici.backend.dto.QuizDto;
import it.eclettici.backend.dto.QuizResultDto;
import it.eclettici.backend.dto.QuizSubmissionDto;
import it.eclettici.backend.entity.*;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.QuizAttemptRepository;
import it.eclettici.backend.repository.QuizRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public QuizService(QuizRepository quizRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       CourseRepository courseRepository,
                       UserRepository userRepository,
                       CertificateService certificateService,
                       NotificationService notificationService,
                       EmailService emailService) {
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.certificateService = certificateService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Optional<QuizDto> getQuizByCourseId(Long courseId) {
        return quizRepository.findByCourseId(courseId).map(quiz -> {
            List<QuizDto.QuestionDto> questions = quiz.getQuestions().stream().map(q -> {
                List<QuizDto.OptionDto> options = q.getOptions().stream()
                        .map(o -> new QuizDto.OptionDto(o.getId(), o.getOptionText()))
                        .collect(Collectors.toList());
                return new QuizDto.QuestionDto(q.getId(), q.getQuestionText(), options);
            }).collect(Collectors.toList());

            return new QuizDto(quiz.getId(), quiz.getTitle(), quiz.getPassingScore(), questions);
        });
    }

    @Transactional(readOnly = true)
    public List<AdminQuizDto> getAllAdminQuizzes() {
        return quizRepository.findAll().stream()
                .map(this::mapToAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AdminQuizDto> getAdminQuizByCourseId(Long courseId) {
        return quizRepository.findByCourseId(courseId).map(this::mapToAdminDto);
    }

    @Transactional
    public AdminQuizDto saveOrUpdateQuiz(Long courseId, AdminQuizDto dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Corso non trovato con ID: " + courseId));

        Quiz quiz = quizRepository.findByCourseId(courseId).orElseGet(() -> {
            Quiz q = new Quiz();
            q.setCourse(course);
            return q;
        });

        quiz.setTitle(dto.getTitle() != null && !dto.getTitle().isBlank() ? dto.getTitle() : "Test Finale: " + course.getTitle());
        quiz.setPassingScore(dto.getPassingScore() > 0 ? dto.getPassingScore() : 60);

        quiz.getQuestions().clear();

        if (dto.getQuestions() != null) {
            for (AdminQuizDto.QuestionDto qDto : dto.getQuestions()) {
                QuizQuestion question = new QuizQuestion();
                question.setQuiz(quiz);
                question.setQuestionText(qDto.getQuestionText());

                List<QuizOption> options = new ArrayList<>();
                if (qDto.getOptions() != null) {
                    for (AdminQuizDto.OptionDto oDto : qDto.getOptions()) {
                        QuizOption opt = new QuizOption();
                        opt.setQuestion(question);
                        opt.setOptionText(oDto.getOptionText());
                        opt.setCorrect(oDto.isCorrect());
                        options.add(opt);
                    }
                }
                question.setOptions(options);
                quiz.getQuestions().add(question);
            }
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToAdminDto(savedQuiz);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trovato con ID: " + quizId));
        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizResultDto submitQuiz(Long quizId, QuizSubmissionDto submission, UUID userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trovato con ID: " + quizId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));

        Map<Long, Long> userAnswers = new HashMap<>();
        if (submission != null && submission.getAnswers() != null) {
            for (QuizSubmissionDto.AnswerDto ans : submission.getAnswers()) {
                userAnswers.put(ans.getQuestionId(), ans.getSelectedOptionId());
            }
        }

        int totalQuestions = quiz.getQuestions().size();
        if (totalQuestions == 0) {
            return new QuizResultDto(100, true, quiz.getPassingScore(), "Quiz superato!");
        }

        int correctAnswers = 0;
        for (QuizQuestion q : quiz.getQuestions()) {
            Long selectedOptionId = userAnswers.get(q.getId());
            if (selectedOptionId != null) {
                boolean isCorrect = q.getOptions().stream()
                        .anyMatch(opt -> opt.getId().equals(selectedOptionId) && opt.isCorrect());
                if (isCorrect) correctAnswers++;
            }
        }

        int score = Math.round(((float) correctAnswers / totalQuestions) * 100);
        boolean passed = score >= quiz.getPassingScore();

        QuizAttempt attempt = new QuizAttempt(user, quiz, score, passed);
        quizAttemptRepository.save(attempt);

        if (passed) {
            try {
                String nome = user.getNome() != null ? user.getNome().trim() : "";
                String cognome = user.getCognome() != null ? user.getCognome().trim() : "";
                String fullName = (nome + " " + cognome).trim();
                String studentName = !fullName.isBlank() ? fullName : user.getEmail();

                byte[] pdfBytes = certificateService.generateCertificatePdf(quiz.getCourse().getId(), user.getId());
                emailService.sendCertificateEmail(user.getEmail(), studentName, quiz.getCourse().getTitle(), pdfBytes);

                // TRIGGER NOTIFICA IN-APP
                notificationService.createNotification(
                        user.getId(),
                        "Attestato Conseguito! 🎓",
                        "Complimenti, hai superato il test di '" + quiz.getCourse().getTitle() + "'. Il tuo certificato è pronto.",
                        "CERTIFICATE",
                        "/dashboard"
                );
            } catch (Exception e) {
                System.err.println("⚠️ Errore post-esame: " + e.getMessage());
            }
        }

    /*    if (passed) {
            try {
                String nome = user.getNome() != null ? user.getNome().trim() : "";
                String cognome = user.getCognome() != null ? user.getCognome().trim() : "";
                String fullName = (nome + " " + cognome).trim();
                String studentName = !fullName.isBlank() ? fullName : user.getEmail();

                byte[] pdfBytes = certificateService.generateCertificatePdf(quiz.getCourse().getId(), user.getId());
                emailService.sendCertificateEmail(user.getEmail(), studentName, quiz.getCourse().getTitle(), pdfBytes);
            } catch (Exception e) {
                System.err.println("⚠️ Impossibile inviare email attestato: " + e.getMessage());
            }
        }*/

        String message = passed
                ? "Congratulazioni! Hai superato il test di verifica."
                : "Punteggio insufficiente. Rivedi le lezioni e riprova!";

        return new QuizResultDto(score, passed, quiz.getPassingScore(), message);
    }

    private AdminQuizDto mapToAdminDto(Quiz quiz) {
        List<AdminQuizDto.QuestionDto> questions = quiz.getQuestions().stream().map(q -> {
            List<AdminQuizDto.OptionDto> options = q.getOptions().stream()
                    .map(o -> new AdminQuizDto.OptionDto(o.getId(), o.getOptionText(), o.isCorrect()))
                    .collect(Collectors.toList());
            return new AdminQuizDto.QuestionDto(q.getId(), q.getQuestionText(), options);
        }).collect(Collectors.toList());

        return new AdminQuizDto(
                quiz.getId(),
                quiz.getCourse().getId(),
                quiz.getCourse().getTitle(),
                quiz.getTitle(),
                quiz.getPassingScore(),
                questions
        );
    }
}