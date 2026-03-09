package com.carrental.repository;

import com.carrental.model.Renter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RenterRepository extends JpaRepository<Renter, UUID> {
    Optional<Renter> findByEmail(String email);
}
