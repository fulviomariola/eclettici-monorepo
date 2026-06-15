package it.eclettici.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO in INGRESSO (Ricezione dati da Postman/Frontend)
 */
public class CommentRequestDto {
    @NotNull(message = "L'ID dell'autore è obbligatorio")
    private UUID authorId;

    @NotBlank(message = "Il contenuto del commento non può essere vuoto")
    private String content;

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public String getContent() { return  content; }
    public void setContent(String content) { this.content = content; }
}
