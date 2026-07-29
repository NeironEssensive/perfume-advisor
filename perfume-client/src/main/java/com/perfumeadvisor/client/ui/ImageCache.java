package com.perfumeadvisor.client.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.image.Image;

public final class ImageCache {

    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private ImageCache() {
    }

    public static Image get(String url, double width, double height) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String key = url + "|" + width + "x" + height;
        return CACHE.computeIfAbsent(key, k -> new Image(url, width, height, false, true, true));
    }
}
