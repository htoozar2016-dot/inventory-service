package com.workshop.inventory.controller;

import com.workshop.inventory.model.Product;
import com.workshop.inventory.model.StockReduceRequest;
import com.workshop.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerTest {

    @Test
    void getAllReturnsProductsFromRepository() {
        Product keyboard = new Product(1L, "Keyboard", 49.99, 10);
        FakeProductRepository productRepository = new FakeProductRepository();
        productRepository.products = List.of(keyboard);
        ProductController productController = new ProductController(productRepository);

        List<Product> products = productController.getAll();

        assertThat(products).containsExactly(keyboard);
        assertThat(productRepository.findAllCalled).isTrue();
    }

    @Test
    void reduceStockReturnsBadRequestWhenStockIsInsufficient() {
        StockReduceRequest.StockItem item = new StockReduceRequest.StockItem();
        item.setProductId(1L);
        item.setQty(99);

        StockReduceRequest request = new StockReduceRequest();
        request.setItems(List.of(item));

        FakeProductRepository productRepository = new FakeProductRepository();
        productRepository.reduceStockException = new IllegalStateException("Insufficient stock for product: 1");
        ProductController productController = new ProductController(productRepository);

        ResponseEntity<Map<String, String>> response = productController.reduceStock(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Insufficient stock for product: 1");
        assertThat(productRepository.reducedItems).isSameAs(request.getItems());
    }

    // Uncomment this test during the workshop to demonstrate a failed PR check.
    //
    // @Test
    // void demoFailingTestForPullRequestCheck() {
    //     assertThat(1 + 1).isEqualTo(3);
    // }

    private static class FakeProductRepository extends ProductRepository {
        private List<Product> products = List.of();
        private boolean findAllCalled;
        private List<StockReduceRequest.StockItem> reducedItems;
        private RuntimeException reduceStockException;

        private FakeProductRepository() {
            super(null);
        }

        @Override
        public List<Product> findAll() {
            findAllCalled = true;
            return products;
        }

        @Override
        public void reduceStock(List<StockReduceRequest.StockItem> items) {
            reducedItems = items;
            if (reduceStockException != null) {
                throw reduceStockException;
            }
        }
    }
}
