package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.Accord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccordRepository extends JpaRepository<Accord, Long> {

    Optional<Accord> findByNameIgnoreCase(String name);
}
