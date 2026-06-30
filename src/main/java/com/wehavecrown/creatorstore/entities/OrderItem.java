/*
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


