package com.workshop.inventory.repository;

import com.workshop.inventory.model.Product;
import com.workshop.inventory.model.StockReduceRequest.StockItem;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<Product> rowMapper = (rs, rowNum) -> new Product(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getInt("qty"));

    public ProductRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> findAll() {
        return jdbc.query("SELECT * FROM products", Map.of(), rowMapper);
    }

    public Optional<Product> findById(Long id) {
        List<Product> result = jdbc.query(
                "SELECT * FROM products WHERE id = :id",
                Map.of("id", id),
                rowMapper);
        return result.stream().findFirst();
    }

    public Product create(String name, double price, int qty) {
        jdbc.update(
                "INSERT INTO products (name, price, qty) VALUES (:name, :price, :qty)",
                Map.of("name", name, "price", price, "qty", qty));
        Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Map.of(), Long.class);
        return new Product(id, name, price, qty);
    }

    public Optional<Product> update(Long id, String name, Double price, Integer qty) {
        return findById(id).map(existing -> {
            String updatedName = name != null ? name : existing.getName();
            double updatedPrice = price != null ? price : existing.getPrice();
            int updatedQty = qty != null ? qty : existing.getQty();

            jdbc.update(
                    "UPDATE products SET name = :name, price = :price, qty = :qty WHERE id = :id",
                    Map.of("name", updatedName, "price", updatedPrice, "qty", updatedQty, "id", id));
            return new Product(id, updatedName, updatedPrice, updatedQty);
        });
    }

    public boolean delete(Long id) {
        int rows = jdbc.update("DELETE FROM products WHERE id = :id", Map.of("id", id));
        return rows > 0;
    }

    @Transactional
    public void reduceStock(List<StockItem> items) {
        // Validate all items first
        for (StockItem item : items) {
            Integer current = jdbc.queryForObject(
                    "SELECT qty FROM products WHERE id = :id",
                    Map.of("id", item.getProductId()),
                    Integer.class);
            if (current == null) {
                throw new IllegalArgumentException("Product not found: " + item.getProductId());
            }
            if (current < item.getQty()) {
                throw new IllegalStateException("Insufficient stock for product: " + item.getProductId());
            }
        }
        // Deduct stock
        for (StockItem item : items) {
            jdbc.update(
                    "UPDATE products SET qty = qty - :qty WHERE id = :id",
                    Map.of("qty", item.getQty(), "id", item.getProductId()));
        }
    }
}
