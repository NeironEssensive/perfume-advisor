package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.PerfumeAccord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeAccordRepository extends JpaRepository<PerfumeAccord, Long> {
}
