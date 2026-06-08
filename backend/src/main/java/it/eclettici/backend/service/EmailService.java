package it.eclettici.backend.service;

import it.eclettici.backend.entity.ContactMessage;
import it.eclettici.backend.entity.Subscriber;
import it.eclettici.backend.repository.ContactMessageRepository;
import it.eclettici.backend.repository.SubscriberRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmailService {

    private final SubscriberRepository subscriberRepository;
    private final ContactMessageRepository contactMessageRepository;

    public EmailService(SubscriberRepository subscriberRepository, ContactMessageRepository contactMessageRepository) {
        this.subscriberRepository = subscriberRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    /**
     * Esecuzione asincrona dell'invio. Non blocca il thread principale dell'applicazione.
     */
    @Async
    public void sendBulkEmail(String target, List<UUID> recipientIds, String subject, String body) {
        List<String> recipientsEmails = new ArrayList<>();

        // 1. Selezione dinamica del Target desiderato
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

        // 2. Ciclo di invio controllato (Evita il surriscaldamento IP e lo Spam)
        for (String email : recipientsEmails) {
            try {
                // Simulazione dell'invio di rete SMTP (In futuro qui chiamerai Brevo/SendGrid via SDK)
                System.out.println("Spedizione in corso verso: " + email + " | Oggetto: " + subject);

                // Pausa artificiale di 500ms tra le e-mail per non intasare i server riceventi
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Motore e-mail interrotto anomala");
                return;
            }
        }

        System.out.println("--- INVIO COMPLETATO CON SUCCESSO ---");
    }
}