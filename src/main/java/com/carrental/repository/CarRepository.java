package com.carrental.repository;

import com.carrental.enums.CarType;
import com.carrental.model.Car;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM Car c
        WHERE c.type = :type
        AND NOT EXISTS (
            SELECT 1 FROM Reservation r
            WHERE r.car = c
            AND r.startDate < :endDate
            AND r.endDate > :startDate
        )
        ORDER BY c.id ASC
        LIMIT 1
    """)
    Optional<Car> findAvailableCar(
            @Param("type") CarType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
