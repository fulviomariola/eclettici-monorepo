# Eclettici Backend - REST API Core

Repository centrale del backend di **eclettici.it**, un ecosistema Full-Stack progettato per la digitalizzazione e la consulenza IT delle PMI e delle attività locali. L'applicazione è sviluppata in Java con l'ecosistema Spring Boot, seguendo un'architettura a livelli (Layered Architecture) disaccoppiata e sicura.

## 🛠️ Tech Stack
* **Linguaggio:** Java 17+
* **Framework:** Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security)
* **Database:** PostgreSQL
* **Validazione:** Jakarta Validation (Hibernate Validator)
* **Sicurezza:** BCrypt Password Encoder

## 🚀 Funzionalità Principali Implementate

1. **Modulo Lead Generation (Contatti Commerciali):** Gestione delle richieste di preventivo da parte delle imprese con immutabilità dello stato iniziale (`PENDING`) a tutela dei flussi interni.
2. **Catalogo Servizi Dinamico:** Gestione delle offerte B2B con possibilità di attivazione/disattivazione logica dei servizi direttamente dal database senza modifiche al codice client.
3. **Mailing Engine Asincrono:** Motore di invio e-mail massivo e mirato basato su `@Async`. Gestisce le code di spedizione in background tramite thread dedicati della CPU per evitare timeout e blocchi antispam.
4. **Blog & Community (Risoluzione Ricorsione Circolare):** Struttura Post/Commenti ottimizzata tramite l'implementazione del pattern **DTO (Data Transfer Object)** sia in ingresso che in uscita. Disaccoppia le entità del database dalla serializzazione JSON, eliminando loop infiniti di memoria.

## 🔒 Sicurezza ed Autorizzazione
Il sistema è protetto centralmente da uno scudo di **Spring Security 6.x** basato su regole granulari per verbo HTTP:
* **Rotte Pubbliche:** Invio contatti, lettura blog, iscrizione newsletter e visualizzazione dei servizi attivi.
* **Rotte Protette (Role `ADMIN`):** Lettura ed avanzamento dei lead, gestione CRUD del catalogo servizi ed invio e-mail massive. Password memorizzate esclusivamente tramite hashing sicuro **BCrypt**.

## 🚀 Come Avviare il Progetto locali
1. Configurare le credenziali del database PostgreSQL nel file `src/main/resources/application.properties`.
2. Eseguire il bootstrap dell'applicazione tramite il proprio IDE o comando Maven:
   ```bash
   mvn spring-boot:run
