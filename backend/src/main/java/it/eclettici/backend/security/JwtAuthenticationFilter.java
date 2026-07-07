package it.eclettici.backend.security;

import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository; // 1. Aggiunto il repository per caricare l'utente

    // Aggiornato il costruttore per iniettare il UserRepository
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.estraiEmail(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.validaToken(jwt, userEmail)) {

                    // 2. Recuperiamo l'oggetto User completo dal database usando l'email
                    User utenteCompleto = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new RuntimeException("Utente non trovato nel database"));

                    String ruolo = jwtService.estraiRuolo(jwt);
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(ruolo));

                    // 3. Passiamo l'oggetto 'utenteCompleto' (e non più solo la stringa email) come Principal
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            utenteCompleto, // <-- Ora Spring sa chi è l'utente reale e ne possiede l'UUID
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            e.printStackTrace();
            logger.warn("Il token JWT fornito è scaduto, la richiesta proseguirà come utente anonimo: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Impossibile impostare l'autenticazione utente: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}