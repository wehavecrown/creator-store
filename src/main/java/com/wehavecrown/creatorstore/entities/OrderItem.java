/*
* ENTITY LAYER - Junction/Join Table
*
* OrderItem is the "many" side of relationships with both Order and Product
* This is a classic JOIN TABLE in a Many-to-Many relationship between Order and Product
*
* */

package com.wehavecrown.creatorstore.entities;

// Jackson - manages serialization to prevent infinite loops
// @JsonBackReference is the "child" side of bidirectional relationship
import com.fasterxml.jackson.annotation.JsonBackReference;

// JPA annotations for database mapping
import jakarta.persistence.*;

// Lombok - reduces boilerplate code
import lombok.*;

// Java types
import java.math.BigDecimal;

/*
* @Entity - Maps to "order_items" table in database
* @Table - Explicit table name
* */
@Entity
@Table(name = "order_items")

/*
* Lombok annotations:
* @Getter/@Setter - Generate getters and setters
* @AllArgsConstructor - Constructor with all fields
* @NoArgsConstructor - Empty constructor (required by JPA)
* @Builder - Builder patter for clean object creation
*
* Example: OrderItem.builder()
*       .quantity(2)
*       .priceAtPurchase(new BigDecimal("49.99"))
*       .order(order)
*       .product(product)
*       .build();
* */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    /*
    * Primary key - auto-incremented by database
    * Each record in order_items is unique
    * */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    * @Column(nullable = false) - Quantity is required
    * How many units of this product in the order
    * */
    @Column(nullable = false)
    private Integer quantity;

    /*
    * priceAtPurchase - snapshot of product price at time of order
    * */
    @Column(name = "price_at_purchase", nullable = false)
    private BigDecimal priceAtPurchase;

    /*
     * @JsonBackReference - The "backward" part of the relationship
     * Paired with @JsonManagedReference in Order
     *
     * Purpose: Prevents infinite recursion when serializing to JSON
     * How it works:
     *   - OrderItem serializes its data
     *   - When it tries to serialize the Order reference, it stops (ignores it)
     *   - This breaks the infinite loop
     *
     * @ManyToOne - MANY OrderItems belong to ONE Order
     *   - Foreign key "order_id" in order_items table
     *
     * @JoinColumn - Defines the foreign key column
     *   - name = "order_id" - Column name in database
     *   - nullable = false - Each order item must belong to an order
     *
     * This is the "child" side - Order is the "parent"
     */
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /*
     * @ManyToOne - MANY OrderItems can reference ONE Product
     *   - Foreign key "product_id" in order_items table
     *
     * @JoinColumn - Defines the foreign key column
     *   - name = "product_id" - Column name in database
     *   - nullable = false - Each order item must reference a product
     *
     * Why no @JsonBackReference here? Product uses @JsonIgnore
     *   - Product doesn't need to serialize its orderItems (using @JsonIgnore)
     *   - So no infinite loop between OrderItem and Product
     */
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