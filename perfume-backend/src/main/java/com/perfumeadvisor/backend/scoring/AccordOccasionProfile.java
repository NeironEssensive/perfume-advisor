package com.perfumeadvisor.backend.scoring;

import com.perfumeadvisor.common.enums.Occasion;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AccordOccasionProfile {

    private static final Map<Occasion, Integer> DEFAULT = Map.of(
            Occasion.OFFICE, 45,
            Occasion.EVERYDAY, 50,
            Occasion.DATE_NIGHT, 50,
            Occasion.SPECIAL_EVENT, 50,
            Occasion.SPORT, 35,
            Occasion.SCHOOL, 48);

    private static final Map<String, Map<Occasion, Integer>> PROFILES = build();

    private AccordOccasionProfile() {
    }

    public static int score(String accordName, Occasion occasion) {
        return PROFILES.getOrDefault(accordName.toLowerCase(Locale.ROOT), DEFAULT).get(occasion);
    }

    private static Map<String, Map<Occasion, Integer>> build() {
        Map<String, Map<Occasion, Integer>> m = new HashMap<>();
        put(m, "woody", 55, 65, 75, 80, 20);
        put(m, "citrus", 80, 85, 40, 35, 90);
        put(m, "aromatic", 75, 80, 50, 45, 80);
        put(m, "sweet", 35, 55, 75, 65, 20);
        put(m, "fruity", 55, 75, 55, 45, 55);
        put(m, "powdery", 65, 60, 55, 60, 25);
        put(m, "floral", 60, 75, 65, 60, 35);
        put(m, "warm spicy", 30, 45, 80, 80, 15);
        put(m, "white floral", 55, 70, 70, 65, 30);
        put(m, "fresh spicy", 65, 75, 55, 50, 65);
        put(m, "amber", 25, 45, 85, 85, 10);
        put(m, "vanilla", 30, 50, 85, 70, 10);
        put(m, "musky", 55, 65, 75, 65, 40);
        put(m, "green", 70, 75, 40, 35, 75);
        put(m, "rose", 50, 65, 70, 65, 25);
        put(m, "fresh", 85, 85, 40, 35, 90);
        put(m, "patchouli", 25, 45, 75, 80, 15);
        put(m, "leather", 20, 40, 70, 85, 10);
        put(m, "earthy", 40, 55, 55, 55, 45);
        put(m, "aquatic", 70, 75, 35, 30, 95);
        put(m, "lavender", 70, 70, 55, 50, 55);
        put(m, "oud", 15, 35, 70, 90, 5);
        put(m, "iris", 60, 60, 60, 65, 30);
        put(m, "yellow floral", 55, 65, 60, 55, 35);
        put(m, "soft spicy", 45, 60, 70, 65, 30);
        put(m, "balsamic", 25, 40, 75, 80, 10);
        put(m, "tropical", 40, 65, 55, 45, 55);
        put(m, "violet", 60, 60, 60, 55, 35);
        put(m, "ozonic", 70, 75, 35, 30, 90);
        put(m, "animalic", 10, 30, 75, 80, 5);
        put(m, "tuberose", 40, 55, 70, 65, 25);
        put(m, "marine", 70, 75, 30, 25, 95);
        put(m, "herbal", 70, 70, 45, 40, 70);
        put(m, "caramel", 25, 45, 75, 65, 10);
        put(m, "cinnamon", 25, 45, 80, 75, 10);
        put(m, "smoky", 15, 30, 65, 85, 5);
        put(m, "mossy", 45, 55, 60, 60, 35);
        put(m, "almond", 35, 55, 70, 60, 20);
        put(m, "tobacco", 10, 30, 65, 85, 5);
        put(m, "coconut", 30, 60, 50, 40, 55);
        put(m, "lactonic", 45, 55, 55, 50, 35);
        put(m, "honey", 25, 45, 75, 65, 10);
        put(m, "aldehydic", 65, 60, 60, 70, 40);
        put(m, "nutty", 35, 50, 65, 55, 25);
        put(m, "cherry", 35, 60, 60, 50, 30);
        put(m, "cacao", 20, 40, 75, 65, 10);
        put(m, "coffee", 25, 40, 70, 60, 15);
        put(m, "salty", 55, 65, 35, 30, 80);
        put(m, "anis", 45, 55, 60, 55, 35);
        put(m, "chocolate", 15, 35, 75, 65, 5);
        put(m, "rum", 15, 35, 70, 70, 10);
        put(m, "metallic", 40, 45, 55, 55, 35);
        put(m, "whiskey", 10, 30, 65, 80, 5);
        put(m, "mineral", 55, 60, 40, 40, 65);
        put(m, "cannabis", 15, 40, 60, 55, 20);
        put(m, "camphor", 40, 45, 45, 45, 50);
        put(m, "champagne", 45, 55, 70, 75, 25);
        put(m, "conifer", 35, 45, 45, 50, 55);
        put(m, "beeswax", 35, 45, 60, 55, 25);
        put(m, "sand", 50, 55, 45, 40, 60);
        put(m, "savory", 35, 45, 50, 45, 35);
        put(m, "wine", 15, 35, 70, 75, 5);
        put(m, "vodka", 35, 45, 55, 50, 30);
        put(m, "soapy", 80, 75, 35, 30, 60);
        put(m, "coca-cola", 30, 55, 45, 35, 35);
        put(m, "alcohol", 35, 45, 55, 50, 30);
        put(m, "sour", 45, 55, 40, 35, 45);
        put(m, "bitter", 40, 45, 50, 50, 35);
        return m;
    }

    private static void put(
            Map<String, Map<Occasion, Integer>> m,
            String accord,
            int office,
            int everyday,
            int dateNight,
            int specialEvent,
            int sport) {
        int school = Math.round(0.55f * everyday + 0.45f * office);
        m.put(
                accord,
                Map.of(
                        Occasion.OFFICE, office,
                        Occasion.EVERYDAY, everyday,
                        Occasion.DATE_NIGHT, dateNight,
                        Occasion.SPECIAL_EVENT, specialEvent,
                        Occasion.SPORT, sport,
                        Occasion.SCHOOL, school));
    }
}
