package it.eclettici.backend.service;

import it.eclettici.backend.entity.ContactMessage;
import it.eclettici.backend.enums.ContactMessageStatus;
import it.eclettici.backend.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.NoSuchElementException;

@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    // Constructor Injection del repository
    public ContactMessageService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    /**
     * Riceve la richiesta dal form pubblico, imposta lo stato a PENDING e la persiste.
     */
    @Transactional
    public ContactMessage createMessage(ContactMessage message) {
        message.setStatus(ContactMessageStatus.PENDING); // Forza lo stato iniziale di default
        return contactMessageRepository.save(message);
    }

    /**
     * Recupera la lista completa dei messaggi ricevuti (per il backoffice amministratore).
     */
    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAll();
    }

    /**
     * Recupera un singolo messaggio tramite il suo UUID.
     */
    public ContactMessage getMessageById(UUID id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Messaggio di contatto non trovato con ID: " + id));
    }

    /**
     * Permette all'amministratore di far avanzare lo stato della trattativa commerciale.
     */
    @Transactional
    public ContactMessage updateMessageStatus(UUID id, ContactMessageStatus newStatus) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Impossibile aggiornare. Messaggio non trovato con ID: " + id));

        message.setStatus(newStatus);
        return contactMessageRepository.save(message);
    }
}