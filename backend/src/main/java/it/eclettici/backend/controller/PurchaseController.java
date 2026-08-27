package it.eclettici.backend.controller;

import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final UserRepository userRepository;

    public PurchaseController(PurchaseService purchaseService, UserRepository userRepository) {
        this.purchaseService = purchaseService;
        this.userRepository = userRepository;
    }

    /**
     * Verifica se l'utente autenticato possiede il corso
     */
    @GetMapping("/check/{courseId}")
    public ResponseEntity<Map<String, Boolean>> checkPurchase(@PathVariable Long courseId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.ok(Collections.singletonMap("purchased", false));
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Collections.singletonMap("purchased", false));
        }

        boolean isPurchased = purchaseService.isCoursePurchased(user.getId(), courseId);
        return ResponseEntity.ok(Collections.singletonMap("purchased", isPurchased));
    }

    /**
     * Endpoint per simulare/eseguire l'acquisto di un corso
     */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<Map<String, String>> buyCourse(@PathVariable Long courseId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Accesso non autorizzato."));
        }

        purchaseService.purchaseCourse(auth.getName(), courseId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Corso acquistato e sbloccato con successo!"));
    }
}