package com.wehavecrown.creatorstore.repositories;

import com.wehavecrown.creatorstore.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
