/*
* ENTITY LAYER - Junction/Join Table
*
* OrderItem is the "many" side of relationships with both Order and Product
* This is a classic JOIN TABLE in a Many-to-Many relationship between Order and Product
*
* */

package com.wehavecrown.creatorstore.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_purchase", nullable = false)
    private BigDecimal priceAtPurchase;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}

/*
 * DATABASE TABLE STRUCTURE: order_items
 *
 * COLUMNS:
 *   - id (BIGINT, PK, AUTO_INCREMENT)
 *   - quantity (INT, NOT NULL)
 *   - price_at_purchase (DECIMAL, NOT NULL)
 *   - order_id (BIGINT, FK to orders.id, NOT NULL)
 *   - product_id (BIGINT, FK to products.id, NOT NULL)
 *
 * FOREIGN KEYS:
 *   - order_id -> orders(id)  (Which order does this item belong to?)
 *   - product_id -> products(id) (Which product is being ordered?)
 */

/*
 * UNDERSTANDING THE RELATIONSHIPS:
 *
 * Order (1) ----< OrderItem >---- (1) Product
 *
 * 1. Order to OrderItem: One-to-Many (1 order, many items)
 *    - Order is the "parent" (@JsonManagedReference)
 *    - OrderItem is the "child" (@JsonBackReference)
 *
 * 2. Product to OrderItem: One-to-Many (1 product, many order items)
 *    - Product uses @JsonIgnore (hides the relationship)
 *    - OrderItem has @ManyToOne to Product
 *
 * 3. The combination creates a Many-to-Many between Order and Product
 *    - An order can have multiple products
 *    - A product can appear in multiple orders
 *    - OrderItem is the "join table" that connects them
 */

/*
 * WHY THIS DESIGN IS IMPORTANT:
 *
 * Problem: Many-to-Many between Order and Product
 * Solution: Create a join table (OrderItem) with extra data
 *
 * WITHOUT OrderItem:
 *   Order (M) ----< (M) Product  (Many-to-Many)
 *   - Can't store quantity or price at purchase
 *   - Can't track when item was added
 *
 * WITH OrderItem:
 *   Order (1) ---< OrderItem >--- (1) Product
 *   - Can store quantity per product
 *   - Can store price at purchase (historical data)
 *   - Can add more fields later (discount, tax, etc.)
 */

/*
 * COMMON OPERATIONS:
 *
 * 1. Get total cost of order:
 *    BigDecimal total = order.getOrderItems().stream()
 *        .map(item -> item.getPriceAtPurchase().multiply(
 *            new BigDecimal(item.getQuantity())))
 *        .reduce(BigDecimal.ZERO, BigDecimal::add);
 *
 * 2. Check if product is in order:
 *    boolean hasProduct = order.getOrderItems().stream()
 *        .anyMatch(item -> item.getProduct().getId().equals(productId));
 *
 * 3. Create order item with builder:
 *    OrderItem item = OrderItem.builder()
 *        .quantity(3)
 *        .priceAtPurchase(new BigDecimal("29.99"))
 *        .order(order)
 *        .product(product)
 *        .build();
 *
 * 4. Adding item to order (in Order class):
 *    // Helper method in Order entity
 *    public void addItem(OrderItem item) {
 *        items.add(item);
 *        item.setOrder(this);
 *    }
 */

/*
 * THREE-WAY RELATIONSHIP DIAGRAM:
 *
 *           ┌─────────────┐
 *           │    Order    │
 *           │  (customer) │
 *           └──────┬──────┘
 *                  │ 1
 *                  │
 *                  │ M
 *           ┌──────▼──────┐
 *           │  OrderItem  │
 *           │ - quantity  │
 *           │ - priceAt   │
 *           │   Purchase  │
 *           └──────┬──────┘
 *                  │ M
 *                  │
 *                  │ 1
 *           ┌──────▼──────┐
 *           │   Product   │
 *           │  (details)  │
 *           └─────────────┘
 *
 * This is a classic normalized database design
 * Avoids data duplication and maintains referential integrity
 */