package com.perfumeadvisor.common.dto;

import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;

public record AiRecommendationRequest(String description, Occasion occasion, Gender gender, Season season) {
}
