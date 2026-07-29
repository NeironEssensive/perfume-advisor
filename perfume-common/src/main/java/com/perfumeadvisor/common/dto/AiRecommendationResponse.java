package com.perfumeadvisor.common.dto;

public record AiRecommendationResponse(PerfumeRecommendationDto perfume, String explanation) {
}
