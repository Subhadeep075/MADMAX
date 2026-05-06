package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findFirstByActiveTrueOrderByIdAsc();
    Optional<Shop> findFirstByOrderByIdAsc();
}
