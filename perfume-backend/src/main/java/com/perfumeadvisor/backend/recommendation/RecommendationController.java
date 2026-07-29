package com.perfumeadvisor.backend.recommendation;

import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.RecommendationSort;
import com.perfumeadvisor.common.enums.Season;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public List<PerfumeRecommendationDto> recommend(
            @RequestParam Occasion occasion,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Season season,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "RELEVANCE") RecommendationSort sort) {
        Season effectiveSeason = season != null ? season : SeasonResolver.currentSeason();
        return recommendationService.recommend(effectiveSeason, occasion, gender, limit, sort);
    }

    @GetMapping("/search")
    public List<PerfumeRecommendationDto> search(
            @RequestParam String query, @RequestParam(defaultValue = "50") int limit) {
        return recommendationService.search(query, limit);
    }
}
