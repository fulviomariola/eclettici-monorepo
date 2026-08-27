package it.eclettici.backend.config;

import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.Quiz;
import it.eclettici.backend.entity.QuizOption;
import it.eclettici.backend.entity.QuizQuestion;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.QuizRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuizDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;

    public QuizDataInitializer(CourseRepository courseRepository, QuizRepository quizRepository) {
        this.courseRepository = courseRepository;
        this.quizRepository = quizRepository;
    }

    @Override
    @Transactional
    public void run(@NonNull String... args) {
        List<Course> courses = courseRepository.findAll();

        for (Course course : courses) {
            // Se il corso non ha ancora un quiz associato, lo crea in automatico
            if (quizRepository.findByCourseId(course.getId()).isEmpty()) {
                Quiz quiz = new Quiz();
                quiz.setTitle("Test Finale: " + course.getTitle());
                quiz.setPassingScore(60);
                quiz.setCourse(course);

                List<QuizQuestion> questions = new ArrayList<>();

                // Domanda 1
                QuizQuestion q1 = new QuizQuestion();
                q1.setQuestionText("Qual è il concetto cardine trattato in questo corso?");
                q1.setQuiz(quiz);

                QuizOption o1 = new QuizOption();
                o1.setOptionText("Architettura pulita e best practice");
                o1.setCorrect(true);
                o1.setQuestion(q1);

                QuizOption o2 = new QuizOption();
                o2.setOptionText("Compilazione manuale del codice binario");
                o2.setCorrect(false);
                o2.setQuestion(q1);

                QuizOption o3 = new QuizOption();
                o3.setOptionText("Nessuna delle precedenti");
                o3.setCorrect(false);
                o3.setQuestion(q1);

                q1.setOptions(List.of(o1, o2, o3));
                questions.add(q1);

                // Domanda 2
                QuizQuestion q2 = new QuizQuestion();
                q2.setQuestionText("Cosa garantisce il corretto funzionamento di un'applicazione web moderna?");
                q2.setQuiz(quiz);

                QuizOption o4 = new QuizOption();
                o4.setOptionText("La separazione tra frontend, backend e database");
                o4.setCorrect(true);
                o4.setQuestion(q2);

                QuizOption o5 = new QuizOption();
                o5.setOptionText("L'utilizzo esclusivo di file statici senza API");
                o5.setCorrect(false);
                o5.setQuestion(q2);

                q2.setOptions(List.of(o4, o5));
                questions.add(q2);

                quiz.setQuestions(questions);
                quizRepository.save(quiz);

                System.out.println("✅ Quiz automatico creato per il corso: " + course.getTitle());
            }
        }
    }
}