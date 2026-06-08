package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Subscriber;
import it.eclettici.backend.repository.SubscriberRepository;
import it.eclettici.backend.service.EmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final SubscriberRepository subscriberRepository;

    public EmailController(EmailService emailService, SubscriberRepository subscriberRepository) {
        this.emailService = emailService;
        this.subscriberRepository = subscriberRepository;
    }

    /**
     * ENDPOINT PUBBLICO: Permette l'iscrizione alla community dal box newsletter.
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Subscriber> subscribe(@Valid @RequestBody Subscriber subscriber) {
        Subscriber saved = subscriberRepository.save(subscriber);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * ENDPOINT AMMINISTRATORE: Presidia il motore di invio dinamico.
     * Ritorna immediatamente 222 Accepted senza attendere il completamento dell'invio.
     */
    @PostMapping("/bulk-send")
    public ResponseEntity<String> sendBulkEmail(@Valid @RequestBody BulkEmailRequestDto dto) {

        // Chiamata asincrona: si avvia in un thread separato
        emailService.sendBulkEmail(dto.getTarget(), dto.getRecipientIds(), dto.getSubject(), dto.getBody());

        return ResponseEntity.accepted().body("Processo di invio massivo preso in carico ed avviato in background.");
    }

    /**
     * DTO per la ricezione flessibile dei comandi di invio
     */
    public static class BulkEmailRequestDto {
        @NotBlank(message = "Il target è obbligatorio (ALL_SUBSCRIBERS, ALL_LEADS, SPECIFIC)")
        private String target;

        private List<UUID> recipientIds; // Usato solo se target = SPECIFIC

        @NotBlank(message = "L'oggetto è obbligatorio")
        private String subject;

        @NotBlank(message = "Il testo della mail è obbligatorio")
        private String body;

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }

        public List<UUID> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<UUID> recipientIds) { this.recipientIds = recipientIds; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }
}