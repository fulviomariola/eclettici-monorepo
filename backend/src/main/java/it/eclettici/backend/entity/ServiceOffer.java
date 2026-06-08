package it.eclettici.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Table(name = "service_offers")
public class ServiceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Il titolo del servizio è obbligatorio")
    @Column(nullable = false, unique = true)
    private String title; // Es: "Sviluppo Web e Applicativi Cloud"

    @NotBlank(message = "La descrizione è obbligatoria")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; // Dettagli tecnici del servizio offerto

    @Column(name = "icon_name")
    private String iconName; // Es: "code", "shield-lock", "network" (servirà al frontend per mostrare l'icona giusta)

    @Column(nullable = false)
    private boolean active = true; // Permette di nascondere un servizio dal sito senza cancellarlo dal DB

    // Getter e Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}