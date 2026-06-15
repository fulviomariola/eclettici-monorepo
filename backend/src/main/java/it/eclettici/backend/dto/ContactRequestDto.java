package it.eclettici.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO statico interno per la validazione del payload JSON in ingresso.
 * Isola l'entità del database e protegge l'applicazione da inserimenti malevoli.
 */
public class ContactRequestDto {

    @NotBlank(message = "Il nome del referente è obbligatorio")
    private String name;

    private String companyName;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    @Size(max = 100, message = "L'email non può superare i 100 caratteri")
    private String email;

    @Size(max = 30, message = "Il numero di telefono non può superare i 30 caratteri")
    private String phone;

    @NotBlank(message = "Il messaggio non può essere vuoto")
    @Size(min = 4, message = "Il messaggio deve avere più di 4 caratteri")
    private String message;

    // Getter e Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}