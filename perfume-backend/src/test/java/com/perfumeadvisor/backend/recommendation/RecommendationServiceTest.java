package com.perfumeadvisor.backend.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.perfumeadvisor.backend.catalog.domain.Brand;
import com.perfumeadvisor.backend.catalog.domain.Perfume;
import com.perfumeadvisor.backend.catalog.repository.PerfumeAccordRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeNoteRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeRepository;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.RecommendationSort;
import com.perfumeadvisor.common.enums.Season;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private PerfumeRepository perfumeRepository;
    @Mock
    private PerfumeNoteRepository perfumeNoteRepository;
    @Mock
    private PerfumeAccordRepository perfumeAccordRepository;

    private RecommendationService service;

    private Perfume tobaccoVanille;
    private Perfume noirExtreme;
    private Perfume greyVetiver;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(perfumeRepository, perfumeNoteRepository, perfumeAccordRepository);

        Brand tomFord = Brand.builder().name("Tom Ford").build();
        tobaccoVanille = Perfume.builder().id(1L).brand(tomFord).name("Tobacco Vanille")
                .ratingValue(4.23).gender(Gender.UNISEX).build();
        noirExtreme = Perfume.builder().id(2L).brand(tomFord).name("Noir Extreme")
                .ratingValue(4.43).gender(Gender.MALE).build();
        greyVetiver = Perfume.builder().id(3L).brand(tomFord).name("Grey Vetiver")
                .ratingValue(4.50).gender(Gender.UNISEX).build();

        lenient().when(perfumeNoteRepository.findNotesByPerfumeIds(any())).thenReturn(List.of());
        lenient().when(perfumeAccordRepository.findAccordNamesByPerfumeIds(any())).thenReturn(List.of());
    }

    private void stubSearch(String pattern, Perfume... perfumes) {
        when(perfumeRepository.searchByBrandOrName(eq(pattern), any()))
                .thenReturn(List.of(perfumes));
    }

    @Test
    void findsSpecificPerfumeWhenOneCandidateDominatesTheScore() {
        stubSearch("tom", tobaccoVanille, noirExtreme, greyVetiver);
        stubSearch("ford", tobaccoVanille, noirExtreme, greyVetiver);
        stubSearch("toba", tobaccoVanille);
        stubSearch("vani", tobaccoVanille);

        Optional<PerfumeRecommendationDto> match = service.findSpecificMatch("tom ford tobacco vanille");

        assertThat(match).isPresent();
        assertThat(match.get().id()).isEqualTo(1L);
        assertThat(match.get().name()).isEqualTo("Tobacco Vanille");
    }

    @Test
    void doesNotPickAnArbitraryPerfumeWhenOnlyTheBrandMatches() {
        stubSearch("tom", tobaccoVanille, noirExtreme, greyVetiver);
        stubSearch("ford", tobaccoVanille, noirExtreme, greyVetiver);

        Optional<PerfumeRecommendationDto> match = service.findSpecificMatch("tom ford");

        assertThat(match).isEmpty();
    }

    @Test
    void findsNothingWhenNoMeaningfulTokensSurvive() {
        Optional<PerfumeRecommendationDto> match = service.findSpecificMatch("the for of to");

        assertThat(match).isEmpty();
    }

    @Test
    void returnsBrandWideMatchesRankedByRatingWhenNoSingleModelIsNamed() {
        stubSearch("tom", tobaccoVanille, noirExtreme, greyVetiver);
        stubSearch("ford", tobaccoVanille, noirExtreme, greyVetiver);

        List<PerfumeRecommendationDto> matches = service.findCatalogMatches("tom ford", 10);

        assertThat(matches).extracting(PerfumeRecommendationDto::id)
                .containsExactly(3L, 2L, 1L);
    }

    @Test
    void ratingSortFetchesALargePoolAndOrdersByTrueRatingRegardlessOfRelevanceOrder() {
        List<Object[]> rows = List.of(
                new Object[] {tobaccoVanille, 50, 50},
                new Object[] {noirExtreme, 50, 50},
                new Object[] {greyVetiver, 50, 50});

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(perfumeRepository.findRecommendations(any(), any(), any(), pageableCaptor.capture()))
                .thenReturn(rows);

        List<PerfumeRecommendationDto> result = service.recommend(
                Season.SUMMER, Occasion.EVERYDAY, null, 2, RecommendationSort.RATING_DESC);

        assertThat(pageableCaptor.getValue().getPageSize()).isGreaterThanOrEqualTo(500);
        assertThat(result).extracting(PerfumeRecommendationDto::id).containsExactly(3L, 2L);
    }

    @Test
    void relevanceSortRequestsExactlyTheLimitAndKeepsServerOrder() {
        List<Object[]> rows = List.of(
                new Object[] {noirExtreme, 80, 70},
                new Object[] {tobaccoVanille, 60, 55});

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(perfumeRepository.findRecommendations(any(), any(), any(), pageableCaptor.capture()))
                .thenReturn(rows);

        List<PerfumeRecommendationDto> result = service.recommend(
                Season.SUMMER, Occasion.EVERYDAY, null, 2, RecommendationSort.RELEVANCE);

        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(2);
        assertThat(result).extracting(PerfumeRecommendationDto::id).containsExactly(2L, 1L);
    }

    @Test
    void priceSortPutsUnpricedPerfumesLast() {
        Perfume priced = Perfume.builder().id(4L).brand(Brand.builder().name("Dior").build())
                .name("Sauvage").price(new BigDecimal("5000")).ratingValue(4.0).build();
        Perfume unpriced = Perfume.builder().id(5L).brand(Brand.builder().name("Dior").build())
                .name("Homme").ratingValue(4.0).build();

        List<Object[]> rows = List.of(
                new Object[] {unpriced, 50, 50},
                new Object[] {priced, 50, 50});

        when(perfumeRepository.findRecommendations(any(), any(), any(), any())).thenReturn(rows);

        List<PerfumeRecommendationDto> result = service.recommend(
                Season.SUMMER, Occasion.EVERYDAY, null, 2, RecommendationSort.PRICE_DESC);

        assertThat(result).extracting(PerfumeRecommendationDto::id).containsExactly(4L, 5L);
    }
}
