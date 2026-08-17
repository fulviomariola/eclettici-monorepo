package it.eclettici.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class BulkEmailDto {

    @NotBlank(message = "L'oggetto della mail è obbligatorio")
    private String subject;

    @NotBlank(message = "Il contenuto della mail è obbligatorio")
    private String body;

    private String targetRole; // "ALL", "STUDENT", "STORE"

    public BulkEmailDto() {}

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
}