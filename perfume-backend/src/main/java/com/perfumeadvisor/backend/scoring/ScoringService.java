package com.perfumeadvisor.backend.scoring;

import com.perfumeadvisor.backend.catalog.domain.PerfumeOccasionScore;
import com.perfumeadvisor.backend.catalog.domain.PerfumeSeasonScore;
import com.perfumeadvisor.backend.catalog.repository.AccordRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeAccordRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeOccasionScoreRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeSeasonScoreRepository;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final PerfumeRepository perfumeRepository;
    private final AccordRepository accordRepository;
    private final PerfumeAccordRepository perfumeAccordRepository;
    private final PerfumeSeasonScoreRepository perfumeSeasonScoreRepository;
    private final PerfumeOccasionScoreRepository perfumeOccasionScoreRepository;

    private record AccordWeight(String accordName, int strength) {
    }

    @Transactional
    public int scoreAll() {
        perfumeSeasonScoreRepository.deleteAllInBatch();
        perfumeOccasionScoreRepository.deleteAllInBatch();

        Map<Long, String> accordNamesById = new HashMap<>();
        accordRepository.findAll().forEach(a -> accordNamesById.put(a.getId(), a.getName()));

        Map<Long, List<AccordWeight>> weightsByPerfumeId = new HashMap<>();
        for (Object[] row : perfumeAccordRepository.findAllPerfumeAccordTriples()) {
            Long perfumeId = (Long) row[0];
            Long accordId = (Long) row[1];
            Integer strength = (Integer) row[2];
            String accordName = accordNamesById.get(accordId);
            weightsByPerfumeId
                    .computeIfAbsent(perfumeId, k -> new ArrayList<>())
                    .add(new AccordWeight(accordName, strength));
        }

        int scored = 0;
        for (Long perfumeId : perfumeRepository.findAllIds()) {
            List<AccordWeight> weights = weightsByPerfumeId.getOrDefault(perfumeId, List.of());
            scoreOne(perfumeId, weights);
            scored++;
        }
        return scored;
    }

    private void scoreOne(Long perfumeId, List<AccordWeight> weights) {
        var perfumeRef = perfumeRepository.getReferenceById(perfumeId);

        for (Season season : Season.values()) {
            int score = weightedScore(weights, name -> AccordSeasonProfile.score(name, season));
            perfumeSeasonScoreRepository.save(PerfumeSeasonScore.builder()
                    .perfume(perfumeRef)
                    .season(season)
                    .score(score)
                    .build());
        }

        for (Occasion occasion : Occasion.values()) {
            int score = weightedScore(weights, name -> AccordOccasionProfile.score(name, occasion));
            perfumeOccasionScoreRepository.save(PerfumeOccasionScore.builder()
                    .perfume(perfumeRef)
                    .occasion(occasion)
                    .score(score)
                    .build());
        }
    }

    private int weightedScore(List<AccordWeight> weights, java.util.function.ToIntFunction<String> scoreFn) {
        if (weights.isEmpty()) {
            return 50;
        }
        int totalWeight = weights.stream().mapToInt(AccordWeight::strength).sum();
        int weightedSum = weights.stream()
                .mapToInt(w -> w.strength() * scoreFn.applyAsInt(w.accordName()))
                .sum();
        return Math.round((float) weightedSum / totalWeight);
    }
}
