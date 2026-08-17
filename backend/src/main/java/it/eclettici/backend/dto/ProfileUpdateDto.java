package it.eclettici.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class ProfileUpdateDto {

    @Email(message = "Email non valida")
    private String email;

    @Size(min = 6, message = "La nuova password deve contenere almeno 6 caratteri")
    private String newPassword;

    private String currentPassword;

    public ProfileUpdateDto() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
}