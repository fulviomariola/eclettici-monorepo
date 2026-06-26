package it.eclettici.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // 1. Iniettiamo i valori definiti in application.properties
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private Long jwtExpirationMs;

    // 2. Trasformiamo la stringa segreta in una SecretKey adatta a HS256
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 3. METODO PER GENERARE IL TOKEN (Chiamato al momento del Login)
    public String generaToken(String email, String ruolo, String idUtente) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", ruolo);
        claims.put("id", idUtente);

        Date oraAttuale = new Date();
        Date dataScadenza = new Date(oraAttuale.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)                     // Dati personalizzati (Payload)
                .subject(email)                     // Soggetto (l'email del titolare)
                .issuedAt(oraAttuale)               // Data di creazione
                .expiration(dataScadenza)           // Data di scadenza dinamica
                .signWith(getSigningKey(), Jwts.SIG.HS256) // Firma digitale di sicurezza
                .compact();
    }

    // 4. METODI DI ESTRAZIONE E VERIFICA (Serviranno per i controlli successivi)

    // Estrae l'email (Subject) dal token
    public String estraiEmail(String token) {
        return estraiClaim(token, Claims::getSubject);
    }

    // Verifica se il token è scaduto
    public boolean isTokenScaduto(String token) {
        return estraiClaim(token, Claims::getExpiration).before(new Date());
    }

    // Valida il token confrontando l'email ed escludendo la scadenza
    public boolean validaToken(String token, String emailUtente) {
        final String emailEstrattta = estraiEmail(token);
        return (emailEstrattta.equals(emailUtente) && !isTokenScaduto(token));
    }

    // Estrae il ruolo del token (chiave "role")
    public String estraiRuolo(String token) {
        Claims claims = estraiTuttiIClaims(token);
        return claims.get("role", String.class);
    }

    // Funzione generica di utility per estrarre un singolo "claim" (pezzo di dato)
    private <T> T estraiClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = estraiTuttiIClaims(token);
        return claimsResolver.apply(claims);
    }

    // Decodifica il token usando la chiave segreta per leggerne il contenuto
    private Claims estraiTuttiIClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}