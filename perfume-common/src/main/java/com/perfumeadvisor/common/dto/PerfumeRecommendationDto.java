package com.perfumeadvisor.common.dto;

import java.math.BigDecimal;
import java.util.List;

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
        Integer occasionScore,
        List<String> topNotes,
        List<String> middleNotes,
        List<String> baseNotes,
        List<AccordDto> accords) {
}
