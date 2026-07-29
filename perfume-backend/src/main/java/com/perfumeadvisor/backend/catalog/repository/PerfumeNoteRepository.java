package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.PerfumeNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeNoteRepository extends JpaRepository<PerfumeNote, Long> {
}
