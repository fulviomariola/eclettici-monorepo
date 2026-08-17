package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Order;
import it.eclettici.backend.enums.OrderStatus;
import it.eclettici.backend.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * ENDPOINT PUBBLICO/AUTENTICATO: Creazione di un nuovo ordine
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        Order saved = orderRepository.save(order);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * ENDPOINT UTENTE: Recupera gli ordini del singolo utente
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Consultazione di tutti gli ordini a sistema
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAllByOrderByCreatedAtDesc());
    }

    /**
     * ENDPOINT RISERVATO (STORE / ADMIN): Aggiornamento dello stato ordine
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {

        return orderRepository.findById(id).map(o -> {
            o.setStatus(status);
            return ResponseEntity.ok(orderRepository.save(o));
        }).orElse(ResponseEntity.notFound().build());
    }
}