package com.perfumeadvisor.client.ui;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class NoteIcons {

    private static final Map<String, String> KEYWORD_ICONS = new LinkedHashMap<>();

    static {
        KEYWORD_ICONS.put("vanilla", "🌼");
        KEYWORD_ICONS.put("caramel", "🍮");
        KEYWORD_ICONS.put("honey", "🍯");
        KEYWORD_ICONS.put("chocolate", "🍫");
        KEYWORD_ICONS.put("cacao", "🍫");
        KEYWORD_ICONS.put("coffee", "☕");
        KEYWORD_ICONS.put("almond", "🌰");
        KEYWORD_ICONS.put("hazelnut", "🌰");
        KEYWORD_ICONS.put("chestnut", "🌰");
        KEYWORD_ICONS.put("coconut", "🥥");
        KEYWORD_ICONS.put("apple", "🍎");
        KEYWORD_ICONS.put("pear", "🍐");
        KEYWORD_ICONS.put("peach", "🍑");
        KEYWORD_ICONS.put("cherry", "🍒");
        KEYWORD_ICONS.put("grape", "🍇");
        KEYWORD_ICONS.put("berry", "🍓");
        KEYWORD_ICONS.put("currant", "🫐");
        KEYWORD_ICONS.put("pineapple", "🍍");
        KEYWORD_ICONS.put("mango", "🥭");
        KEYWORD_ICONS.put("melon", "🍈");
        KEYWORD_ICONS.put("plum", "🟣");
        KEYWORD_ICONS.put("fruit", "🍑");
        KEYWORD_ICONS.put("citrus", "🍊");
        KEYWORD_ICONS.put("orange", "🍊");
        KEYWORD_ICONS.put("mandarin", "🍊");
        KEYWORD_ICONS.put("tangerine", "🍊");
        KEYWORD_ICONS.put("bergamot", "🍋");
        KEYWORD_ICONS.put("lemon", "🍋");
        KEYWORD_ICONS.put("lime", "🍋");
        KEYWORD_ICONS.put("grapefruit", "🍊");
        KEYWORD_ICONS.put("rose", "🌹");
        KEYWORD_ICONS.put("jasmine", "🌸");
        KEYWORD_ICONS.put("lily", "🌸");
        KEYWORD_ICONS.put("violet", "🌸");
        KEYWORD_ICONS.put("iris", "🌸");
        KEYWORD_ICONS.put("peony", "🌸");
        KEYWORD_ICONS.put("magnolia", "🌸");
        KEYWORD_ICONS.put("tuberose", "🌸");
        KEYWORD_ICONS.put("gardenia", "🌸");
        KEYWORD_ICONS.put("orchid", "🌺");
        KEYWORD_ICONS.put("hibiscus", "🌺");
        KEYWORD_ICONS.put("blossom", "🌸");
        KEYWORD_ICONS.put("flower", "🌸");
        KEYWORD_ICONS.put("floral", "🌸");
        KEYWORD_ICONS.put("lavender", "💜");
        KEYWORD_ICONS.put("mint", "🌿");
        KEYWORD_ICONS.put("basil", "🌿");
        KEYWORD_ICONS.put("sage", "🌿");
        KEYWORD_ICONS.put("rosemary", "🌿");
        KEYWORD_ICONS.put("thyme", "🌿");
        KEYWORD_ICONS.put("herb", "🌿");
        KEYWORD_ICONS.put("tea", "🍵");
        KEYWORD_ICONS.put("grass", "🌾");
        KEYWORD_ICONS.put("hay", "🌾");
        KEYWORD_ICONS.put("fennel", "🌿");
        KEYWORD_ICONS.put("gentian", "🪻");
        KEYWORD_ICONS.put("wood", "🌳");
        KEYWORD_ICONS.put("cedar", "🌲");
        KEYWORD_ICONS.put("sandalwood", "🌳");
        KEYWORD_ICONS.put("pine", "🌲");
        KEYWORD_ICONS.put("oud", "🪵");
        KEYWORD_ICONS.put("agarwood", "🪵");
        KEYWORD_ICONS.put("vetiver", "🌾");
        KEYWORD_ICONS.put("patchouli", "🍂");
        KEYWORD_ICONS.put("moss", "🍃");
        KEYWORD_ICONS.put("musk", "⚪");
        KEYWORD_ICONS.put("amber", "🟠");
        KEYWORD_ICONS.put("labdanum", "🟠");
        KEYWORD_ICONS.put("leather", "🧥");
        KEYWORD_ICONS.put("suede", "🧥");
        KEYWORD_ICONS.put("tobacco", "🍂");
        KEYWORD_ICONS.put("smoke", "💨");
        KEYWORD_ICONS.put("incense", "🕯️");
        KEYWORD_ICONS.put("pepper", "🌶️");
        KEYWORD_ICONS.put("cardamom", "🌶️");
        KEYWORD_ICONS.put("cinnamon", "🟤");
        KEYWORD_ICONS.put("clove", "🌰");
        KEYWORD_ICONS.put("ginger", "🫚");
        KEYWORD_ICONS.put("nutmeg", "🌰");
        KEYWORD_ICONS.put("saffron", "🌶️");
        KEYWORD_ICONS.put("anis", "⭐");
        KEYWORD_ICONS.put("spic", "🌶️");
        KEYWORD_ICONS.put("salt", "🧂");
        KEYWORD_ICONS.put("marine", "🌊");
        KEYWORD_ICONS.put("aquatic", "🌊");
        KEYWORD_ICONS.put("sea", "🌊");
        KEYWORD_ICONS.put("ozonic", "🌬️");
        KEYWORD_ICONS.put("rain", "🌧️");
        KEYWORD_ICONS.put("water", "💧");
        KEYWORD_ICONS.put("milk", "🥛");
        KEYWORD_ICONS.put("cream", "🥛");
        KEYWORD_ICONS.put("rum", "🥃");
        KEYWORD_ICONS.put("whiskey", "🥃");
        KEYWORD_ICONS.put("wine", "🍷");
        KEYWORD_ICONS.put("champagne", "🥂");
        KEYWORD_ICONS.put("cognac", "🥃");
        KEYWORD_ICONS.put("bean", "🫘");
        KEYWORD_ICONS.put("praline", "🍬");
        KEYWORD_ICONS.put("sugar", "🍬");
        KEYWORD_ICONS.put("candy", "🍬");
        KEYWORD_ICONS.put("metal", "⚙️");
        KEYWORD_ICONS.put("mineral", "💎");
        KEYWORD_ICONS.put("aldehyde", "✨");
        KEYWORD_ICONS.put("powder", "🧴");
        KEYWORD_ICONS.put("soap", "🧼");
    }

    private NoteIcons() {
    }

    public static String iconFor(String noteName) {
        String lower = noteName.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : KEYWORD_ICONS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "🌿";
    }
}
