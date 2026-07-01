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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAutheFilter;

    // Iniettare il nuovo filtro JWT
    public SecurityConfig(JwtAuthenticationFilter jwtAutheFilter) {
        this.jwtAutheFilter = jwtAutheFilter;
    }

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

                // Configura la gestione della sessione come STATELESS (essenziale per JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. Configurazione delle regole sugli URL
                .authorizeHttpRequests(auth -> auth
                        // --- 1. TUTTI GLI ENDPOINT PUBBLICI (permitAll) ---
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/posts/**").permitAll()

                        // Permetti a tutti l'accesso ai video pubblici
                        .requestMatchers(HttpMethod.GET, "/api/videos/pubblici").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/services").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contacts").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email/subscribe").permitAll()

                        // --- 2. ENDPOINT PROTETTI DA AUTORIZZAZIONE (hasAuthority / hasAnyAuthority) ---

                        // Corretto da hasRole a hasAuthority per allinearsi al JWT senza prefisso ROLE_
                        .requestMatchers("/api/admin/**").hasAuthority("STORE")

                        // Proteggi l'inserimento manuale sulla rotta esatta
                        .requestMatchers(HttpMethod.POST, "/api/videos").hasAuthority("STORE")

                        // Proteggi la rotta premium: Accesso consentito a STUDENT e STORE autenticati
                        .requestMatchers(HttpMethod.GET, "/api/videos/premium").hasAnyAuthority("STUDENT", "STORE")

                        // Gestione Contatti, Servizi e invio Bulk (Solo ADMIN e STORE)
                        .requestMatchers("/api/contacts/**").hasAnyAuthority("ADMIN", "STORE")
                        .requestMatchers(HttpMethod.POST, "/api/services").hasAnyAuthority("ADMIN", "STORE")
                        .requestMatchers(HttpMethod.PUT, "/api/services/**").hasAnyAuthority("ADMIN", "STORE")
                        .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasAnyAuthority("ADMIN", "STORE")
                        .requestMatchers("/api/email/bulk-send").hasAnyAuthority("ADMIN", "STORE")

                        // --- 3. CHIUSURA DELLA CATENA ---
                        // Qualsiasi altra richiesta non specificata richiede l'autenticazione
                        .anyRequest().authenticated()
                );

        // ABILITARE FILTRAGGIO REALE: esegue jwtAuthFilter prima di UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAutheFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        // Autorizza esplicitamente l'URL del frontend Angular
        configuration.setAllowedOriginPatterns(java.util.List.of(
                "http://localhost:4200",
                "http://192.168.1.*:4200"
        ));

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


    // ---------------DA COMMENTARE QUANDO SONO IN PRODUZIONE-----------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Creiamo un encoder delegato che capisce i prefissi
        String idForEncode = "bcrypt";
        java.util.Map<String, PasswordEncoder> encoders = new java.util.HashMap<>();

        encoders.put("bcrypt", new BCryptPasswordEncoder());
        // Il NoOpPasswordEncoder serve per leggere le password in chiaro (NON USARE IN PRODUZIONE)
        encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());

        org.springframework.security.crypto.password.DelegatingPasswordEncoder delegatingPasswordEncoder =
                new org.springframework.security.crypto.password.DelegatingPasswordEncoder(idForEncode, encoders);

        // Questa è la riga magica: se la password nel DB non ha un prefisso (es. non inizia con {bcrypt}),
        // usa l'encoder "noop" (in chiaro) come fallback invece di lanciare un'eccezione.
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(
                org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance()
        );

        return delegatingPasswordEncoder;
    }

}