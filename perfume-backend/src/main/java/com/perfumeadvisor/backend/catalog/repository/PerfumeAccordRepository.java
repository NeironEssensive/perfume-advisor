package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.PerfumeAccord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PerfumeAccordRepository extends JpaRepository<PerfumeAccord, Long> {

    @Query("select pa.perfume.id, pa.accord.id, pa.strength from PerfumeAccord pa")
    List<Object[]> findAllPerfumeAccordTriples();
}
