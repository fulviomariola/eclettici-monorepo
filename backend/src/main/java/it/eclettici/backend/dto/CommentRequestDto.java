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
    private UUID postId;        // Valorizzato se il commento arriva dalla Dashboard
    private Long videoId;       // Valorizzato se il commento arriva dalla Videolezioni

    public UUID getPostId() { return postId; }
    public void setPostId(UUID postId) { this.postId = postId; }

    public Long getVideoId() { return videoId;}
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}