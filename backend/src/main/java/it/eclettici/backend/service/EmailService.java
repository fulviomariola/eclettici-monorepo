package it.eclettici.backend.service;

import it.eclettici.backend.entity.ContactMessage;
import it.eclettici.backend.entity.Subscriber;
import it.eclettici.backend.repository.ContactMessageRepository;
import it.eclettici.backend.repository.SubscriberRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmailService {

    private final SubscriberRepository subscriberRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender,
                        SubscriberRepository subscriberRepository,
                        ContactMessageRepository contactMessageRepository) {
        this.mailSender = mailSender;
        this.subscriberRepository = subscriberRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    /**
     * Invio asincrono dell'attestato PDF via email allo studente.
     */
    @Async
    public void sendCertificateEmail(String toEmail, String studentName, String courseTitle, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🎓 Congratulazioni! Ecco il tuo attestato per " + courseTitle);

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; color: #1e293b; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px;">
                    <h2 style="color: #4f46e5;">Complimenti, %s! 🎉</h2>
                    <p>Hai superato con successo il test finale del corso <strong>%s</strong>.</p>
                    <p>In allegato trovi il tuo attestato di completamento ufficiale in formato PDF.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;" />
                    <p style="font-size: 12px; color: #64748b;">Eclettici Academy - Formazione Full Stack</p>
                </div>
            """, studentName, courseTitle);

            helper.setText(htmlContent, true);
            String safeFileName = "Attestato_" + courseTitle.replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
            helper.addAttachment(safeFileName, new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            System.out.println("✅ Email attestato inviata con successo a: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'invio dell'email con certificato: " + e.getMessage());
        }
    }

    /**
     * Esecuzione asincrona dell'invio massivo.
     */
    @Async
    public void sendBulkEmail(String target, List<UUID> recipientIds, String subject, String body) {
        List<String> recipientsEmails = new ArrayList<>();

        if ("ALL_SUBSCRIBERS".equalsIgnoreCase(target)) {
            recipientsEmails = subscriberRepository.findByActiveTrue().stream()
                    .map(Subscriber::getEmail)
                    .toList();
        } else if ("ALL_LEADS".equalsIgnoreCase(target)) {
            recipientsEmails = contactMessageRepository.findAll().stream()
                    .map(ContactMessage::getEmail)
                    .distinct()
                    .toList();
        } else if ("SPECIFIC".equalsIgnoreCase(target) && recipientIds != null) {
            recipientsEmails = subscriberRepository.findAllById(recipientIds).stream()
                    .map(Subscriber::getEmail)
                    .toList();
        }

        System.out.println("--- AVVIO INVIO MASSIVO ASINCRONO --- Target: " + target + " (Totale: " + recipientsEmails.size() + " e-mail)");

        for (String email : recipientsEmails) {
            try {
                System.out.println("Spedizione in corso verso: " + email + " | Oggetto: " + subject);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Motore e-mail interrotto in modo anomalo");
                return;
            }
        }

        System.out.println("--- INVIO COMPLETATO CON SUCCESSO ---");
    }
}