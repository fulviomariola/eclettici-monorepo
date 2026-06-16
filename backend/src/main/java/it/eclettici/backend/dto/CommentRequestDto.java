package it.eclettici.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO in INGRESSO (Ricezione dati da Frontend)
 * Alleggerito: l'autore viene iniettato dal contesto di sicurezza del server.
 */
public class CommentRequestDto {

    @NotBlank(message = "Il contenuto del commento non può essere vuoto")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}