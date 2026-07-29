package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.Brand;
import com.perfumeadvisor.backend.catalog.domain.Perfume;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeRepository extends JpaRepository<Perfume, Long> {

    Optional<Perfume> findByNameIgnoreCaseAndBrand(String name, Brand brand);
}
