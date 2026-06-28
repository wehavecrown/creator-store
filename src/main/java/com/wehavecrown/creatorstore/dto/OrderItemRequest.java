// A DTO is a dumb object used purely to move data between the client and the server.
// No business logic, just fields, getters, setters, or Java Records.

package com.wehavecrown.creatorstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;


}

// DTO (Data Transfer Object)
// Entity
// Repository
// Service
// Controller

/*
*
*
* To build scalable, maintainable applications in Java Spring Boot, we use a layered architecture. Each component has a single, dedicated responsibility.

Here is the breakdown of **DTOs, Entities, Controllers, Services, and Repositories**, explaining the theory behind them and how they look in code.

---

## The Architecture Lifecycle

When a request hits your server, it flows through the layers like this:
`HTTP Request` ➔ **Controller** ➔ **Service** ➔ **Repository** ➔ `Database`
The data changes shape along the way using **DTOs** and **Entities**.

```
[ Client ]
   │  ▲  (DTOs: Data Transfer Objects)
   ▼  │
[ Controller Layer ]  <-- Handles HTTP requests/responses
   │  ▲  (DTOs)
   ▼  │
[ Service Layer ]     <-- Handles Business Logic & Transactions
   │  ▲  (Entities: Database Models)
   ▼  │
[ Repository Layer ]  <-- Handles Database Queries (SQL/NoSQL)
   │  ▲
   ▼  │
[ Database ]

```

---

## 1. DTO (Data Transfer Object)

### 💡 The Theory

A **DTO** is a dumb object used purely to move data between the client and the server.

* **Why use it?** You rarely want to expose your exact database structure to the frontend. DTOs allow you to hide sensitive data (like passwords), aggregate data from multiple sources, and validate incoming requests before they hit your business logic.
* **Characteristics:** No business logic, just fields, getters, setters, or Java Records.

### 💻 The Code

```java
package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Using Java Records is perfect for DTOs as they are immutable
public record UserRegistrationDto(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20)
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password
) {}

```

---

## 2. Entity

### 💡 The Theory

An **Entity** is a direct mapping to a table in your database using an ORM (Object-Relational Mapping) framework like Hibernate/JPA.

* **Why use it?** It allows you to interact with your database using Java objects instead of writing raw SQL queries.
* **Characteristics:** Annotated with `@Entity`, contains a primary key (`@Id`), and reflects the exact schema of your database table.

### 💻 The Code

```java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter // Using Lombok to keep code clean
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // This will store the hashed password
}

```

---

## 3. Repository

### 💡 The Theory

The **Repository Layer** (Data Access Layer) is responsible for interacting directly with the database.

* **Why use it?** Spring Data JPA provides out-of-the-box CRUD operations without you needing to write implementation code. You just define the interface.
* **Characteristics:** Inherits from `JpaRepository`. You can create custom queries simply by naming methods correctly (Query Methods).

### 💻 The Code

```java
package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring automatically generates the SQL for this based on the method name!
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}

```

---

## 4. Service

### 💡 The Theory

The **Service Layer** is the heart of your application. This is where your **business logic** lives.

* **Why use it?** The Controller shouldn't care *how* a user is saved; it only cares about handling the HTTP request. The Service layer handles things like password hashing, checking if an email is already taken, orchestrating multiple repository calls, and managing transactions.
* **Characteristics:** Annotated with `@Service`. This is where you inject your repositories and use `@Transactional` to ensure data integrity.

### 💻 The Code

```java
package com.example.demo.service;

import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    // In a real app, you'd inject a BCryptPasswordEncoder here

    // Constructor injection (Best practice)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerNewUser(UserRegistrationDto dto) {
        // 1. Business Logic: Check if email exists
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // 2. Mapping: DTO -> Entity
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(dto.password()); // In real life: passwordEncoder.encode(dto.password())

        // 3. Database Interaction
        return userRepository.save(user);
    }
}

```

---

## 5. Controller

### 💡 The Theory

The **Controller Layer** (Presentation Layer) is the entry point of your application. It exposes REST API endpoints to the outside world.

* **Why use it?** It handles routing, unmarshalling JSON payload into a DTO, triggering request validation, and returning the proper HTTP status codes (like `201 Created` or `400 Bad Request`).
* **Characteristics:** Annotated with `@RestController`. It should remain "skinny"—meaning **zero** business logic or direct database queries should happen here. It just delegates to the Service layer.

### 💻 The Code

```java
package com.example.demo.controller;

import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // Delegates the actual work to the service layer
        User savedUser = userService.registerNewUser(registrationDto);

        // Returns the response with HTTP Status 201 (Created)
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}

```

---

## Summary Cheat Sheet

| Component | Responsibility | Annotations | Relies On |
| --- | --- | --- | --- |
| **Controller** | Handles HTTP requests, routes, and JSON parsing. | `@RestController`, `@PostMapping`, etc. | Service |
| **DTO** | Carries clean, validated data between layers/network. | `@NotBlank`, `@Email` (JPA-free) | *None* |
| **Service** | Executes business logic, permissions, and transactions. | `@Service`, `@Transactional` | Repository, Other Services |
| **Repository** | Runs database queries (Abstraction over SQL). | `@Repository` | *None (Spring Data)* |
| **Entity** | Maps a Java Object directly to a Database Table. | `@Entity`, `@Table`, `@Id` | *None* |
* */
