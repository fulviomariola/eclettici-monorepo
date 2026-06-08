package it.eclettici.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscribers")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    @Column(nullable = false, unique = true)
    private String email;

    private String name; // Opzionale

    @Column(name = "subscribed_at", nullable = false, updatable = false)
    private LocalDateTime subscribedAt;

    private boolean active = true; // Permette all'utente di fare l'opt-out (disiscrizione)

    @PrePersist
    protected void onCreate() {
        this.subscribedAt = LocalDateTime.now();
    }

    // Getter e Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDateTime subscribedAt) { this.subscribedAt = subscribedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}