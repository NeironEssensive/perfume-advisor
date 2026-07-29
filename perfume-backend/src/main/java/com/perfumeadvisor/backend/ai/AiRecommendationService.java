package com.perfumeadvisor.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfumeadvisor.backend.catalog.repository.PerfumeAccordRepository;
import com.perfumeadvisor.backend.recommendation.RecommendationService;
import com.perfumeadvisor.backend.recommendation.SeasonResolver;
import com.perfumeadvisor.common.dto.AiRecommendationRequest;
import com.perfumeadvisor.common.dto.AiRecommendationResponse;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.Season;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiRecommendationService {

    private static final int CANDIDATE_LIMIT = 20;

    private final RecommendationService recommendationService;
    private final PerfumeAccordRepository perfumeAccordRepository;
    private final RestClient ollamaRestClient;
    private final ObjectMapper objectMapper;
    private final String ollamaModel;

    public AiRecommendationService(
            RecommendationService recommendationService,
            PerfumeAccordRepository perfumeAccordRepository,
            RestClient ollamaRestClient,
            ObjectMapper objectMapper,
            @Value("${ollama.model}") String ollamaModel) {
        this.recommendationService = recommendationService;
        this.perfumeAccordRepository = perfumeAccordRepository;
        this.ollamaRestClient = ollamaRestClient;
        this.objectMapper = objectMapper;
        this.ollamaModel = ollamaModel;
    }

    public AiRecommendationResponse recommend(AiRecommendationRequest request) {
        Season season = request.season() != null ? request.season() : SeasonResolver.currentSeason();
        List<PerfumeRecommendationDto> candidates =
                recommendationService.recommend(season, request.occasion(), request.gender(), CANDIDATE_LIMIT);

        if (candidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Нет подходящих кандидатов");
        }

        Map<Long, List<String>> accordsByPerfumeId = loadAccordNames(candidates);
        String prompt = buildPrompt(request, season, candidates, accordsByPerfumeId);

        AiPick pick = callOllama(prompt);

        PerfumeRecommendationDto chosen = candidates.stream()
                .filter(c -> c.id() == pick.perfumeId())
                .findFirst()
                .orElse(candidates.get(0));

        return new AiRecommendationResponse(chosen, pick.explanation());
    }

    private AiPick callOllama(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", ollamaModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "format", "json",
                "stream", false);

        Map<?, ?> response = ollamaRestClient
                .post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        Map<?, ?> message = (Map<?, ?>) response.get("message");
        String content = (String) message.get("content");

        try {
            return objectMapper.readValue(content, AiPick.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Ollama вернула невалидный JSON: " + content, e);
        }
    }

    private Map<Long, List<String>> loadAccordNames(List<PerfumeRecommendationDto> candidates) {
        List<Long> ids = candidates.stream().map(PerfumeRecommendationDto::id).toList();
        Map<Long, List<String>> result = new HashMap<>();
        for (Object[] row : perfumeAccordRepository.findAccordNamesByPerfumeIds(ids)) {
            Long perfumeId = (Long) row[0];
            String accordName = (String) row[1];
            result.computeIfAbsent(perfumeId, k -> new ArrayList<>()).add(accordName);
        }
        return result;
    }

    private String buildPrompt(
            AiRecommendationRequest request,
            Season season,
            List<PerfumeRecommendationDto> candidates,
            Map<Long, List<String>> accordsByPerfumeId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ты — эксперт-парфюмер, который подбирает аромат под конкретного человека и случай.\n");
        sb.append("О человеке и запросе: ").append(request.description()).append("\n");
        sb.append("Повод: ").append(request.occasion()).append("\n");
        sb.append("Сезон: ").append(season).append("\n");
        sb.append("Ниже список кандидатов (уже отфильтрованы по сезону и поводу). ");
        sb.append("Выбери ОДИН, который лучше всего подходит под описание человека.\n\n");

        for (PerfumeRecommendationDto candidate : candidates) {
            List<String> accords = accordsByPerfumeId.getOrDefault(candidate.id(), List.of());
            sb.append("id=").append(candidate.id())
                    .append(", ").append(candidate.brand()).append(" — ").append(candidate.name())
                    .append(", пол: ").append(candidate.gender())
                    .append(", аккорды: ").append(String.join(", ", accords))
                    .append(", рейтинг: ").append(candidate.ratingValue())
                    .append('\n');
        }

        sb.append("\nОтветь СТРОГО в виде JSON без пояснений и без markdown, вида:\n");
        sb.append("{\"perfumeId\": <id из списка выше>, \"explanation\": \"<объяснение выбора на русском, 2-4 предложения>\"}");
        return sb.toString();
    }
}
