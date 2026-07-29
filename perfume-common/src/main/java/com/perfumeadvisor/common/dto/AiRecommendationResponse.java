package com.perfumeadvisor.common.dto;

import java.util.List;

public record AiRecommendationResponse(List<PerfumeRecommendationDto> perfumes, String explanation) {
}
