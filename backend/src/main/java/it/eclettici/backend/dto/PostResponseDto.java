package it.eclettici.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO IN USCITA (Risposta ottimizzata per il Frontend)
 */
public class PostResponseDto {
    private UUID id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private UUID authorId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id;  }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
}