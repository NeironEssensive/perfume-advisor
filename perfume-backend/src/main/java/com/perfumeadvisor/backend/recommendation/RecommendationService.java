package com.perfumeadvisor.backend.recommendation;

import com.perfumeadvisor.backend.catalog.domain.Perfume;
import com.perfumeadvisor.backend.catalog.repository.PerfumeRepository;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PerfumeRepository perfumeRepository;

    @Transactional(readOnly = true)
    public List<PerfumeRecommendationDto> recommend(Season season, Occasion occasion, Gender gender, int limit) {
        List<Object[]> rows =
                perfumeRepository.findRecommendations(season, occasion, gender, PageRequest.of(0, limit));

        List<PerfumeRecommendationDto> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            Perfume perfume = (Perfume) row[0];
            Integer seasonScore = (Integer) row[1];
            Integer occasionScore = (Integer) row[2];
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
                    seasonScore,
                    occasionScore));
        }
        return result;
    }
}
