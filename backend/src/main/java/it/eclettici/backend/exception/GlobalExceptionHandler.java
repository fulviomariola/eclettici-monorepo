package it.eclettici.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. IL PRIMO VIGILE (Per il Test A - Dato non trovato, rilancia status 404
    // ad un errore che è 500 Internal Server Error).
    // Il vigile si attiva solo ed esclusivamente se nel codice viene lanciata un'eccezione
    // di tipo java.util.NoSuchElementException (che è un'eccezione nativa di Java)
    /* @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NoSuchElementException ex) {

        // Creiamo un pacchetto JSON pulito e personalizzato
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "404");
        errorResponse.put("errore", "Risorsa non trovata");
        errorResponse.put("dettaglio", "L'ID specificato non corrisponde a nessun elemento nel database.");

        // Restituiamo il pacchetto dicendo a Spring di usare il codice HTTP 404 (invece del 500)
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    */


    // 1. IL PRIMO VIGILE (Gestisce sia NoSuchElementException che la nostra ResourceNotFoundException custom)
    @ExceptionHandler({NoSuchElementException.class, ResourceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFoundException(RuntimeException ex) {

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "404");
        errorResponse.put("errore", "Risorsa non trovata");

        // Usando ex.getMessage() il messaggio diventerà dinamico!
        // Se l'errore arriva dal CommentService, leggerai l'ID preciso del Post o dell'Utente mancante.
        errorResponse.put("dettaglio", ex.getMessage() != null ? ex.getMessage() : "L'ID specificato non corrisponde a nessun elemento.");

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 2. IL SECONDO VIGILE (Per il Test B - Formato parametro errato, non ha la sintassi di
    // un UUID ma inserisco un codice qualunque, come pippo)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "400");
        errorResponse.put("errore", "Formato parametro non valido");
        errorResponse.put("dettaglio", "Il valore inserito nell'URL non rispetta il formato UUID richiesto.");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 3. IL TERZO VIGILE (Per il Bodyguard - Dati non validi in ingresso)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "400");
        errorResponse.put("errore", "Validazione fallita");

        // Questa riga magica va a pescare esattamente la frase
        // "Il titolo del post è obbligatorio..." che hai scritto dentro @NotBlank
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        errorResponse.put("dettaglio", errorMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}