package com.perfumeadvisor.backend.catalog.domain;

import com.perfumeadvisor.common.enums.Occasion;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "perfume_occasion_score",
        uniqueConstraints = @UniqueConstraint(columnNames = {"perfume_id", "occasion"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "occasion", "score"})
public class PerfumeOccasionScore {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "perfume_occasion_score_seq")
    @SequenceGenerator(
            name = "perfume_occasion_score_seq",
            sequenceName = "perfume_occasion_score_id_seq",
            allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfume_id", nullable = false)
    private Perfume perfume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Occasion occasion;

    @Column(nullable = false)
    private Integer score;
}
