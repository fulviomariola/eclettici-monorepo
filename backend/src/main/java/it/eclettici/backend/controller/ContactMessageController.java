package it.eclettici.backend.controller;

import it.eclettici.backend.entity.ContactMessage;
import it.eclettici.backend.enums.ContactMessageStatus;
import it.eclettici.backend.service.ContactMessageService;
import jakarta.validation.Valid;
import it.eclettici.backend.dto.ContactRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    // Constructor Injection del servizio
    public ContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    /**
     * ENDPOINT PUBBLICO: Permette alle aziende di inviare una richiesta dal form di eclettici.it
     * CORRISPONDENZA: POST http://localhost:8082/api/contacts
     */
    @PostMapping
    public ResponseEntity<ContactMessage> receiveContactMessage(@Valid @RequestBody ContactRequestDto dto) {
        ContactMessage message = new ContactMessage();
        message.setName(dto.getName());
        message.setCompanyName(dto.getCompanyName());
        message.setEmail(dto.getEmail());
        message.setPhone(dto.getPhone());
        message.setMessage(dto.getMessage());

        ContactMessage savedMessage = contactMessageService.createMessage(message);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }

    /**
     * ENDPOINT AMMINISTRATORE: Permette di leggere tutte le richieste ricevute.
     * CORRISPONDENZA: GET http://localhost:8082/api/contacts
     */
    @GetMapping
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        List<ContactMessage> messages = contactMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    /**
     * ENDPOINT AMMINISTRATORE: Permette di aggiornare lo stato di una trattativa (es. da PENDING a IN_PROGRESS)
     * CORRISPONDENZA: PUT http://localhost:8082/api/contacts/{id}/status?status=IN_PROGRESS
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ContactMessage> updateMessageStatus(
            @PathVariable UUID id,
            @RequestParam ContactMessageStatus status) {

        ContactMessage updatedMessage = contactMessageService.updateMessageStatus(id, status);
        return ResponseEntity.ok(updatedMessage);
    }
}