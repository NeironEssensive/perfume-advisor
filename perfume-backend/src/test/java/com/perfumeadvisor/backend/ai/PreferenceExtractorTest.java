package com.perfumeadvisor.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;
import org.junit.jupiter.api.Test;

class PreferenceExtractorTest {

    @Test
    void extractsMaleGenderFromRussianText() {
        assertThat(PreferenceExtractor.extractGender("Хочу список мужских ароматов"))
                .contains(Gender.MALE);
    }

    @Test
    void extractsFemaleGenderFromRussianText() {
        assertThat(PreferenceExtractor.extractGender("что-то женское и лёгкое"))
                .contains(Gender.FEMALE);
    }

    @Test
    void extractsWinterSeasonFromRussianText() {
        assertThat(PreferenceExtractor.extractSeason("хочу что-то на зиму, тёплое"))
                .contains(Season.WINTER);
    }

    @Test
    void extractsDateNightOccasionFromRussianText() {
        assertThat(PreferenceExtractor.extractOccasion("ищу аромат на свидание"))
                .contains(Occasion.DATE_NIGHT);
    }

    @Test
    void extractsSportOccasionFromEnglishText() {
        assertThat(PreferenceExtractor.extractOccasion("something for the gym"))
                .contains(Occasion.SPORT);
    }

    @Test
    void returnsEmptyWhenNothingIsMentioned() {
        assertThat(PreferenceExtractor.extractGender("хочу приятный аромат")).isEmpty();
        assertThat(PreferenceExtractor.extractSeason("хочу приятный аромат")).isEmpty();
        assertThat(PreferenceExtractor.extractOccasion("хочу приятный аромат")).isEmpty();
    }
}
