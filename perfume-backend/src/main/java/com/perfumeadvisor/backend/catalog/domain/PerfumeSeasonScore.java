package com.perfumeadvisor.backend.catalog.domain;

import com.perfumeadvisor.common.enums.Season;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Насколько парфюм уместен в конкретный сезон (0-100) — используется фильтрацией
 * перед тем, как отдать кандидатов в LLM для финальной рекомендации.
 */
@Entity
@Table(name = "perfume_season_score", uniqueConstraints = @UniqueConstraint(columnNames = {"perfume_id", "season"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "season", "score"})
public class PerfumeSeasonScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfume_id", nullable = false)
    private Perfume perfume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Season season;

    @Column(nullable = false)
    private Integer score;
}
