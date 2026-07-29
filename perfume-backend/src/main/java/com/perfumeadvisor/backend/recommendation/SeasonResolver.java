package com.perfumeadvisor.backend.recommendation;

import com.perfumeadvisor.common.enums.Season;
import java.time.LocalDate;

public final class SeasonResolver {

    private SeasonResolver() {
    }

    public static Season resolve(LocalDate date) {
        return switch (date.getMonthValue()) {
            case 12, 1, 2 -> Season.WINTER;
            case 3, 4, 5 -> Season.SPRING;
            case 6, 7, 8 -> Season.SUMMER;
            default -> Season.AUTUMN;
        };
    }

    public static Season currentSeason() {
        return resolve(LocalDate.now());
    }
}
