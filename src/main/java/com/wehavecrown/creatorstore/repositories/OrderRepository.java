// Repository Layer (Data Access Layer) is responsible for interacting directly with the database.
// Inherits from JpaRepository. You can create custom queries simply by naming methods correctly (Query Methods).

package com.wehavecrown.creatorstore.repositories;

import com.wehavecrown.creatorstore.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
