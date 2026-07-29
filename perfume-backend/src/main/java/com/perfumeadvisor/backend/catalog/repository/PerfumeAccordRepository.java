package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.PerfumeAccord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PerfumeAccordRepository extends JpaRepository<PerfumeAccord, Long> {

    @Query("select pa.perfume.id, pa.accord.id, pa.strength from PerfumeAccord pa")
    List<Object[]> findAllPerfumeAccordTriples();

    @Query(
            "select pa.perfume.id, pa.accord.name, pa.strength from PerfumeAccord pa "
                    + "where pa.perfume.id in :perfumeIds order by pa.strength desc")
    List<Object[]> findAccordNamesByPerfumeIds(@Param("perfumeIds") List<Long> perfumeIds);
}
