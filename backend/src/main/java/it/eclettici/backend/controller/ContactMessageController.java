package it.eclettici.backend.controller;

import it.eclettici.backend.entity.ContactMessage;
import it.eclettici.backend.enums.ContactMessageStatus;
import it.eclettici.backend.service.ContactMessageService;
import jakarta.validation.Valid;
import it.eclettici.backend.dto.ContactRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    public ContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    /**
     * ENDPOINT PUBBLICO: Form di contatto landing page
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
     * ENDPOINT RISERVATO (STORE / ADMIN): Lettura di tutte le richieste
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        List<ContactMessage> messages = contactMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Aggiornamento dello stato trattativa
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<ContactMessage> updateMessageStatus(
            @PathVariable UUID id,
            @RequestParam ContactMessageStatus status) {

        ContactMessage updatedMessage = contactMessageService.updateMessageStatus(id, status);
        return ResponseEntity.ok(updatedMessage);
    }
}