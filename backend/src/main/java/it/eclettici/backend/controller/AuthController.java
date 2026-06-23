package it.eclettici.backend.controller;

import it.eclettici.backend.entity.User;
import it.eclettici.backend.dto.UserRegistrationDto;
import it.eclettici.backend.enums.Role;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // 1. Controllo di sicurezza: verifichiamo se l'email esiste già nel DB
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("dettaglio", "Errore: l'indirizzo email è già in uso."));
        }

        // 2. Creazione dell'entità User e cifratura della password con BCrypt
        User newUser = new User();
        newUser.setNome(registrationDto.getNome());
        newUser.setCognome(registrationDto.getCognome());
        newUser.setEmail(registrationDto.getEmail());

        // Cifratura della password usando il passwordEncoder del tuo SecurityConfig
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        // 3. Mappatura del Ruolo dall'Enum
        try {
            newUser.setRole(Role.valueOf(registrationDto.getRuolo()));
        } catch (IllegalArgumentException | NullPointerException e) {
            // Se il ruolo non è valido o arriva vuoto, assegniamo "STUDENT" di default
            newUser.setRole(Role.STUDENT);
        }

        // 4. Salvataggio effettivo su PostgreSQL
        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of("messaggio", "Utente registrato con successo!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        // Ricevo oggetto Authentication direttamente da Spring Security
        String emailInviata = loginData.get("email");
        String passwordInviata = loginData.get("password");

        // 1. Cerco utente nel DB tramite email
        User user = userRepository.findByEmail(emailInviata)
                .orElse(null);

        // 2. Verifico se esiste utente e se password coincide nel DB
        if (user == null || !passwordEncoder.matches(passwordInviata, user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("dettaglio", "Credenziali non valide. Controlla email e password."));
        }

        // 3. Recupero ruolo reale
        String cleanedRole = user.getRole().name();

        // 4. RESITUIRE L'ID REALE AL FRONTEND
        return ResponseEntity.ok(Map.of(
                "id", user.getId().toString(),  // ID dinamico che serve ad Angular!
                "email", user.getEmail(),
                "role", cleanedRole,
                "messaggio", "Autenticazione eseguita con successo!"
        ));
    }
}








