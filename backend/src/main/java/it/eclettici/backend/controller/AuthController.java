package it.eclettici.backend.controller;

import it.eclettici.backend.entity.User;
import it.eclettici.backend.dto.UserRegistrationDto;
import it.eclettici.backend.dto.LoginResponseDto;
import it.eclettici.backend.enums.Role;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;  // Aggiunta riferimento a JwtService.java

    // Aggiornamento costruttore per iniettare, fra gli altri, JwtService
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // 1. Controllo di sicurezza: verifichiamo se l'email esiste già nel DB
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("dettaglio", "Errore: l'indirizzo email è già in uso."));
        }

        // Creazione dell'entità User e cifratura della password con BCrypt
        User newUser = new User();
        newUser.setNome(registrationDto.getNome());
        newUser.setCognome(registrationDto.getCognome());
        newUser.setEmail(registrationDto.getEmail());

        // Cifratura della password usando il passwordEncoder del tuo SecurityConfig
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        // Mappatura del Ruolo dall'Enum
        try {
            newUser.setRole(Role.valueOf(registrationDto.getRuolo()));
        } catch (IllegalArgumentException | NullPointerException e) {
            newUser.setRole(Role.STUDENT);
        }

        // Salvataggio effettivo su PostgreSQL
        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("messaggio", "Utente registrato con successo!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        // Ricevo oggetto Authentication direttamente da Spring Security
        String emailInviata = loginData.get("email");
        String passwordInviata = loginData.get("password");

        // Cerco utente nel DB tramite email
        User user = userRepository.findByEmail(emailInviata)
                .orElse(null);

        // Verifico se esiste utente e se password coincide nel DB
        if (user == null || !passwordEncoder.matches(passwordInviata, user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("dettaglio", "Credenziali non valide. Controlla email e password."));
        }

        // Recupero ruolo reale
        String cleanedRole = user.getRole().name();

        // GENERARE IL TOKEN REALE SFRUTTANDO IL JWT SERVICE
        String tokenGenerato = jwtService.generaToken(
                user.getEmail(),
                cleanedRole,
                user.getId().toString()
        );

        // RESTITUIREO IL DTO COMPLETO DI TOKEN ANZICHÉ LA MAPPA ANONIMA
        LoginResponseDto risposta = new LoginResponseDto(
                user.getId().toString(),
                user.getEmail(),
                cleanedRole,
                "Autenticazione eeguita con successo!",
                tokenGenerato
        );

        return ResponseEntity.ok(risposta);
    }
}








