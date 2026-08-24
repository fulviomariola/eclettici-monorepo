package it.eclettici.backend.config;

import it.eclettici.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAutheFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAutheFilter) {
        this.jwtAutheFilter = jwtAutheFilter;
    }

    /**
     * Definizione delle regole di sicurezza e autorizzazione per gli endpoint.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Attivazione CORS collegato al bean corsConfigurationSource()
                .cors(Customizer.withDefaults())

                // 2. Disabilitazione CSRF (necessario per API REST stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Gestione della sessione come STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Regole sugli endpoint (DALLA PIÙ SPECIFICA ALLA PIÙ GENERALE)
                .authorizeHttpRequests(auth -> auth
                        // --- 1. TUTTI GLI ENDPOINT PUBBLICI (permitAll) ---
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/debug-auth").permitAll()
                        .requestMatchers("/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contacts").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email/subscribe").permitAll()

                        // Permessi dei corsi
                        .requestMatchers("/api/courses/**").permitAll()

                        // Rotte Video e Importazione pubbliche per i test
                        .requestMatchers("/api/admin/videos/**").permitAll() // <-- Spostato in cima!
                        .requestMatchers("/api/videos/pubblici", "/api/videos/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/videos/**").permitAll()

                        // Commenti pubblici in lettura (entrambe le varianti con e senza /api)
                        .requestMatchers("/api/comments/video/**", "/comments/video/**").permitAll() // <-- Spostato in cima!

                        // --- 2. ENDPOINT PROTETTI DA RUOLI ---
                        .requestMatchers(HttpMethod.POST, "/api/comments/**").hasAnyRole("STUDENT", "STORE", "ADMIN")
                        .requestMatchers("/api/comments/**").hasAnyRole("STUDENT", "STORE", "ADMIN")
                        .requestMatchers("/api/progress/**").authenticated()

                        // Rotte generiche Admin / Store
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/videos").hasRole("STORE")
                        .requestMatchers(HttpMethod.GET, "/api/videos/premium").hasAnyRole("STUDENT", "STORE", "ADMIN")

                        .requestMatchers("/api/contacts/**").hasAnyRole("ADMIN", "STORE")
                        .requestMatchers(HttpMethod.POST, "/api/services").hasAnyRole("ADMIN", "STORE")
                        .requestMatchers(HttpMethod.PUT, "/api/services/**").hasAnyRole("ADMIN", "STORE")
                        .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasAnyRole("ADMIN", "STORE")
                        .requestMatchers("/api/email/bulk-send").hasAnyRole("ADMIN", "STORE")

                        // --- 3. TUTTE LE ALTRE ROTTE ---
                        .anyRequest().authenticated()
                )

                // 5. Filtro JWT prima del filtro standard di autenticazione
                .addFilterBefore(jwtAutheFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origini autorizzate: include sviluppo locale e il dominio reale su VPS
        configuration.setAllowedOriginPatterns(List.of(
                "https://test.eclettici.it",
                "https://*.eclettici.it",
                "http://localhost:4200",
                "http://localhost:*",
                "http://192.168.1.*:4200"
        ));

        // Metodi HTTP ammessi
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Header ammessi
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));

        // Header esposti al frontend
        configuration.setExposedHeaders(List.of("Authorization"));

        // Abilita invio credenziali
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}