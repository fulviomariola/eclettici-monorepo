package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Subscriber;
import it.eclettici.backend.repository.SubscriberRepository;
import it.eclettici.backend.service.EmailService;
import jakarta.validation.Valid;
import it.eclettici.backend.dto.BulkEmailRequestDto;
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
}