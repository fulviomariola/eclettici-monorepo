package it.eclettici.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

/**
 * DTO per la ricezione flessibile dei comandi di invio
 */
public class BulkEmailRequestDto {
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