package it.eclettici.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import it.eclettici.backend.dto.CertificateVerifyDto;
import it.eclettici.backend.dto.UserCertificateDto;
import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.QuizAttempt;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.QuizAttemptRepository;
import it.eclettici.backend.repository.QuizRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class CertificateService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendBaseUrl;

    public CertificateService(QuizRepository quizRepository,
                              QuizAttemptRepository quizAttemptRepository,
                              CourseRepository courseRepository,
                              UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateCertificatePdf(Long courseId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Corso non trovato"));

        var quiz = quizRepository.findByCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Quiz non associato a questo corso"));

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdAndQuizIdOrderByAttemptedAtDesc(userId, quiz.getId());
        QuizAttempt passedAttempt = attempts.stream()
                .filter(QuizAttempt::isPassed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Test non ancora superato per questo corso"));

        return createPdf(user, course, passedAttempt);
    }

    @Transactional(readOnly = true)
    public CertificateVerifyDto verifyCertificate(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Certificato non trovato con ID: " + attemptId));

        if (!attempt.isPassed()) {
            throw new RuntimeException("Il tentativo specificato non risulta superato");
        }

        User user = attempt.getUser();
        String nome = user.getNome() != null ? user.getNome().trim() : "";
        String cognome = user.getCognome() != null ? user.getCognome().trim() : "";
        String studentFullName = !(nome + " " + cognome).isBlank() ? (nome + " " + cognome).trim() : user.getEmail();

        return new CertificateVerifyDto(
                attempt.getId(),
                studentFullName,
                attempt.getQuiz().getCourse().getTitle(),
                attempt.getScore(),
                attempt.isPassed(),
                attempt.getAttemptedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<UserCertificateDto> getUserCertificates(UUID userId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findAll().stream()
                .filter(a -> a.getUser().getId().equals(userId) && a.isPassed())
                .sorted(Comparator.comparing(QuizAttempt::getAttemptedAt).reversed())
                .toList();

        Map<Long, UserCertificateDto> uniqueCertificates = new LinkedHashMap<>();
        for (QuizAttempt a : attempts) {
            Long courseId = a.getQuiz().getCourse().getId();
            uniqueCertificates.putIfAbsent(courseId, new UserCertificateDto(
                    a.getId(),
                    courseId,
                    a.getQuiz().getCourse().getTitle(),
                    a.getScore(),
                    a.getAttemptedAt()
            ));
        }

        return new ArrayList<>(uniqueCertificates.values());
    }

    private byte[] createPdf(User user, Course course, QuizAttempt attempt) {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new java.awt.Color(30, 41, 59));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 13, new java.awt.Color(100, 116, 139));
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new java.awt.Color(79, 70, 229));
            Font courseFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new java.awt.Color(15, 23, 42));
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new java.awt.Color(100, 116, 139));

            Paragraph title = new Paragraph("CERTIFICATO DI COMPLETAMENTO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            document.add(title);

            Paragraph subtitle = new Paragraph("Si attesta che", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(15);
            document.add(subtitle);

            String nome = user.getNome() != null ? user.getNome().trim() : "";
            String cognome = user.getCognome() != null ? user.getCognome().trim() : "";
            String fullName = (nome + " " + cognome).trim();
            String studentFullName = !fullName.isBlank() ? fullName : user.getEmail();

            Paragraph studentName = new Paragraph(studentFullName.toUpperCase(), nameFont);
            studentName.setAlignment(Element.ALIGN_CENTER);
            studentName.setSpacingBefore(8);
            document.add(studentName);

            Paragraph text = new Paragraph("ha completato con successo il percorso formativo e superato il test di verifica finale di:", subtitleFont);
            text.setAlignment(Element.ALIGN_CENTER);
            text.setSpacingBefore(12);
            document.add(text);

            Paragraph courseTitle = new Paragraph("\"" + course.getTitle() + "\"", courseFont);
            courseTitle.setAlignment(Element.ALIGN_CENTER);
            courseTitle.setSpacingBefore(8);
            document.add(courseTitle);

            LineSeparator separator = new LineSeparator();
            separator.setLineColor(new java.awt.Color(226, 232, 240));
            separator.setPercentage(70);
            Paragraph line = new Paragraph();
            line.add(separator);
            line.setSpacingBefore(20);
            line.setSpacingAfter(15);
            document.add(line);

            // Generazione Immagine QR Code per la verifica
            String verifyUrl = frontendBaseUrl + "/verifica-certificato/" + attempt.getId();
            byte[] qrBytes = generateQrCodeImage(verifyUrl, 95, 95);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setSpacingBefore(5);
            document.add(qrImage);

            String dateStr = attempt.getAttemptedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Paragraph meta = new Paragraph(
                    "Data: " + dateStr + "  |  Punteggio: " + attempt.getScore() + "%  |  ID: #" + attempt.getId() + "  |  Scansiona per verificare",
                    metaFont
            );
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingBefore(8);
            document.add(meta);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la creazione del PDF", e);
        }

        return out.toByteArray();
    }

    private byte[] generateQrCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}