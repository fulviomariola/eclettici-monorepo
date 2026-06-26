package it.eclettici.backend.security;

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

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String userEmail;

        // Se l'header manca o non inizia con "Bearer", passo al filterChain successivo senza autenticare
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request,response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.estraiEmail(jwt);

            // Se l'email è valida e l'utente non è già autenticato nel contesto corrente
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.validaToken(jwt, userEmail)) {
                    // Estraggo il ruolo dal token
                    String ruolo = jwtService.estraiRuolo(jwt);

                    // Creo l'autorità per Spring Security
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(ruolo));

                    // Costruire l'oggetto di autenticazione iniettando le autorità (ruoli) reali
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Iniettare l'utente autenticato nel contesto di Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // --- CATTURIAMO LA SCADENZA SENZA BLOCCARE IL SERVER ---
            e.printStackTrace();
            logger.warn("Il token JWT fornito è scaduto, la richiesta proseguirà come utente anonimo: " + e.getMessage());
        } catch (Exception e) {
            // In caso di token corrotto o alterato, evitare blocco applicazione
            e.printStackTrace();
            logger.error("Impossibile impostare l'autenticazione utente: " + e.getMessage());
        }

        filterChain.doFilter(request,response);
    }
}




















