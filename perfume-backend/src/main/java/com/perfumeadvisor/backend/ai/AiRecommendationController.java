package com.perfumeadvisor.backend.ai;

import com.perfumeadvisor.common.dto.AiRecommendationRequest;
import com.perfumeadvisor.common.dto.AiRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-recommendations")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @PostMapping
    public AiRecommendationResponse recommend(@RequestBody AiRecommendationRequest request) {
        return aiRecommendationService.recommend(request);
    }
}
