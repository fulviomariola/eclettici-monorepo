package it.eclettici.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
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
    private Boolean isPrivate;
    private List<CommentResponseDto> comments;
    private Boolean isPremium;
    private String userRole;
    private String authorEmail;


    public Boolean getPremium() {
        return isPremium;
    }
    public void setPremium(Boolean premium) {
        isPremium = premium;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }
    public void setIsPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

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

    public List<CommentResponseDto> getComments() { return comments; }
    public void setComments(List<CommentResponseDto> comments) { this.comments = comments; }

    public String getUserRole() {return userRole;}
    public void setUserRole(String userRole) {this.userRole = userRole;}

    public String getAuthorEmail() {return authorEmail;}
    public void setAuthorEmail(String authorEmail) {this.authorEmail = authorEmail;}
}