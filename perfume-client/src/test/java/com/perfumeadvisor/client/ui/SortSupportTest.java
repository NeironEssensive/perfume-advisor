package com.perfumeadvisor.client.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.RecommendationSort;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class SortSupportTest {

    private PerfumeRecommendationDto perfume(long id, String name, Double rating, BigDecimal price) {
        return new PerfumeRecommendationDto(id, "Brand", name, null, null, price, rating, 10, "UNISEX",
                null, null, List.of(), List.of(), List.of(), List.of());
    }

    @Test
    void onlyRatingAndPriceOptionsAreServerSortable() {
        assertThat(SortSupport.isServerSortable(SortOption.RATING_DESC)).isTrue();
        assertThat(SortSupport.isServerSortable(SortOption.RATING_ASC)).isTrue();
        assertThat(SortSupport.isServerSortable(SortOption.PRICE_DESC)).isTrue();
        assertThat(SortSupport.isServerSortable(SortOption.PRICE_ASC)).isTrue();
        assertThat(SortSupport.isServerSortable(SortOption.NAME)).isFalse();
        assertThat(SortSupport.isServerSortable(SortOption.SEASON_SCORE)).isFalse();
        assertThat(SortSupport.isServerSortable(SortOption.OCCASION_SCORE)).isFalse();
        assertThat(SortSupport.isServerSortable(SortOption.RELEVANCE)).isFalse();
    }

    @Test
    void mapsSortOptionsToTheMatchingServerSort() {
        assertThat(SortSupport.toServerSort(SortOption.RATING_DESC)).isEqualTo(RecommendationSort.RATING_DESC);
        assertThat(SortSupport.toServerSort(SortOption.PRICE_ASC)).isEqualTo(RecommendationSort.PRICE_ASC);
        assertThat(SortSupport.toServerSort(SortOption.NAME)).isEqualTo(RecommendationSort.RELEVANCE);
    }

    @Test
    void sortsByRatingDescendingWithNullsLast() {
        List<PerfumeRecommendationDto> list = List.of(
                perfume(1, "A", 3.5, null),
                perfume(2, "B", null, null),
                perfume(3, "C", 4.8, null));

        List<PerfumeRecommendationDto> sorted =
                list.stream().sorted(SortSupport.comparatorFor(SortOption.RATING_DESC)).toList();

        assertThat(sorted).extracting(PerfumeRecommendationDto::id).containsExactly(3L, 1L, 2L);
    }

    @Test
    void sortsByPriceAscendingWithUnpricedPerfumesLast() {
        List<PerfumeRecommendationDto> list = List.of(
                perfume(1, "A", null, new BigDecimal("5000")),
                perfume(2, "B", null, null),
                perfume(3, "C", null, new BigDecimal("1000")));

        List<PerfumeRecommendationDto> sorted =
                list.stream().sorted(SortSupport.comparatorFor(SortOption.PRICE_ASC)).toList();

        assertThat(sorted).extracting(PerfumeRecommendationDto::id).containsExactly(3L, 1L, 2L);
    }

    @Test
    void sortsByNameCaseInsensitively() {
        List<PerfumeRecommendationDto> list = List.of(
                perfume(1, "zephyr", null, null),
                perfume(2, "Amber", null, null));

        List<PerfumeRecommendationDto> sorted =
                list.stream().sorted(SortSupport.comparatorFor(SortOption.NAME)).toList();

        assertThat(sorted).extracting(PerfumeRecommendationDto::id).containsExactly(2L, 1L);
    }
}
