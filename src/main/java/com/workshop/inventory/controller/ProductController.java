package com.workshop.inventory.controller;

import com.workshop.inventory.model.Product;
import com.workshop.inventory.model.StockReduceRequest;
import com.workshop.inventory.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/products")
    public List<Product> getAll() {
        return repo.findAll();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/products")
    public ResponseEntity<Product> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        double price = ((Number) body.get("price")).doubleValue();
        int qty = ((Number) body.get("qty")).intValue();
        return ResponseEntity.status(201).body(repo.create(name, price, qty));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Double price = body.get("price") != null ? ((Number) body.get("price")).doubleValue() : null;
        Integer qty = body.get("qty") != null ? ((Number) body.get("qty")).intValue() : null;
        return repo.update(id, name, price, qty)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        if (!repo.delete(id))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    @PostMapping("/reduce-stock")
    public ResponseEntity<Map<String, String>> reduceStock(@RequestBody StockReduceRequest request) {
        try {
            repo.reduceStock(request.getItems());
            return ResponseEntity.ok(Map.of("message", "Stock reduced successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
