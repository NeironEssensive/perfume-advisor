package com.perfumeadvisor.backend.ai;

import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;
import java.util.Locale;
import java.util.Optional;

public final class PreferenceExtractor {

    private PreferenceExtractor() {
    }

    public static Optional<Gender> extractGender(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        if (normalized.contains("унисекс") || normalized.contains("unisex")) {
            return Optional.of(Gender.UNISEX);
        }
        if (normalized.contains("женск") || normalized.contains("women") || normalized.contains("female")) {
            return Optional.of(Gender.FEMALE);
        }
        if (normalized.contains("мужск") || normalized.contains("men") || normalized.contains("male")) {
            return Optional.of(Gender.MALE);
        }
        return Optional.empty();
    }

    public static Optional<Season> extractSeason(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        if (normalized.contains("зим") || normalized.contains("winter")) {
            return Optional.of(Season.WINTER);
        }
        if (normalized.contains("весен") || normalized.contains("весн") || normalized.contains("spring")) {
            return Optional.of(Season.SPRING);
        }
        if (normalized.contains("лет") || normalized.contains("summer")) {
            return Optional.of(Season.SUMMER);
        }
        if (normalized.contains("осен") || normalized.contains("autumn") || normalized.contains("fall")) {
            return Optional.of(Season.AUTUMN);
        }
        return Optional.empty();
    }

    public static Optional<Occasion> extractOccasion(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        if (normalized.contains("свидан") || normalized.contains("date")) {
            return Optional.of(Occasion.DATE_NIGHT);
        }
        if (normalized.contains("офис") || normalized.contains("работ") || normalized.contains("office")
                || normalized.contains("work")) {
            return Optional.of(Occasion.OFFICE);
        }
        if (normalized.contains("спорт") || normalized.contains("sport") || normalized.contains("трениров")
                || normalized.contains("зал") || normalized.contains("gym")) {
            return Optional.of(Occasion.SPORT);
        }
        if (normalized.contains("праздник") || normalized.contains("особый случ") || normalized.contains("special")
                || normalized.contains("вечеринк") || normalized.contains("party")) {
            return Optional.of(Occasion.SPECIAL_EVENT);
        }
        if (normalized.contains("повседневн") || normalized.contains("каждый день") || normalized.contains("everyday")) {
            return Optional.of(Occasion.EVERYDAY);
        }
        return Optional.empty();
    }
}
