package com.perfumeadvisor.client.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class UiEffects {

    private static final int EMPTY_STATE_ICON_SIZE = 48;

    private UiEffects() {
    }

    public static void fadeIn(Node node) {
        FadeTransition transition = new FadeTransition(Duration.millis(280), node);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.play();
    }

    public static VBox emptyState(String emoji, String message) {
        ImageView icon = new ImageView(EmojiRenderer.render(emoji, EMPTY_STATE_ICON_SIZE));
        icon.setFitWidth(EMPTY_STATE_ICON_SIZE);
        icon.setFitHeight(EMPTY_STATE_ICON_SIZE);
        icon.getStyleClass().add("empty-state-icon");

        Label label = new Label(message);
        label.getStyleClass().add("placeholder-label");

        VBox box = new VBox(10, icon, label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 0, 0, 0));
        return box;
    }
}
