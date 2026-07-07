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
    private UUID authorId;
    private String authorName;
    private String authorRole;
    private UUID postId;
    private Long videoId;


    // Getter e Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getPostId() { return postId; }
    public void setPostId(UUID postId) { this.postId = postId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
}