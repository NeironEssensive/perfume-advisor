package com.perfumeadvisor.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfumeadvisor.backend.recommendation.RecommendationService;
import com.perfumeadvisor.backend.recommendation.SeasonResolver;
import com.perfumeadvisor.common.dto.AccordDto;
import com.perfumeadvisor.common.dto.AiRecommendationRequest;
import com.perfumeadvisor.common.dto.AiRecommendationResponse;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class AiRecommendationService {

    private static final int CANDIDATE_LIMIT = 10;
    private static final int NUM_PREDICT = 200;

    private final RecommendationService recommendationService;
    private final RestClient ollamaRestClient;
    private final ObjectMapper objectMapper;
    private final String ollamaModel;

    public AiRecommendationService(
            RecommendationService recommendationService,
            RestClient ollamaRestClient,
            ObjectMapper objectMapper,
            @Value("${ollama.model}") String ollamaModel) {
        this.recommendationService = recommendationService;
        this.ollamaRestClient = ollamaRestClient;
        this.objectMapper = objectMapper;
        this.ollamaModel = ollamaModel;
    }

    public AiRecommendationResponse recommend(AiRecommendationRequest request) {
        Optional<PerfumeRecommendationDto> specificMatch =
                recommendationService.findSpecificMatch(request.description());
        if (specificMatch.isPresent()) {
            PerfumeRecommendationDto match = specificMatch.get();
            return new AiRecommendationResponse(
                    List.of(match), "Вы искали именно этот аромат: " + match.brand() + " — " + match.name() + ".");
        }

        List<PerfumeRecommendationDto> catalogMatches =
                recommendationService.findCatalogMatches(request.description(), CANDIDATE_LIMIT);
        if (!catalogMatches.isEmpty()) {
            String explanation = explainCatalogMatches(request, catalogMatches);
            return new AiRecommendationResponse(catalogMatches, explanation);
        }

        Season season = PreferenceExtractor.extractSeason(request.description())
                .orElseGet(() -> request.season() != null ? request.season() : SeasonResolver.currentSeason());
        Gender gender = PreferenceExtractor.extractGender(request.description()).orElse(request.gender());
        Occasion occasion = PreferenceExtractor.extractOccasion(request.description()).orElse(request.occasion());

        List<PerfumeRecommendationDto> candidates =
                recommendationService.recommend(season, occasion, gender, CANDIDATE_LIMIT);

        if (candidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Нет подходящих кандидатов");
        }

        String explanation = explainSelection(request, season, occasion, candidates);
        return new AiRecommendationResponse(candidates, explanation);
    }

    private String explainSelection(
            AiRecommendationRequest request, Season season, Occasion occasion, List<PerfumeRecommendationDto> candidates) {
        try {
            String prompt = buildPrompt(request, season, occasion, candidates);
            return callOllama(prompt).explanation();
        } catch (Exception e) {
            log.warn("Ollama недоступна или вернула ошибку, используем запасное объяснение", e);
            return "Эти ароматы лучше всего подходят на " + ru(season) + " и на повод \"" + ru(occasion) + "\".";
        }
    }

    private String explainCatalogMatches(AiRecommendationRequest request, List<PerfumeRecommendationDto> matches) {
        try {
            String prompt = buildCatalogPrompt(request, matches);
            return callOllama(prompt).explanation();
        } catch (Exception e) {
            log.warn("Ollama недоступна или вернула ошибку, используем запасное объяснение", e);
            return "Вот подборка ароматов по вашему запросу, отсортированная по рейтингу.";
        }
    }

    private AiExplanation callOllama(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", ollamaModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "format", "json",
                "stream", false,
                "options", Map.of("num_predict", NUM_PREDICT, "temperature", 0.4));

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
            return objectMapper.readValue(content, AiExplanation.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Ollama вернула невалидный JSON: " + content, e);
        }
    }

    private String persona() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ты — Виктор, парфюмерный эксперт с двадцатилетним стажем: работал парфюмером-композитором ");
        sb.append("и консультантом в нишевых бутиках, знаешь классификацию ароматов, аккорды, пирамиду нот ");
        sb.append("и умеешь простыми словами объяснить, почему аромат подходит человеку.\n\n");

        sb.append("ТРЕБОВАНИЯ К ЯЗЫКУ:\n");
        sb.append("- Пиши только на грамотном, естественном русском языке, как носитель языка.\n");
        sb.append("- Никогда не вставляй английские названия сезонов, поводов или категорий (season, occasion ");
        sb.append("и т.п.) — переводи их на русский.\n");
        sb.append("- Не повторяй дословно список аккордов из подборки, а органично вплетай их в описание ");
        sb.append("(например, вместо \"аккорды: woody, vanilla\" пиши \"тёплая древесно-ванильная база\").\n");
        sb.append("- Пиши живо и по-человечески, как парфюмерный консультант в разговоре с клиентом, ");
        sb.append("без канцелярита и без общих фраз ни о чём.\n\n");
        return sb.toString();
    }

    private String buildCatalogPrompt(AiRecommendationRequest request, List<PerfumeRecommendationDto> matches) {
        StringBuilder sb = new StringBuilder();
        sb.append(persona());

        sb.append("ЗАПРОС КЛИЕНТА: ").append(request.description()).append("\n");
        sb.append("Клиент искал конкретный бренд или название аромата, а не описывал повод/сезон/предпочтения. ");
        sb.append("Ниже — найденные варианты, отсортированные по рейтингу:\n");
        for (PerfumeRecommendationDto candidate : matches) {
            String accords = candidate.accords().stream()
                    .map(AccordDto::name)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            sb.append("- ").append(candidate.brand()).append(" — ").append(candidate.name())
                    .append(" (аккорды: ").append(accords).append(")")
                    .append('\n');
        }

        sb.append("\nЗАДАЧА: напиши короткое дружелюбное вступление (1-3 предложения) к этой подборке — ");
        sb.append("что объединяет эти ароматы по стилю или характеру. Не выбирай и не выделяй один аромат.\n");
        sb.append("Ответь СТРОГО в виде JSON без пояснений и без markdown, в точности такого вида:\n");
        sb.append("{\"explanation\": \"<текст на русском>\"}");
        return sb.toString();
    }

    private String buildPrompt(
            AiRecommendationRequest request, Season season, Occasion occasion, List<PerfumeRecommendationDto> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append(persona());

        sb.append("ЗАПРОС КЛИЕНТА: ").append(request.description()).append("\n");
        sb.append("Повод: ").append(ru(occasion)).append("\n");
        sb.append("Сезон: ").append(ru(season)).append("\n\n");

        sb.append("Ниже подборка ароматов, уже подобранных под сезон и повод:\n");
        for (PerfumeRecommendationDto candidate : candidates) {
            String accords = candidate.accords().stream()
                    .map(AccordDto::name)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            sb.append("- ").append(candidate.brand()).append(" — ").append(candidate.name())
                    .append(" (аккорды: ").append(accords).append(")")
                    .append('\n');
        }

        sb.append("\nПРИМЕР ХОРОШЕГО ОТВЕТА (для другого запроса, только для образца стиля):\n");
        sb.append("{\"explanation\": \"Для тёплого осеннего вечера эта подборка делает акцент на уютных ");
        sb.append("пряно-древесных композициях с оттенком ванили и мускуса — они звучат обволакивающе и ");
        sb.append("хорошо держатся на коже весь вечер, оставаясь при этом ненавязчивыми для окружающих.\"}\n\n");

        sb.append("ЗАДАЧА: напиши одно связное объяснение на 2-4 предложения, почему именно эта подборка ");
        sb.append("подходит под запрос клиента. Говори о подборке в целом, не выделяй один аромат.\n");
        sb.append("Ответь СТРОГО в виде JSON без пояснений и без markdown, в точности такого вида:\n");
        sb.append("{\"explanation\": \"<текст на русском>\"}");
        return sb.toString();
    }

    private String ru(Season season) {
        return switch (season) {
            case SPRING -> "весна";
            case SUMMER -> "лето";
            case AUTUMN -> "осень";
            case WINTER -> "зима";
        };
    }

    private String ru(Occasion occasion) {
        return switch (occasion) {
            case OFFICE -> "офис";
            case EVERYDAY -> "повседневная носка";
            case DATE_NIGHT -> "свидание";
            case SPECIAL_EVENT -> "особый случай";
            case SPORT -> "спорт";
            case SCHOOL -> "школа";
        };
    }
}
