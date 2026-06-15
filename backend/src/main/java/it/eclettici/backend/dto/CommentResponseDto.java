package it.eclettici.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO in USCITA (Risposta pulita per Postman/Frontend)
 * Risolve il bug della ricorsione circolare (3800 righe).
 */
public class CommentResponseDto {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private UUID postId;
    private UUID authorId;

    // Getter e Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getPostId() { return postId; }
    public void setPostId(UUID postId) { this.postId = postId; }

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
}