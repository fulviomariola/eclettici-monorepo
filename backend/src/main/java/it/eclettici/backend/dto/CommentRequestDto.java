package it.eclettici.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * DTO in INGRESSO (Ricezione dati da Frontend)
 * Alleggerito: l'autore viene iniettato dal contesto di sicurezza del server.
 */
public class CommentRequestDto {

    @NotBlank(message = "Il contenuto del commento non può essere vuoto")
    private String content;
    private UUID authorId;

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}