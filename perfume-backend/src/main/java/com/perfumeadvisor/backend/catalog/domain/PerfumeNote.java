package com.perfumeadvisor.backend.catalog.domain;

import com.perfumeadvisor.common.enums.PyramidPosition;
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
 * Связь парфюма с нотой и её позицией в пирамиде (верхняя/средняя/базовая).
 */
@Entity
@Table(
        name = "perfume_note",
        uniqueConstraints = @UniqueConstraint(columnNames = {"perfume_id", "note_id", "pyramid_position"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "pyramidPosition"})
public class PerfumeNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfume_id", nullable = false)
    private Perfume perfume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Enumerated(EnumType.STRING)
    @Column(name = "pyramid_position", nullable = false, length = 10)
    private PyramidPosition pyramidPosition;
}
