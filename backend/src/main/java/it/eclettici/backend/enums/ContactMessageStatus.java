package it.eclettici.backend.enums;

public enum ContactMessageStatus {
    PENDING,      // Il negozio ha inviato il form, deve essere ricontattato
    IN_PROGRESS,  // Trattativa o analisi tecnica in corso
    COMPLETED     // Contratto chiuso o richiesta evasa
}