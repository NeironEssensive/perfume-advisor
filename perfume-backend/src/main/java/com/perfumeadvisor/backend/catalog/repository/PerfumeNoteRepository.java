package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.PerfumeNote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PerfumeNoteRepository extends JpaRepository<PerfumeNote, Long> {

    @Query(
            "select pn.perfume.id, pn.pyramidPosition, pn.note.name from PerfumeNote pn "
                    + "where pn.perfume.id in :perfumeIds")
    List<Object[]> findNotesByPerfumeIds(@Param("perfumeIds") List<Long> perfumeIds);
}
