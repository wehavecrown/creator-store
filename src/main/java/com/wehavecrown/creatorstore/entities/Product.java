/*
* ENTITY LAYER
*
* - Direct mapping to a database table (PRODUCT table)
* - Each instance represents one row in the table
* - Uses JPA (Java Persistence API) annotations to define table structure
* - Part of ORM (Object-Relational Mapping) - maps Java objects to database tables
*
* */

// Package declaration - matches folder structure
package com.wehavecrown.creatorstore.entities;

// Jackson annotation prevents infinite recursion when serializing to JSON
// (OrderItem references Product, Product references OrderItem - this breaks the loop)
import com.fasterxml.jackson.annotation.JsonIgnore;

// JPA annotations - define how this class maps to database
import jakarta.persistence.*;

// Validation annotations - ensure data integrity before saving to database
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Lombok annotations - generate boilerplate code automatically
import lombok.*;

// Java types
import java.math.BigDecimal; // For precise monetary values
import java.util.List;

/*
* @Entity - Marks this as a JPA entity (will be mapped to a database table)
* @Table - Specifies the table name in database (defaults to class name if omitted)
* */
@Entity
@Table(name = "products")

/*
* Lombok annotations that generate code at compile time
* @Getter - Generates getter methods for all fields
* @Setter - Generate setter methods for all fields
* @AllArgsConstructor - Generates constructor with all fields as parameters
* @NoArgsConstructor - Generates empty constructor (required by JPA)
* @Builder - Implements Builder pattern for clean object creation
*            Example: Product.builder().name("iPhone").price(999.99).build()
* */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    /*
    * @Id - Primary key of the table
    * @GeneratedValue - How the ID is generated
    *       strategy = GenerationType.IDENTITY -> Auto-increment (MySQL/PostgreSQL)
    *       other strategies: AUTO, SEQUENCE, TABLE
    * */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    * @NotBlank - Validation: field cannot be null, empty or whitespace
    * @Column(nullable = false) - Database constraint: column cannot be NULL
    * Both validations work together (JPA-level and database level)
    * */
    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    /*
     * No validation annotations - this field is optional
     * Can be null in database
     * */
    private String description;

    private String category;

    /*
    * @NotNull - Validation: field cannot be null
    * @DecimalMin - Validation: value must be > 0.0
    *   inclusive = false means strictly greater than zero (0.01 is valid, 0.0 is not)
    *
    * BigDecimal is preferred for money (avoids floating-point precision issues)
    * */
    @NotNull(message = "Price")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Column(nullable = false)
    private BigDecimal price;

    /*
    * @NotNull - Validation: field cannot be null
    * @Min - Validation: stock cannot be negative (less than zero)
    * */
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be less than 0")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    /*
    * @JsonIgnore - prevents infinite recursion when converting to JSON
    *   Product has a list of OrderItems
    *   Each OrderItem has Product reference
    *   Without @JsonIgnore: Product -> OrderItem -> Product -> OrderItem...(infinite)
    *
    * @OneToMany - One product can have many OrderItems
    *   mappedBy = "product" -> Points to the 'product' field in OrderItem
    *   This is the inverse side (OrderItem owns the relationship with @ManyToOne)
    * */
    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;
}

/*

* QUICK SUMMARY - Product Entity:
*
* DATABASE TABLE: products
* COLUMNS:
* - id (BIGINT, PK, auto-increment)
* - name (VARCHAR, NOT NULL)
* - description (VARCHAR, nullable)
* - category (VARCHAR, nullable)
* - price (DECIMAL, NOT NULL, > 0)
* - stock_quantity (INT, NOT NULL, >= 0)
*
* RELATIONSHIPS:
* - One-to-Many with OrderItem (one product can be in many orders)
*
* VALIDATION RULES (before saving to DB)
* - name: required, not blank
* - price: required, must be > 0
* - stockQuantity: required, must be >= 0
*
* KEY CONCEPTS:
* 1. JPA Annotations (@Entity, @Table, @Id, @GeneratedValue) - Database mapping
* 2. Validation Annotations (@NotNull, @NotBlank, @Min, @DecimalMin) - Data integrity
* 3. Lombok Annotations (@Getter, @Setter, @Builder) - Reduce boilerplate code
* 4. Jackson Annotation (@JsonIgnore) - Controls JSON serialization
* 5. Relationship Mapping (@OneToMany) - Defines database relationships
*
* */
