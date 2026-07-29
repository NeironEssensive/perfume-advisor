package com.perfumeadvisor.backend.catalog.repository;

import com.perfumeadvisor.backend.catalog.domain.Brand;
import com.perfumeadvisor.backend.catalog.domain.Perfume;
import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PerfumeRepository extends JpaRepository<Perfume, Long> {

    Optional<Perfume> findByNameIgnoreCaseAndBrand(String name, Brand brand);

    @Query("select p.id from Perfume p")
    List<Long> findAllIds();

    @Query("""
            select p, pss.score, pos.score
            from Perfume p
            join PerfumeSeasonScore pss on pss.perfume = p and pss.season = :season
            join PerfumeOccasionScore pos on pos.perfume = p and pos.occasion = :occasion
            where (:gender is null or p.gender = :gender or p.gender = com.perfumeadvisor.common.enums.Gender.UNISEX)
            order by (pss.score + pos.score + coalesce(p.ratingValue, 0) * 10) desc
            """)
    List<Object[]> findRecommendations(
            @Param("season") Season season,
            @Param("occasion") Occasion occasion,
            @Param("gender") Gender gender,
            Pageable pageable);

    @Query("""
            select p from Perfume p
            where lower(p.brand.name) like lower(concat('%', :query, '%'))
               or lower(p.name) like lower(concat('%', :query, '%'))
            order by coalesce(p.ratingValue, 0) desc
            """)
    List<Perfume> searchByBrandOrName(@Param("query") String query, Pageable pageable);
}
