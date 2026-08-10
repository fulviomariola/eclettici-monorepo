package it.eclettici.backend.controller;

import it.eclettici.backend.dto.ChangePasswordDto;
import it.eclettici.backend.dto.UserProfileDto;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Recupera il profilo dell'utente correntemente autenticato
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'STORE', 'ADMIN')")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserProfileDto dto = new UserProfileDto(user.getEmail(), user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(dto);
    }

    /**
     * Aggiorna la password dell'utente autenticato
     */
    @PutMapping("/change-password")
    @PreAuthorize("hasAnyRole('STUDENT', 'STORE', 'ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto dto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        // 1. Verifichiamo che la password attuale coincida
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La password attuale non è corretta."));
        }

        // 2. Codifichiamo e salviamo la nuova password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password aggiornata con successo!"));
    }
}