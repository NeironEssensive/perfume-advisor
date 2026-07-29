package com.perfumeadvisor.client.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public final class EmojiRenderer {

    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private EmojiRenderer() {
    }

    public static Image render(String emoji, int size) {
        String key = emoji + "|" + size;
        return CACHE.computeIfAbsent(key, k -> renderToImage(emoji, size));
    }

    private static Image renderToImage(String emoji, int size) {
        BufferedImage buffered = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = buffered.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font font = new Font("Segoe UI Emoji", Font.PLAIN, size - 6);
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics();
        int x = (size - metrics.stringWidth(emoji)) / 2;
        int y = (size - metrics.getHeight()) / 2 + metrics.getAscent();
        graphics.drawString(emoji, x, y);
        graphics.dispose();

        return SwingFXUtils.toFXImage(buffered, null);
    }
}
