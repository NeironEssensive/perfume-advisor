package com.perfumeadvisor.client.api;

import com.perfumeadvisor.common.dto.AiRecommendationRequest;
import com.perfumeadvisor.common.dto.AiRecommendationResponse;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.RecommendationSort;
import com.perfumeadvisor.common.enums.Season;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public class ApiClient {

    private final RestClient restClient;

    public ApiClient(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<PerfumeRecommendationDto> recommend(Occasion occasion, Gender gender, Season season, int limit) {
        return recommend(occasion, gender, season, limit, RecommendationSort.RELEVANCE);
    }

    public List<PerfumeRecommendationDto> recommend(
            Occasion occasion, Gender gender, Season season, int limit, RecommendationSort sort) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/recommendations")
                            .queryParam("occasion", occasion)
                            .queryParam("limit", limit)
                            .queryParam("sort", sort);
                    if (gender != null) {
                        uriBuilder.queryParam("gender", gender);
                    }
                    if (season != null) {
                        uriBuilder.queryParam("season", season);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<PerfumeRecommendationDto>>() {});
    }

    public List<PerfumeRecommendationDto> search(String query, int limit) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/recommendations/search")
                        .queryParam("query", query)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<PerfumeRecommendationDto>>() {});
    }

    public AiRecommendationResponse aiRecommend(AiRecommendationRequest request) {
        return restClient.post()
                .uri("/api/ai-recommendations")
                .body(request)
                .retrieve()
                .body(AiRecommendationResponse.class);
    }
}
