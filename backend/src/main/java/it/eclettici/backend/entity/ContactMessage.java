package it.eclettici.backend.entity;

import it.eclettici.backend.enums.ContactMessageStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Il nome del referente è obbligatorio")
    @Column(nullable = false)
    private String name;

    @Column(name = "company_name")
    private String companyName; // Opzionale (es. nome del negozio)

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    @Column(nullable = false)
    private String email;

    private String phone; // Opzionale (recapito telefonico diretto)

    @NotBlank(message = "Il messaggio non può essere vuoto")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactMessageStatus status;

    // Metodo di ciclo di vita di Hibernate per impostare automaticamente la data prima dell'insert
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getter e Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ContactMessageStatus getStatus() { return status; }
    public void setStatus(ContactMessageStatus status) { this.status = status; }
}