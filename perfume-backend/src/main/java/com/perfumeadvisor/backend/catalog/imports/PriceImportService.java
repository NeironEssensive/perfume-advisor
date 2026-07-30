package com.perfumeadvisor.backend.catalog.imports;

import com.perfumeadvisor.backend.catalog.domain.Perfume;
import com.perfumeadvisor.backend.catalog.repository.PerfumeRepository;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceImportService {

    private static final Set<String> STOPWORDS = Set.of(
            "eau", "de", "parfum", "edp", "edt", "cologne", "spray", "fl", "oz", "ml", "new",
            "sealed", "tester", "box", "for", "men", "women", "womens", "mens", "by", "fragrance",
            "perfume", "authentic", "original", "brand", "full", "size", "bottle", "free",
            "shipping", "fast", "the", "and", "with", "her", "him");

    private static final Set<String> EXCLUDED_TITLE_MARKERS = Set.of(
            "set of", "lot of", "bundle", "sample", "decant", "miniature", "travel size", "mini ",
            "impression", "inspired by", "compare to", "dupe", "clone", "replica", "our version");

    private static final Set<String> EXCLUDED_TYPE_MARKERS = Set.of(
            "oil", "roll", "freshener", "deodorant", "pheromone", "gift set");

    private static final int MATCH_SEARCH_LIMIT = 200;
    private static final int MIN_NAME_MATCH_SCORE = 1;
    private static final BigDecimal RUB_PER_USD = new BigDecimal("80");

    private final PerfumeRepository perfumeRepository;

    @Transactional
    public PriceImportResult importFromCsv(List<Path> csvPaths) throws IOException {
        Map<Long, List<BigDecimal>> pricesByPerfumeId = new HashMap<>();
        Map<Long, Perfume> perfumeById = new HashMap<>();
        int totalRows = 0;

        for (Path csvPath : csvPaths) {
            totalRows += processFile(csvPath, pricesByPerfumeId, perfumeById);
        }

        int pricedPerfumes = 0;
        for (Map.Entry<Long, List<BigDecimal>> entry : pricesByPerfumeId.entrySet()) {
            Perfume perfume = perfumeById.get(entry.getKey());
            perfume.setPrice(median(entry.getValue()));
            perfumeRepository.save(perfume);
            pricedPerfumes++;
        }

        return new PriceImportResult(totalRows, pricedPerfumes);
    }

    private int processFile(
            Path csvPath, Map<Long, List<BigDecimal>> pricesByPerfumeId, Map<Long, Perfume> perfumeById)
            throws IOException {
        String content = Files.readString(csvPath, StandardCharsets.UTF_8);
        if (content.startsWith("﻿")) {
            content = content.substring(1);
        }

        CSVFormat format = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        int rows = 0;
        try (CSVParser parser = format.parse(new StringReader(content))) {
            for (CSVRecord record : parser) {
                rows++;
                try {
                    processRow(record, pricesByPerfumeId, perfumeById);
                } catch (Exception e) {
                    log.warn("Пропущена строка {} в {}: {}", record.getRecordNumber(), csvPath, e.getMessage());
                }
            }
        }
        return rows;
    }

    private void processRow(
            CSVRecord record, Map<Long, List<BigDecimal>> pricesByPerfumeId, Map<Long, Perfume> perfumeById) {
        String brand = record.get("brand");
        String title = record.get("title");
        String priceRaw = record.get("price");
        String type = record.get("type");

        String titleLower = title.toLowerCase(Locale.ROOT);
        for (String marker : EXCLUDED_TITLE_MARKERS) {
            if (titleLower.contains(marker)) {
                return;
            }
        }

        String typeLower = type.toLowerCase(Locale.ROOT);
        for (String marker : EXCLUDED_TYPE_MARKERS) {
            if (typeLower.contains(marker)) {
                return;
            }
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceRaw.strip()).multiply(RUB_PER_USD).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return;
        }
        if (price.signum() <= 0) {
            return;
        }

        List<String> brandTokens = tokenize(brand);
        List<String> titleTokens = tokenize(title).stream().filter(t -> !brandTokens.contains(t)).toList();
        if (brandTokens.isEmpty() || titleTokens.isEmpty()) {
            return;
        }

        Map<Long, Perfume> brandCandidates = new HashMap<>();
        for (String token : brandTokens) {
            for (Perfume perfume : perfumeRepository.searchByBrand(pattern(token), PageRequest.of(0, MATCH_SEARCH_LIMIT))) {
                brandCandidates.putIfAbsent(perfume.getId(), perfume);
            }
        }

        List<String> titlePatterns = titleTokens.stream().map(this::pattern).toList();
        Perfume bestMatch = null;
        int bestScore = 0;
        for (Perfume candidate : brandCandidates.values()) {
            String nameLower = candidate.getName().toLowerCase(Locale.ROOT);
            int score = (int) titlePatterns.stream().filter(nameLower::contains).count();
            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        if (bestMatch != null && bestScore >= MIN_NAME_MATCH_SCORE) {
            perfumeById.putIfAbsent(bestMatch.getId(), bestMatch);
            pricesByPerfumeId.computeIfAbsent(bestMatch.getId(), k -> new ArrayList<>()).add(price);
        }
    }

    private String pattern(String token) {
        return token.length() >= 6 ? token.substring(0, 4) : token;
    }

    private List<String> tokenize(String rawText) {
        return Arrays.stream(rawText.toLowerCase(Locale.ROOT).split("[^a-z]+"))
                .filter(w -> w.length() >= 3)
                .filter(w -> !STOPWORDS.contains(w))
                .distinct()
                .toList();
    }

    private BigDecimal median(List<BigDecimal> prices) {
        List<BigDecimal> sorted = prices.stream().sorted().toList();
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return sorted.get(middle - 1).add(sorted.get(middle)).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        }
        return sorted.get(middle);
    }
}
