package it.eclettici.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AttachmentResponseDto {
    private UUID id;
    private String name;
    private String fileType;
    private long size;
    private LocalDateTime createdAt;

    // --- GETTER E SETTER ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}