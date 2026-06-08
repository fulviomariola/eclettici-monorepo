package it.eclettici.backend.controller;

import it.eclettici.backend.entity.ServiceOffer;
import it.eclettici.backend.service.ServiceOfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class ServiceOfferController {

    private final ServiceOfferService serviceOfferService;

    public ServiceOfferController(ServiceOfferService serviceOfferService) {
        this.serviceOfferService = serviceOfferService;
    }

    /**
     * ENDPOINT PUBBLICO: Il frontend lo invoca per popolare la Homepage di eclettici.it
     * Ritorna solo i servizi attivi.
     */
    @GetMapping
    public List<ServiceOffer> getPublicServices() {
        return serviceOfferService.getActiveServices();
    }

    /**
     * ENDPOINT AMMINISTRATORE (Futuro protetto): Permette a te di inserire un nuovo servizio.
     */
    @PostMapping
    public ResponseEntity<ServiceOffer> createService(@Valid @RequestBody ServiceOffer serviceOffer) {
        ServiceOffer saved = serviceOfferService.createService(serviceOffer);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * ENDPOINT AMMINISTRATORE (Futuro protetto): Permette di modificare i testi o disattivare un servizio.
     */
    @PutMapping("/{id}")
    public ServiceOffer updateService(@PathVariable UUID id, @Valid @RequestBody ServiceOffer details) {
        return serviceOfferService.updateService(id, details);
    }

    /**
     * ENDPOINT AMMINISTRATORE (Futuro protetto): Eliminazione fisica dal database.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        serviceOfferService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}