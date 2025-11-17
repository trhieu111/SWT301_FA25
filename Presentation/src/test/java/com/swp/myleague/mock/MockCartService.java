package com.swp.myleague.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.swp.myleague.model.entities.saleproduct.CartItem;
import com.swp.myleague.model.entities.saleproduct.Product;

public class MockCartService {
    private final Map<String, CartItem> cart = new HashMap<>();
    private double discount = 0.0;

    public void addItem(Product product, int quantity) {
        String id = product.getProductId() != null ? product.getProductId().toString() : UUID.randomUUID().toString();
        CartItem item = cart.getOrDefault(id, new CartItem());
        item.setProduct(product);
        item.setProductAmount((item.getProductAmount() == null ? 0 : item.getProductAmount()) + quantity);
        cart.put(id, item);
    }

    public void removeItem(String productId) {
        cart.remove(productId);
    }

    public void updateQuantity(String productId, int qty) {
        CartItem item = cart.get(productId);
        if (item != null) {
            item.setProductAmount(qty);
        }
    }

    public double calculateTotal() {
        double total = 0.0;
        for (CartItem item : cart.values()) {
            if (item.getProduct() != null && item.getProductAmount() != null) {
                total += item.getProduct().getProductPrice() * item.getProductAmount();
            }
        }
        return total * (1.0 - discount);
    }

    public void applyDiscount(String code) {
        // Mock: if code is "SALE10" apply 10% discount
        if ("SALE10".equalsIgnoreCase(code)) {
            discount = 0.10;
        } else {
            discount = 0.0;
        }
    }

    public void clearCart() {
        cart.clear();
        discount = 0.0;
    }

    public Map<String, CartItem> getItems() {
        return cart;
    }
}
