package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;

public final class Labels {

    private Labels() {
    }

    public static String of(Occasion occasion) {
        return switch (occasion) {
            case OFFICE -> "Офис";
            case EVERYDAY -> "Повседневный";
            case DATE_NIGHT -> "Свидание";
            case SPECIAL_EVENT -> "Особый случай";
            case SPORT -> "Спорт";
            case SCHOOL -> "Школа";
        };
    }

    public static String of(Gender gender) {
        return switch (gender) {
            case MALE -> "Мужской";
            case FEMALE -> "Женский";
            case UNISEX -> "Унисекс";
        };
    }

    public static String of(Season season) {
        return switch (season) {
            case SPRING -> "Весна";
            case SUMMER -> "Лето";
            case AUTUMN -> "Осень";
            case WINTER -> "Зима";
        };
    }
}
