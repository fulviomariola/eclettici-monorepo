package it.eclettici.backend.dto;

import java.util.UUID;

public class LoginResponseDto {
    private String id; // Stringa o UUID in base a come lo gestisci, usiamo String per comodità con la mappa precedente
    private String email;
    private String role;
    private String messaggio;
    private String token; // Il nostro passaporto digitale JWT

    // Costruttore vuoto necessario per la serializzazione di Spring/Jackson
    public LoginResponseDto() {}

    // Costruttore completo
    public LoginResponseDto(String id, String email, String role, String messaggio, String token) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.messaggio = messaggio;
        this.token = token;
    }

    // Getter e Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessaggio() { return messaggio; }
    public void setMessaggio(String messaggio) { this.messaggio = messaggio; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}