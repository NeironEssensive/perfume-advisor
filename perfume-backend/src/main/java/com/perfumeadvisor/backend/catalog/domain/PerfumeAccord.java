package com.perfumeadvisor.backend.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Связь парфюма с аккордом и его силой (0-100) — как ширина полоски на Fragrantica.
 */
@Entity
@Table(name = "perfume_accord", uniqueConstraints = @UniqueConstraint(columnNames = {"perfume_id", "accord_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "strength"})
public class PerfumeAccord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfume_id", nullable = false)
    private Perfume perfume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accord_id", nullable = false)
    private Accord accord;

    @Column(nullable = false)
    private Integer strength;
}
