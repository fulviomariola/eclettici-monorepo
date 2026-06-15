package it.eclettici.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // <--- ATTIVAZIONE MOTORE ASINCRONO DI SPRING
public class BackendApplication {

    public static void main(String[] args) {
        // Avvia il contesto di Spring e l'application server integrato
        SpringApplication.run(BackendApplication.class, args);
    }

}
