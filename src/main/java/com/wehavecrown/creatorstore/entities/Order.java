/*
* ENTITY LAYER - Database table mapping
*
* This represents the ORDERS table in your database
* Each order tracks a customer's purchase with multiple items
* */

package com.wehavecrown.creatorstore.entities;

// Jackson annotation - manages serialization to prevent infinite loops
// @JsonManagedReference is the "parent" side of a bidirectional relationship
import com.fasterxml.jackson.annotation.JsonManagedReference;

// JPA annotations for database mapping
import jakarta.persistence.*;

// Lombok - reduces boilerplate code
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Java types
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/*
* @Entity - Maps to "orders" table in database
* @Table - Explicitly specifies table name
* */
@Entity
@Table(name = "orders")

/*
* Lombok annotations:
* @Getter/@Setter - Generate getters and setters for all fields
* @AllArgsConstructor - Constructor with all fields
* @NoArgsConstructor - Empty constructor (required by JPA)r
* */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    /*
    * Primary key - auto-incremented by database
    * */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    * @Column - customize column properties
    *   name = "customer_name" - Database column name (different from field name)
    *   nullable = false - Database constraint: cannot be NULL
    *
    *   customerName maps to "customer_name" column in DB
    * */
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    /*
    * Status field tracks order lifecycle:
    * common status values: "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
    * */
    @Column(nullable = false)
    private String status;

    /*
    * totalPrice - Total cost of ALL items in this order
    * Calculated when order is created (sum of each item's price * quantity)
    * Stored as BigDecimal for monetary precision
    * */
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    /*
    * @JsonManagedReference - The "forward" part of the relationship
    * Paired with @JsonBackReference in OrderItem
    *
    * Purpose: Prevents infinite recursion when serializing to JSON
    * How it works:
    *   - Order (this) is serialized normally with its orderItems
    *   - When OrderItem tries to serialize back to Order, it stops
    *
    * @OneToMany - One Order can have MANY OrderItems
    *   mappedBy = "order" - Points to the 'order' field in OrderItem
    *
    *   cascade = CascadeType.ALL - All operations cascade to child entities
    *   - if you save/delete/update this Order, the same happens to all OrderItem
    *   - Example: order.Repository.save(order) will also save all orderItems
    * */
    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    /*
    * createdAt - Timestamp when order was created
    * Auto-populated by @PrePersist method below
    * */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /*
    * @PrePersist - JPA lifecycle callback
    * This method is automatically called BEFORE the entity is saved to database
    *
    * Used to auto-set the createdAt timestamp
    * No need to manually set when creating orders
    * */
    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

}

/*
* RELATIONSHIP SUMMARY
*
* Order (1) ----< (M) OrderItem
* - One order has many order items
* - Order is the parent or owning side
* - CascadeType.ALL means operations on Order affect OrderItems
*
*
* DATA FLOW EXAMPLE:
*
* 1. Customer places order:
*   Order order = new Order();
*   order.setCustomerName("John");
*   order.setStatus("PENDING");
*
*   OrderItem item1 = new OrderItem();
*   item1.setProduct(product1);
*   item1.setQuantity(2);
*
*   order.setOrderItems(List.of(item1));
*   order.setTotalPrice(new BigDecimal("99.98"));
*
* 2. Save order:
*    orderRepository.save(order);
*    -> Saves Order first
*    -> CascadeType.ALL also saves both OrderItems
*    -> createdAt auto-set by @PrePersist
*
* 3. Delete order:
*    orderRepository.delete(order);
*    -> CascadeType.ALL also deletes all OrderItems
*
* KEY DIFFERENCES FROM PRODUCT ENTITY:
*
* 1. Order uses @JsonManagedReference (not @JsonIgnore like Product)
 *    - Product used @JsonIgnore because it's the "many" side
 *    - Order uses @JsonManagedReference because it's the "one" side
 *
 * 2. Order has @PrePersist for auto-timestamping
 *    - Product didn't need this
 *
 * 3. Order has CascadeType.ALL
 *    - Saving order automatically saves items
 *    - Product doesn't cascade (no need to save products when saving order items)
 *
 * 4. Order doesn't use @Builder (unlike Product)
 *    - Builder pattern is optional
 *    - Could be added if needed: @Builder
* */

/*
 * DATABASE TABLE STRUCTURE: orders
 *
 * COLUMNS:
 *   - id (BIGINT, PK, AUTO_INCREMENT)
 *   - customer_name (VARCHAR, NOT NULL)
 *   - customer_email (VARCHAR, NOT NULL)
 *   - status (VARCHAR, NOT NULL)
 *   - total_price (DECIMAL, NOT NULL)
 *   - created_at (TIMESTAMP)
 *
 * FOREIGN KEY RELATIONSHIP:
 *   - No direct FK in orders table
 *   - FK is in order_items table (order_id column)
 *   - This is a unidirectional relationship at database level
 */

/*
 * COMMON OPERATIONS:
 *
 * 1. Create order:
 *    Order order = new Order();
 *    order.setCustomerName("John Doe");
 *    order.setCustomerEmail("john@email.com");
 *    order.setStatus("PENDING");
 *    order.setTotalPrice(new BigDecimal("149.97"));
 *    order.setOrderItems(items);
 *    orderService.createOrder(order);
 *
 * 2. Update status:
 *    Order order = orderService.getOrderById(1L);
 *    order.setStatus("SHIPPED");
 *    orderService.updateOrder(order);
 *
 * 3. Get all items in order:
 *    List<OrderItem> items = order.getOrderItems();
 *    for (OrderItem item : items) {
 *        System.out.println(item.getProduct().getName());
 *    }
 */