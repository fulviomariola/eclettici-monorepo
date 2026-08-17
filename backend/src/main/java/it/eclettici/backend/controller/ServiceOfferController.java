package it.eclettici.backend.controller;

import it.eclettici.backend.entity.ServiceOffer;
import it.eclettici.backend.service.ServiceOfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceOfferController {

    private final ServiceOfferService serviceOfferService;

    public ServiceOfferController(ServiceOfferService serviceOfferService) {
        this.serviceOfferService = serviceOfferService;
    }

    /**
     * ENDPOINT PUBBLICO: Utilizzato dal frontend per la vetrina dei servizi attivi.
     */
    @GetMapping
    public List<ServiceOffer> getPublicServices() {
        return serviceOfferService.getActiveServices();
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Inserimento di un nuovo servizio.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<ServiceOffer> createService(@Valid @RequestBody ServiceOffer serviceOffer) {
        ServiceOffer saved = serviceOfferService.createService(serviceOffer);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Modifica testi, icone o stato attivo del servizio.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ServiceOffer updateService(@PathVariable UUID id, @Valid @RequestBody ServiceOffer details) {
        return serviceOfferService.updateService(id, details);
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Eliminazione dal database.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        serviceOfferService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}