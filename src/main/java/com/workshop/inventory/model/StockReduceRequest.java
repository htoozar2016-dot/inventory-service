package com.workshop.inventory.model;

import java.util.List;

public class StockReduceRequest {

    private List<StockItem> items;

    public List<StockItem> getItems() { return items; }
    public void setItems(List<StockItem> items) { this.items = items; }

    public static class StockItem {
        private Long productId;
        private int qty;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }
    }
}
