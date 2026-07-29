package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.RecommendationSort;
import java.math.BigDecimal;
import java.util.Comparator;

public final class SortSupport {

    private SortSupport() {
    }

    public static boolean isServerSortable(SortOption option) {
        return option == SortOption.RATING_DESC || option == SortOption.RATING_ASC
                || option == SortOption.PRICE_DESC || option == SortOption.PRICE_ASC;
    }

    public static RecommendationSort toServerSort(SortOption option) {
        return switch (option) {
            case RATING_DESC -> RecommendationSort.RATING_DESC;
            case RATING_ASC -> RecommendationSort.RATING_ASC;
            case PRICE_DESC -> RecommendationSort.PRICE_DESC;
            case PRICE_ASC -> RecommendationSort.PRICE_ASC;
            default -> RecommendationSort.RELEVANCE;
        };
    }

    public static Comparator<PerfumeRecommendationDto> comparatorFor(SortOption option) {
        return switch (option) {
            case RATING_DESC -> Comparator.comparing(
                    (PerfumeRecommendationDto p) -> p.ratingValue() == null ? -1 : p.ratingValue()).reversed();
            case RATING_ASC -> Comparator.comparing(
                    (PerfumeRecommendationDto p) -> p.ratingValue() == null ? Double.MAX_VALUE : p.ratingValue());
            case PRICE_DESC -> Comparator.comparing(
                    (PerfumeRecommendationDto p) -> p.price() == null ? BigDecimal.ZERO : p.price())
                    .reversed();
            case PRICE_ASC -> Comparator.comparing((PerfumeRecommendationDto p) ->
                    p.price() == null ? new BigDecimal("999999") : p.price());
            case SEASON_SCORE -> Comparator.comparing(
                    (PerfumeRecommendationDto p) -> p.seasonScore() == null ? -1 : p.seasonScore()).reversed();
            case OCCASION_SCORE -> Comparator.comparing(
                    (PerfumeRecommendationDto p) -> p.occasionScore() == null ? -1 : p.occasionScore()).reversed();
            case NAME -> Comparator.comparing(PerfumeRecommendationDto::name, String.CASE_INSENSITIVE_ORDER);
            case RELEVANCE -> (a, b) -> 0;
        };
    }
}
