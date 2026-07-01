package it.eclettici.backend;

import it.eclettici.backend.service.YoutubeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // <--- ATTIVAZIONE MOTORE ASINCRONO DI SPRING
public class BackendApplication {

    public static void main(String[] args) {
        // Avvia il contesto di Spring e l'application server integrato
        SpringApplication.run(BackendApplication.class, args);
    }

    // COMMENTA QUESTO BLOCCO DOPO IL TEST
/*    @Bean
    CommandLineRunner testSync(YoutubeService youtubeService) {
        return args -> {
            try {
                System.out.println("🚀 AVVIO TEST DI SINCRONIZZAZIONE PLAYLIST...");
                // Inserisci qui un ID playlist reale che hai pre-caricato nella tabella 'courses'
                youtubeService.syncPlaylist("PLFv9W5SOpvJE");
                System.out.println("✅ SINCRONIZZAZIONE COMPLETATA CON SUCCESSO! Controlla la tabella 'videos'.");
            } catch (Exception e) {
                System.err.println("❌ ERRORE DURANTE IL TEST: " + e.getMessage());
            }
        };
    }*/
}
