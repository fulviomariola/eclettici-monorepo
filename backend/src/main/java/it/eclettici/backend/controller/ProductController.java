package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Product;
import it.eclettici.backend.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * ENDPOINT PUBBLICO: Recupera i soli prodotti disponibili per lo Store
     */
    @GetMapping
    public ResponseEntity<List<Product>> getPublicProducts() {
        return ResponseEntity.ok(productRepository.findByAvailableTrue());
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Creazione nuovo prodotto
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product saved = productRepository.save(product);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Aggiornamento dati prodotto
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @Valid @RequestBody Product details) {
        return productRepository.findById(id).map(p -> {
            p.setName(details.getName());
            p.setDescription(details.getDescription());
            p.setPrice(details.getPrice());
            p.setImageUrl(details.getImageUrl());
            p.setAvailable(details.isAvailable());
            return ResponseEntity.ok(productRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Eliminazione prodotto
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}