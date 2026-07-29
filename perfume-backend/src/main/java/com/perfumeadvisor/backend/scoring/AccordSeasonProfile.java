package com.perfumeadvisor.backend.scoring;

import com.perfumeadvisor.common.enums.Season;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AccordSeasonProfile {

    private static final Map<Season, Integer> DEFAULT = Map.of(
            Season.SPRING, 50,
            Season.SUMMER, 50,
            Season.AUTUMN, 50,
            Season.WINTER, 50);

    private static final Map<String, Map<Season, Integer>> PROFILES = build();

    private AccordSeasonProfile() {
    }

    public static int score(String accordName, Season season) {
        return PROFILES.getOrDefault(accordName.toLowerCase(Locale.ROOT), DEFAULT).get(season);
    }

    private static Map<String, Map<Season, Integer>> build() {
        Map<String, Map<Season, Integer>> m = new HashMap<>();
        put(m, "woody", 40, 30, 80, 85);
        put(m, "citrus", 85, 90, 40, 30);
        put(m, "aromatic", 70, 75, 55, 45);
        put(m, "sweet", 50, 55, 70, 75);
        put(m, "fruity", 75, 85, 45, 30);
        put(m, "powdery", 55, 35, 65, 70);
        put(m, "floral", 90, 70, 40, 30);
        put(m, "warm spicy", 30, 25, 80, 90);
        put(m, "white floral", 80, 75, 40, 30);
        put(m, "fresh spicy", 75, 70, 45, 35);
        put(m, "amber", 25, 20, 80, 90);
        put(m, "vanilla", 35, 30, 75, 85);
        put(m, "musky", 55, 45, 65, 70);
        put(m, "green", 90, 60, 30, 20);
        put(m, "rose", 85, 55, 45, 40);
        put(m, "fresh", 85, 90, 35, 25);
        put(m, "patchouli", 25, 20, 80, 85);
        put(m, "leather", 20, 15, 75, 90);
        put(m, "earthy", 40, 30, 75, 70);
        put(m, "aquatic", 70, 95, 25, 15);
        put(m, "lavender", 75, 65, 50, 40);
        put(m, "oud", 15, 10, 75, 95);
        put(m, "iris", 70, 40, 55, 55);
        put(m, "yellow floral", 75, 65, 45, 35);
        put(m, "soft spicy", 50, 45, 65, 65);
        put(m, "balsamic", 25, 20, 80, 85);
        put(m, "tropical", 55, 95, 25, 15);
        put(m, "violet", 75, 50, 50, 45);
        put(m, "ozonic", 80, 90, 25, 15);
        put(m, "animalic", 20, 15, 65, 85);
        put(m, "tuberose", 55, 60, 45, 45);
        put(m, "marine", 70, 95, 20, 10);
        put(m, "herbal", 75, 65, 45, 35);
        put(m, "caramel", 30, 30, 75, 80);
        put(m, "cinnamon", 25, 20, 85, 85);
        put(m, "smoky", 15, 10, 70, 90);
        put(m, "mossy", 45, 35, 70, 65);
        put(m, "almond", 45, 45, 65, 60);
        put(m, "tobacco", 15, 10, 75, 90);
        put(m, "coconut", 45, 95, 20, 10);
        put(m, "lactonic", 55, 55, 50, 50);
        put(m, "honey", 30, 25, 75, 75);
        put(m, "aldehydic", 80, 55, 45, 45);
        put(m, "nutty", 40, 35, 70, 65);
        put(m, "cherry", 55, 60, 50, 40);
        put(m, "cacao", 25, 20, 75, 80);
        put(m, "coffee", 20, 20, 75, 80);
        put(m, "salty", 60, 90, 25, 15);
        put(m, "anis", 45, 40, 60, 55);
        put(m, "chocolate", 25, 20, 70, 80);
        put(m, "rum", 20, 30, 70, 75);
        put(m, "metallic", 50, 45, 50, 50);
        put(m, "whiskey", 15, 15, 75, 85);
        put(m, "mineral", 60, 70, 35, 30);
        put(m, "cannabis", 35, 35, 60, 55);
        put(m, "camphor", 45, 40, 55, 55);
        put(m, "champagne", 60, 55, 45, 45);
        put(m, "conifer", 30, 20, 60, 85);
        put(m, "beeswax", 40, 30, 65, 65);
        put(m, "sand", 50, 60, 45, 40);
        put(m, "savory", 40, 40, 55, 55);
        put(m, "wine", 25, 25, 70, 75);
        put(m, "vodka", 45, 50, 45, 40);
        put(m, "soapy", 70, 65, 40, 40);
        put(m, "coca-cola", 40, 50, 50, 45);
        put(m, "alcohol", 45, 45, 45, 45);
        put(m, "sour", 55, 55, 40, 35);
        put(m, "bitter", 45, 45, 50, 50);
        return m;
    }

    private static void put(
            Map<String, Map<Season, Integer>> m, String accord, int spring, int summer, int autumn, int winter) {
        m.put(
                accord,
                Map.of(
                        Season.SPRING, spring,
                        Season.SUMMER, summer,
                        Season.AUTUMN, autumn,
                        Season.WINTER, winter));
    }
}
