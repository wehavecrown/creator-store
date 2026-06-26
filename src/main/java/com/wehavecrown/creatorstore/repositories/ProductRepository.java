package com.wehavecrown.creatorstore.repositories;

import com.wehavecrown.creatorstore.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>{
}
