package it.eclettici.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Definizione delle regole di autorizzazione per gli endpoint.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Attiviamo il CORS con la configurazione personalizzata descritta sotto
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Disabilitiamo il CSRF (necessario per le API REST stateless che usano Postman/Frontend)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Configurazione delle regole sugli URL
                .authorizeHttpRequests(auth -> auth
                        // --- ENDPOINT PUBBLICI ---
                        // Chiunque può vedere i servizi e inviare un messaggio di contatto
                        .requestMatchers(HttpMethod.GET, "/api/services").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contacts").permitAll()
                        // Nuove rotte abilitate
                        .requestMatchers(HttpMethod.POST, "/api/email/subscribe").permitAll()

                        // --- ENDPOINT PROTETTI (Solo ADMIN) ---
                        // La gestione dei contatti (lettura/modifica) è riservata all'amministratore
                        .requestMatchers("/api/contacts/**").hasRole("ADMIN")
                        // La scrittura, modifica e cancellazione dei servizi sono riservate all'amministratore
                        .requestMatchers(HttpMethod.POST, "/api/services").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/services/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasRole("ADMIN")
                        .requestMatchers("/api/email/bulk-send").hasRole("ADMIN")

                        // Qualsiasi altra richiesta non specificata richiede l'autenticazione
                        .anyRequest().authenticated()
                )

                // 3. Attiviamo l'autenticazione HTTP Basic (ideale per i test immediati su Postman)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        // Autorizza esplicitamente l'URL del frontend Angular
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:4200"));

        // Abilita i metodi HTTP necessari per le operazioni CRUD e pre-flight (OPTIONS)
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Consente gli header standard per il passaggio di JSON e token di autenticazione
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));

        // Permette al browser di leggere l'header Authorization (utile per i futuri token JWT)
        configuration.setExposedHeaders(java.util.List.of("Authorization"));

        // Consente l'invio di credenziali (cookie, HTTP Basic, ecc.) se necessario
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Applica la regola a tutti gli endpoint
        return source;
    }

    /**
     * Configurazione temporanea di un utente in memoria per i test su Postman.
     * Sostituisce temporaneamente la tabella del DB per convalidare lo scudo di sicurezza.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin@eclettici.it")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * Definizione dell'algoritmo di cifratura delle password (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}