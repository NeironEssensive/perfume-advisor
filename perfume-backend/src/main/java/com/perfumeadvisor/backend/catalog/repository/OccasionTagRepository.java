package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.OccasionTag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OccasionTagRepository extends JpaRepository<OccasionTag, Long> {

    Optional<OccasionTag> findByNameIgnoreCase(String name);
}
