package com.perfumeadvisor.common.dto;

import java.math.BigDecimal;

public record PerfumeRecommendationDto(
        Long id,
        String brand,
        String name,
        String imageUrl,
        String sourceUrl,
        BigDecimal price,
        Double ratingValue,
        Integer ratingCount,
        String gender,
        Integer seasonScore,
        Integer occasionScore) {
}
