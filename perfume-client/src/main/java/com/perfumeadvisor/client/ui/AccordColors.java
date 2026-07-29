package com.perfumeadvisor.client.ui;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AccordColors {

    private static final Map<String, String> NAMED_COLORS = new LinkedHashMap<>();

    static {
        NAMED_COLORS.put("citrus", "#f5d90a");
        NAMED_COLORS.put("white floral", "#ffb6d9");
        NAMED_COLORS.put("floral", "#ff6fae");
        NAMED_COLORS.put("rose", "#ff4d6d");
        NAMED_COLORS.put("violet", "#a78bfa");
        NAMED_COLORS.put("iris", "#a78bfa");
        NAMED_COLORS.put("woody", "#8b5a2b");
        NAMED_COLORS.put("fruity", "#ff8c42");
        NAMED_COLORS.put("tropical", "#ff9f1c");
        NAMED_COLORS.put("coconut", "#f5e6c8");
        NAMED_COLORS.put("cherry", "#d1274c");
        NAMED_COLORS.put("sweet", "#e6399b");
        NAMED_COLORS.put("vanilla", "#f4c95d");
        NAMED_COLORS.put("caramel", "#c17817");
        NAMED_COLORS.put("honey", "#f2a900");
        NAMED_COLORS.put("praline", "#c17817");
        NAMED_COLORS.put("amber", "#d98324");
        NAMED_COLORS.put("musky", "#c9b8a8");
        NAMED_COLORS.put("musk", "#c9b8a8");
        NAMED_COLORS.put("aromatic", "#4fd1c5");
        NAMED_COLORS.put("fresh spicy", "#7ed957");
        NAMED_COLORS.put("warm spicy", "#e8543e");
        NAMED_COLORS.put("spicy", "#f2542d");
        NAMED_COLORS.put("cinnamon", "#a0522d");
        NAMED_COLORS.put("fresh", "#38bdf8");
        NAMED_COLORS.put("green", "#22c55e");
        NAMED_COLORS.put("herbal", "#6f9a3c");
        NAMED_COLORS.put("aquatic", "#2dd4bf");
        NAMED_COLORS.put("marine", "#0ea5e9");
        NAMED_COLORS.put("ozonic", "#7dd3fc");
        NAMED_COLORS.put("powdery", "#c4a7e7");
        NAMED_COLORS.put("lavender", "#9d7fd6");
        NAMED_COLORS.put("leather", "#7c4a3a");
        NAMED_COLORS.put("tobacco", "#8a6642");
        NAMED_COLORS.put("oud", "#5c4326");
        NAMED_COLORS.put("patchouli", "#6b4226");
        NAMED_COLORS.put("earthy", "#7a6650");
        NAMED_COLORS.put("mossy", "#5f7a4f");
        NAMED_COLORS.put("animalic", "#9a7b6b");
        NAMED_COLORS.put("smoky", "#6b6b6b");
        NAMED_COLORS.put("lactonic", "#f7e7ce");
        NAMED_COLORS.put("nutty", "#b08968");
        NAMED_COLORS.put("cacao", "#5a3825");
        NAMED_COLORS.put("chocolate", "#4a2c17");
        NAMED_COLORS.put("coffee", "#4b3621");
        NAMED_COLORS.put("aldehydic", "#e8e8f0");
        NAMED_COLORS.put("metallic", "#9aa5b1");
        NAMED_COLORS.put("mineral", "#8899aa");
        NAMED_COLORS.put("salty", "#a3c9d1");
        NAMED_COLORS.put("soapy", "#cdeaf2");
        NAMED_COLORS.put("champagne", "#f0e6a0");
        NAMED_COLORS.put("rum", "#8b4a1c");
        NAMED_COLORS.put("whiskey", "#a2691e");
        NAMED_COLORS.put("wine", "#7b1338");
        NAMED_COLORS.put("vodka", "#d9e6f0");
        NAMED_COLORS.put("conifer", "#2f6b3a");
        NAMED_COLORS.put("camphor", "#a9c7c9");
        NAMED_COLORS.put("cannabis", "#5c7a3a");
        NAMED_COLORS.put("bitter", "#5c5c3a");
        NAMED_COLORS.put("sour", "#c6d94e");
        NAMED_COLORS.put("beeswax", "#e0b13a");
        NAMED_COLORS.put("sand", "#e0c896");
        NAMED_COLORS.put("savory", "#8a7a5c");
        NAMED_COLORS.put("alcohol", "#dbe8f0");
    }

    private static final String[] FALLBACK_PALETTE = {
        "#f5d90a", "#ff6fae", "#4fd1c5", "#ff8c42", "#38bdf8", "#22c55e", "#a78bfa",
        "#e8543e", "#c4a7e7", "#7ed957", "#f2a900", "#0ea5e9", "#d1274c", "#8b5a2b"
    };

    private AccordColors() {
    }

    public static String colorFor(String accordName) {
        String lower = accordName.toLowerCase(Locale.ROOT);
        String direct = NAMED_COLORS.get(lower);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<String, String> entry : NAMED_COLORS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        int index = Math.floorMod(lower.hashCode(), FALLBACK_PALETTE.length);
        return FALLBACK_PALETTE[index];
    }

    public static String textColorFor(String hexColor) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        return luminance > 0.55 ? "#1c1b22" : "#f2f1f7";
    }
}
