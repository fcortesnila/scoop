package com.scoop.api.repository;

import com.scoop.api.entity.OrderStatus;
import com.scoop.api.entity.ScoopOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<ScoopOrder, Long> {
    List<ScoopOrder> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
    List<ScoopOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ScoopOrder> findByIdAndUserId(Long id, Long userId);
    List<ScoopOrder> findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            OrderStatus status, LocalDateTime start, LocalDateTime end);
}
