package it.eclettici.backend.service;

import it.eclettici.backend.entity.ServiceOffer;
import it.eclettici.backend.repository.ServiceOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.NoSuchElementException;

@Service
public class ServiceOfferService {

    private final ServiceOfferRepository serviceOfferRepository;

    public ServiceOfferService(ServiceOfferRepository serviceOfferRepository) {
        this.serviceOfferRepository = serviceOfferRepository;
    }

    @Transactional
    public ServiceOffer createService(ServiceOffer serviceOffer) {
        return serviceOfferRepository.save(serviceOffer);
    }

    // Usato dal Frontend pubblico: mostra solo i servizi attivi sul mercato
    public List<ServiceOffer> getActiveServices() {
        return serviceOfferRepository.findByActiveTrue();
    }

    // Usato dal tuo Backoffice: ti mostra tutti i servizi, anche quelli temporaneamente disattivati
    public List<ServiceOffer> getAllServices() {
        return serviceOfferRepository.findAll();
    }

    @Transactional
    public ServiceOffer updateService(UUID id, ServiceOffer details) {
        ServiceOffer existing = serviceOfferRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Servizio non trovato con ID: " + id));

        existing.setTitle(details.getTitle());
        existing.setDescription(details.getDescription());
        existing.setIconName(details.getIconName());
        existing.setActive(details.isActive());

        return serviceOfferRepository.save(existing);
    }

    @Transactional
    public void deleteService(UUID id) {
        if (!serviceOfferRepository.existsById(id)) {
            throw new NoSuchElementException("Impossibile eliminare. Servizio non trovato.");
        }
        serviceOfferRepository.deleteById(id);
    }
}