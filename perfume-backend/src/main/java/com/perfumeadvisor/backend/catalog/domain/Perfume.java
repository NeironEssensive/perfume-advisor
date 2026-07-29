package com.perfumeadvisor.backend.catalog.domain;

import com.perfumeadvisor.common.enums.Concentration;
import com.perfumeadvisor.common.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "perfume", uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public class Perfume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Concentration concentration;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "rating_value")
    private Double ratingValue;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "perfume_occasion",
            joinColumns = @JoinColumn(name = "perfume_id"),
            inverseJoinColumns = @JoinColumn(name = "occasion_tag_id"))
    @Builder.Default
    private Set<OccasionTag> occasions = new HashSet<>();
}
