package it.eclettici.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    // 3. IL TERZO VIGILE (Aggiornato per gestire errori MULTIPLI in ingresso)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "400");
        errorResponse.put("errore", "Validazione fallita");

        // 1. Raccogliamo TUTTI i messaggi di errore dei campi non validi
        List<String> listaErrori = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        // 2. Inseriamo la lista intera nel JSON con la chiave "errori"
        errorResponse.put("errori", listaErrori);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    // 3. IL TERZO VIGILE (Per il Bodyguard - Dati non validi in ingresso)
/*    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "400");
        errorResponse.put("errore", "Validazione fallita");

        // Questa riga magica va a pescare esattamente la frase
        // "Il titolo del post è obbligatorio..." che hai scritto dentro @NotBlank
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        errorResponse.put("dettaglio", errorMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }*/
}