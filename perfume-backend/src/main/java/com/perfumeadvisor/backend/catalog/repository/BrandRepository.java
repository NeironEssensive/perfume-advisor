package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.Brand;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByNameIgnoreCase(String name);
}
