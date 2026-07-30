package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.client.favorites.FavoritesStore;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class PerfumeRow extends HBox {

    private static final double IMAGE_WIDTH = 55;
    private static final double IMAGE_HEIGHT = 75;

    public PerfumeRow(
            PerfumeRecommendationDto perfume,
            Consumer<PerfumeRecommendationDto> onClick,
            FavoritesStore favoritesStore,
            Runnable onFavoriteToggled) {
        super(12);
        getStyleClass().add("perfume-card");
        setPadding(new Insets(8, 12, 8, 8));
        setOnMouseClicked(e -> onClick.accept(perfume));

        getChildren().addAll(
                buildImage(perfume.imageUrl()), buildInfo(perfume),
                buildFavoriteButton(perfume, favoritesStore, onFavoriteToggled));
    }

    private ImageView buildImage(String imageUrl) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(IMAGE_WIDTH);
        imageView.setFitHeight(IMAGE_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("perfume-image");

        Rectangle clip = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        imageView.setClip(clip);

        imageView.setImage(ImageCache.get(imageUrl, IMAGE_WIDTH, IMAGE_HEIGHT));
        return imageView;
    }

    private VBox buildInfo(PerfumeRecommendationDto perfume) {
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(perfume.brand() + " — " + perfume.name());
        title.getStyleClass().add("perfume-title");
        title.setWrapText(true);

        FlowPane meta = new FlowPane(8, 6);
        meta.getChildren().add(chip(genderLabel(perfume.gender())));
        if (perfume.ratingValue() != null) {
            meta.getChildren().add(chip(String.format("★ %.2f", perfume.ratingValue())));
        }
        meta.getChildren().add(chip(perfume.price() != null ? "≈ " + perfume.price() + " ₽" : "Цена уточняется"));
        if (perfume.seasonScore() != null) {
            meta.getChildren().add(chip("Сезон: " + perfume.seasonScore()));
        }
        if (perfume.occasionScore() != null) {
            meta.getChildren().add(chip("Повод: " + perfume.occasionScore()));
        }

        info.getChildren().addAll(title, meta);
        return info;
    }

    private Button buildFavoriteButton(
            PerfumeRecommendationDto perfume, FavoritesStore favoritesStore, Runnable onFavoriteToggled) {
        Button button = new Button();
        button.getStyleClass().add("favorite-toggle");
        updateFavoriteButtonLabel(button, favoritesStore.isFavorite(perfume.id()));

        button.setOnMouseClicked(e -> {
            favoritesStore.toggle(perfume);
            updateFavoriteButtonLabel(button, favoritesStore.isFavorite(perfume.id()));
            if (onFavoriteToggled != null) {
                onFavoriteToggled.run();
            }
            e.consume();
        });
        return button;
    }

    private void updateFavoriteButtonLabel(Button button, boolean isFavorite) {
        button.setText(isFavorite ? "★" : "☆");
        button.getStyleClass().removeAll("favorite-toggle-active");
        if (isFavorite) {
            button.getStyleClass().add("favorite-toggle-active");
        }
    }

    private Label chip(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("chip");
        return label;
    }

    private String genderLabel(String gender) {
        if (gender == null) {
            return "Унисекс";
        }
        return switch (gender) {
            case "MALE" -> "Мужской";
            case "FEMALE" -> "Женский";
            default -> "Унисекс";
        };
    }
}
