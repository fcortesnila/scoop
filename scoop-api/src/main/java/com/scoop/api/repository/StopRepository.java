package com.scoop.api.repository;

import com.scoop.api.entity.TruckStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StopRepository extends JpaRepository<TruckStop, Long> {
    Optional<TruckStop> findFirstByEndedAtIsNullOrderByStartedAtDesc();

    @Query(value = """
            SELECT s.id AS stopId, s.name AS stopName, s.latitude AS latitude, s.longitude AS longitude,
                   DAYOFWEEK(o.created_at) AS dayOfWeek, HOUR(o.created_at) AS hour,
                   COALESCE(SUM(o.total), 0) AS orderValue, COUNT(o.id) AS orderCount,
                   GREATEST(TIMESTAMPDIFF(MINUTE, s.started_at, COALESCE(s.ended_at, NOW())), 1) AS stopMinutes,
                   s.started_at AS lastVisited
            FROM stops s
            LEFT JOIN orders o ON o.stop_id = s.id AND o.status = 'SERVED'
            GROUP BY s.id, s.name, s.latitude, s.longitude, s.started_at, s.ended_at,
                     DAYOFWEEK(o.created_at), HOUR(o.created_at)
            ORDER BY s.name, dayOfWeek, hour
            """, nativeQuery = true)
    java.util.List<RouteMetricRow> findRouteMetrics();

    interface RouteMetricRow {
        Long getStopId();
        String getStopName();
        java.math.BigDecimal getLatitude();
        java.math.BigDecimal getLongitude();
        Integer getDayOfWeek();
        Integer getHour();
        java.math.BigDecimal getOrderValue();
        Long getOrderCount();
        Long getStopMinutes();
        java.time.LocalDateTime getLastVisited();
    }
}
