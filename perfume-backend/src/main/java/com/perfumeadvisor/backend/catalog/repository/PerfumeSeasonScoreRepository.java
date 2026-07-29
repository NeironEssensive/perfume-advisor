package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.PerfumeSeasonScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeSeasonScoreRepository extends JpaRepository<PerfumeSeasonScore, Long> {
}
