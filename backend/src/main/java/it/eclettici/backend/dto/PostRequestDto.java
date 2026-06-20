package it.eclettici.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class PostRequestDto {

    @NotBlank(message = "Il titolo del post è obbligatorio")
    private String title;

    @NotBlank(message = "Il contenuto del post è obbligatorio")
    private String content;

    // Scommentiamo il campo per ricevere l'ID in modo stateless
    @NotNull(message = "L'ID dell'autore è obbligatorio")
    private UUID authorId;

    private Boolean isPrivate; // Assicurati che ci sia anche questo se gestisci i post privati

    // Metodi Getter e Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }
}