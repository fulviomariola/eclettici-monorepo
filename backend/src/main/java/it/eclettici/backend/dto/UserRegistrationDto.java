package it.eclettici.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 2, max = 50, message = "Il nome deve essere compreso tra 2 e 50 caratteri")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(min = 2, max = 50, message = "Il cognome deve essere compreso tra 2 e 50 caratteri")
    private String cognome;

    @NotBlank(message = "L'indirizzo email è obbligatorio")
    @Email(message = "Inserisci un indirizzo email valido")
    private String email;

    //@Size(min = 8, message = "La password deve contenere almeno 8 caratteri.")
    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve contenere almeno 6 caratteri")
    @Pattern(
            regexp = "^(?=.*[$&%@#!*?]).*$",
            message = "La password deve contenere almeno un carattere speciale tra: $, &, %, @, #, !, *, ?"
    )
    private String password;

    @NotBlank(message = "La scelta del ruolo è obbligatoria")
    private String ruolo; // Riceverà "STUDENT" o "STORE" dal frontend

    // --- GETTER AND SETTER ---
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }
}