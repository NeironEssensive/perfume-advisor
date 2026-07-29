package com.perfumeadvisor.backend.recommendation;

import com.perfumeadvisor.backend.ai.Transliterator;
import com.perfumeadvisor.backend.catalog.domain.Perfume;
import com.perfumeadvisor.backend.catalog.repository.PerfumeAccordRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeNoteRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeRepository;
import com.perfumeadvisor.common.dto.AccordDto;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.PyramidPosition;
import com.perfumeadvisor.common.enums.RecommendationSort;
import com.perfumeadvisor.common.enums.Season;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final Set<String> STOPWORDS = Set.of(
            "мне", "для", "хочу", "нужен", "нужна", "нужно", "покажи", "пожалуйста", "есть", "будь",
            "аромат", "парфюм", "парфюма", "духи", "запах", "этот", "эта", "это", "эти", "чтобы",
            "его", "ее", "её", "любой", "какой", "какая", "самый", "очень", "просто", "можно",
            "хотел", "хотела", "хотелось", "или", "так", "же", "то", "список", "топ",
            "the", "and", "for", "with", "please", "show", "me", "want", "need", "of", "to",
            "top", "only", "one", "not", "just", "give", "list", "many", "some", "few", "best",
            "good", "fragrance", "fragrances", "perfume", "perfumes", "scent", "scents");

    private static final Map<String, String> BRAND_ABBREVIATIONS = Map.ofEntries(
            Map.entry("jpg", "jean paul gaultier"),
            Map.entry("ysl", "yves saint laurent"),
            Map.entry("dg", "dolce gabbana"),
            Map.entry("d&g", "dolce gabbana"),
            Map.entry("ck", "calvin klein"),
            Map.entry("tf", "tom ford"),
            Map.entry("mfk", "maison francis kurkdjian"),
            Map.entry("pdm", "parfums de marly"),
            Map.entry("mmm", "maison martin margiela"));

    private static final int MATCH_SEARCH_LIMIT = 200;
    private static final int MIN_MATCH_SCORE = 2;
    private static final int RATING_SORT_POOL_SIZE = 500;
    private static final int PRICE_SORT_POOL_SIZE = 6000;

    private final PerfumeRepository perfumeRepository;
    private final PerfumeNoteRepository perfumeNoteRepository;
    private final PerfumeAccordRepository perfumeAccordRepository;

    @Transactional(readOnly = true)
    public Optional<PerfumeRecommendationDto> findSpecificMatch(String rawText) {
        List<String> tokens = tokenize(rawText);
        if (tokens.isEmpty()) {
            return Optional.empty();
        }

        TokenMatchResult result = matchByTokens(tokens);
        if (result.scoreById().isEmpty()) {
            return Optional.empty();
        }

        int maxScore = Collections.max(result.scoreById().values());
        if (maxScore < MIN_MATCH_SCORE) {
            return Optional.empty();
        }

        List<Long> topIds = result.scoreById().entrySet().stream()
                .filter(e -> e.getValue() == maxScore)
                .map(Map.Entry::getKey)
                .toList();
        if (topIds.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(enrich(List.of(result.perfumeById().get(topIds.get(0))), Map.of(), Map.of()).get(0));
    }

    @Transactional(readOnly = true)
    public List<PerfumeRecommendationDto> findCatalogMatches(String rawText, int limit) {
        List<String> tokens = tokenize(rawText);
        if (tokens.isEmpty()) {
            return List.of();
        }

        TokenMatchResult result = matchByTokens(tokens);
        List<Perfume> ranked = result.scoreById().entrySet().stream()
                .filter(e -> e.getValue() >= MIN_MATCH_SCORE)
                .sorted(Map.Entry.<Long, Integer>comparingByValue()
                        .reversed()
                        .thenComparing(e -> ratingOf(result.perfumeById().get(e.getKey())), Comparator.reverseOrder()))
                .map(e -> result.perfumeById().get(e.getKey()))
                .limit(limit)
                .toList();

        return enrich(ranked, Map.of(), Map.of());
    }

    private record TokenMatchResult(Map<Long, Integer> scoreById, Map<Long, Perfume> perfumeById) {
    }

    private TokenMatchResult matchByTokens(List<String> tokens) {
        Map<Long, Integer> scoreById = new HashMap<>();
        Map<Long, Perfume> perfumeById = new HashMap<>();
        for (String token : tokens) {
            String pattern = searchPattern(token);
            for (Perfume perfume : perfumeRepository.searchByBrandOrName(pattern, PageRequest.of(0, MATCH_SEARCH_LIMIT))) {
                scoreById.merge(perfume.getId(), 1, Integer::sum);
                perfumeById.putIfAbsent(perfume.getId(), perfume);
            }
        }
        return new TokenMatchResult(scoreById, perfumeById);
    }

    private String searchPattern(String token) {
        return token.length() >= 5 ? token.substring(0, 4) : token;
    }

    private List<String> tokenize(String rawText) {
        List<String> rawWords = Arrays.asList(rawText.toLowerCase(Locale.ROOT).split("[^a-zа-яё&]+"));
        List<String> expanded = new ArrayList<>();
        for (String word : rawWords) {
            String expansion = BRAND_ABBREVIATIONS.get(word);
            if (expansion != null) {
                expanded.addAll(Arrays.asList(expansion.split(" ")));
            } else {
                expanded.add(word);
            }
        }

        return expanded.stream()
                .filter(w -> w.length() >= 3)
                .filter(w -> !STOPWORDS.contains(w))
                .map(Transliterator::toLatin)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PerfumeRecommendationDto> recommend(Season season, Occasion occasion, Gender gender, int limit) {
        return recommend(season, occasion, gender, limit, RecommendationSort.RELEVANCE);
    }

    @Transactional(readOnly = true)
    public List<PerfumeRecommendationDto> recommend(
            Season season, Occasion occasion, Gender gender, int limit, RecommendationSort sort) {
        int fetchSize = switch (sort) {
            case RELEVANCE -> limit;
            case RATING_DESC, RATING_ASC -> Math.max(limit, RATING_SORT_POOL_SIZE);
            case PRICE_DESC, PRICE_ASC -> Math.max(limit, PRICE_SORT_POOL_SIZE);
        };

        List<Object[]> rows = perfumeRepository.findRecommendations(season, occasion, gender, PageRequest.of(0, fetchSize));

        List<Perfume> perfumes = rows.stream().map(row -> (Perfume) row[0]).toList();
        Map<Long, Integer> seasonScores = new HashMap<>();
        Map<Long, Integer> occasionScores = new HashMap<>();
        for (Object[] row : rows) {
            Long id = ((Perfume) row[0]).getId();
            seasonScores.put(id, (Integer) row[1]);
            occasionScores.put(id, (Integer) row[2]);
        }

        List<PerfumeRecommendationDto> enriched = enrich(perfumes, seasonScores, occasionScores);
        if (sort == RecommendationSort.RELEVANCE) {
            return enriched;
        }
        return enriched.stream().sorted(comparatorFor(sort)).limit(limit).toList();
    }

    private Comparator<PerfumeRecommendationDto> comparatorFor(RecommendationSort sort) {
        return switch (sort) {
            case RATING_DESC -> Comparator.comparing(
                    (PerfumeRecommendationDto p) -> p.ratingValue() == null ? -1 : p.ratingValue()).reversed();
            case RATING_ASC -> Comparator.comparing(
                    (PerfumeRecommendationDto p) -> p.ratingValue() == null ? Double.MAX_VALUE : p.ratingValue());
            case PRICE_DESC -> Comparator.comparing(
                    (PerfumeRecommendationDto p) -> p.price() == null ? BigDecimal.ZERO : p.price())
                    .reversed();
            case PRICE_ASC -> Comparator.comparing((PerfumeRecommendationDto p) ->
                    p.price() == null ? new BigDecimal("999999999") : p.price());
            case RELEVANCE -> (a, b) -> 0;
        };
    }

    @Transactional(readOnly = true)
    public List<PerfumeRecommendationDto> search(String query, int limit) {
        List<String> tokens = tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }

        TokenMatchResult result = matchByTokens(tokens);
        List<Perfume> ranked = result.scoreById().entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue()
                        .reversed()
                        .thenComparing(e -> ratingOf(result.perfumeById().get(e.getKey())), Comparator.reverseOrder()))
                .map(e -> result.perfumeById().get(e.getKey()))
                .limit(limit)
                .toList();

        return enrich(ranked, Map.of(), Map.of());
    }

    private double ratingOf(Perfume perfume) {
        return perfume.getRatingValue() == null ? 0.0 : perfume.getRatingValue();
    }

    private List<PerfumeRecommendationDto> enrich(
            List<Perfume> perfumes, Map<Long, Integer> seasonScores, Map<Long, Integer> occasionScores) {
        if (perfumes.isEmpty()) {
            return List.of();
        }
        List<Long> perfumeIds = perfumes.stream().map(Perfume::getId).toList();

        Map<Long, Map<PyramidPosition, List<String>>> notesByPerfumeId = loadNotes(perfumeIds);
        Map<Long, List<AccordDto>> accordsByPerfumeId = loadAccords(perfumeIds);

        List<PerfumeRecommendationDto> result = new ArrayList<>(perfumes.size());
        for (Perfume perfume : perfumes) {
            Map<PyramidPosition, List<String>> notes =
                    notesByPerfumeId.getOrDefault(perfume.getId(), Map.of());
            result.add(new PerfumeRecommendationDto(
                    perfume.getId(),
                    perfume.getBrand().getName(),
                    perfume.getName(),
                    perfume.getImageUrl(),
                    perfume.getSourceUrl(),
                    perfume.getPrice(),
                    perfume.getRatingValue(),
                    perfume.getRatingCount(),
                    perfume.getGender() == null ? null : perfume.getGender().name(),
                    seasonScores.get(perfume.getId()),
                    occasionScores.get(perfume.getId()),
                    notes.getOrDefault(PyramidPosition.TOP, List.of()),
                    notes.getOrDefault(PyramidPosition.MIDDLE, List.of()),
                    notes.getOrDefault(PyramidPosition.BASE, List.of()),
                    accordsByPerfumeId.getOrDefault(perfume.getId(), List.of())));
        }
        return result;
    }

    private Map<Long, Map<PyramidPosition, List<String>>> loadNotes(List<Long> perfumeIds) {
        Map<Long, Map<PyramidPosition, List<String>>> result = new HashMap<>();
        for (Object[] row : perfumeNoteRepository.findNotesByPerfumeIds(perfumeIds)) {
            Long perfumeId = (Long) row[0];
            PyramidPosition position = (PyramidPosition) row[1];
            String noteName = (String) row[2];
            result.computeIfAbsent(perfumeId, k -> new HashMap<>())
                    .computeIfAbsent(position, k -> new ArrayList<>())
                    .add(noteName);
        }
        return result;
    }

    private Map<Long, List<AccordDto>> loadAccords(List<Long> perfumeIds) {
        Map<Long, List<AccordDto>> result = new HashMap<>();
        for (Object[] row : perfumeAccordRepository.findAccordNamesByPerfumeIds(perfumeIds)) {
            Long perfumeId = (Long) row[0];
            String accordName = (String) row[1];
            Integer strength = (Integer) row[2];
            result.computeIfAbsent(perfumeId, k -> new ArrayList<>()).add(new AccordDto(accordName, strength));
        }
        return result;
    }
}
