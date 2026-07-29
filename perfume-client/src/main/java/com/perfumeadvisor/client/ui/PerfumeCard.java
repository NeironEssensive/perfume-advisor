package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.common.dto.AccordDto;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class PerfumeCard extends HBox {

    private static final double IMAGE_WIDTH = 130;
    private static final double IMAGE_HEIGHT = 175;
    private static final double ACCORD_BAR_MAX_WIDTH = 260;
    private static final double ACCORD_BAR_HEIGHT = 24;

    public PerfumeCard(PerfumeRecommendationDto perfume, boolean showScores) {
        super(18);
        getStyleClass().add("perfume-card");
        setPadding(new Insets(14));

        getChildren().addAll(buildImage(perfume.imageUrl()), buildInfo(perfume, showScores));
    }

    private ImageView buildImage(String imageUrl) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(IMAGE_WIDTH);
        imageView.setFitHeight(IMAGE_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("perfume-image");

        Rectangle clip = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        imageView.setClip(clip);

        imageView.setImage(ImageCache.get(imageUrl, IMAGE_WIDTH, IMAGE_HEIGHT));
        return imageView;
    }

    private VBox buildInfo(PerfumeRecommendationDto perfume, boolean showScores) {
        VBox info = new VBox(10);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(perfume.brand() + " — " + perfume.name());
        title.getStyleClass().add("perfume-title");
        title.setWrapText(true);

        FlowPane meta = new FlowPane(10, 8);
        meta.getChildren().add(chip(genderLabel(perfume.gender())));
        if (perfume.ratingValue() != null) {
            meta.getChildren().add(chip(String.format("★ %.2f (%d)", perfume.ratingValue(),
                    perfume.ratingCount() == null ? 0 : perfume.ratingCount())));
        }
        if (showScores && perfume.seasonScore() != null) {
            meta.getChildren().add(chip("Сезон: " + perfume.seasonScore()));
        }
        if (showScores && perfume.occasionScore() != null) {
            meta.getChildren().add(chip("Повод: " + perfume.occasionScore()));
        }
        Label price = new Label(perfume.price() != null ? "≈ " + perfume.price() + " ₽" : "Цена уточняется");
        price.getStyleClass().add("price-tag");
        meta.getChildren().add(price);

        info.getChildren().addAll(title, meta, separator());

        addNotesSection(info, "Верхние ноты", perfume.topNotes());
        addNotesSection(info, "Средние ноты", perfume.middleNotes());
        addNotesSection(info, "Базовые ноты", perfume.baseNotes());
        if (hasAnyNotes(perfume)) {
            info.getChildren().add(separator());
        }

        if (perfume.accords() != null && !perfume.accords().isEmpty()) {
            Label accordsLabel = new Label("Аккорды");
            accordsLabel.getStyleClass().add("section-label");
            info.getChildren().addAll(accordsLabel, buildAccordBars(perfume.accords()));
        }

        if (perfume.sourceUrl() != null) {
            Hyperlink link = new Hyperlink("Открыть на Fragrantica");
            link.setOnAction(e -> openInBrowser(perfume.sourceUrl()));
            info.getChildren().add(link);
        }

        return info;
    }

    private Separator separator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("card-divider");
        return separator;
    }

    private boolean hasAnyNotes(PerfumeRecommendationDto perfume) {
        return !isEmpty(perfume.topNotes()) || !isEmpty(perfume.middleNotes()) || !isEmpty(perfume.baseNotes());
    }

    private boolean isEmpty(List<String> notes) {
        return notes == null || notes.isEmpty();
    }

    private void addNotesSection(VBox info, String caption, List<String> notes) {
        if (isEmpty(notes)) {
            return;
        }
        Label header = new Label(caption.toUpperCase());
        header.getStyleClass().add("section-label");

        FlowPane grid = new FlowPane(12, 10);
        for (String note : notes) {
            grid.getChildren().add(buildNoteCell(note));
        }

        info.getChildren().addAll(header, grid);
    }

    private static final int NOTE_ICON_SIZE = 32;

    private VBox buildNoteCell(String note) {
        ImageView icon = new ImageView(EmojiRenderer.render(NoteIcons.iconFor(note), NOTE_ICON_SIZE));
        icon.setFitWidth(NOTE_ICON_SIZE);
        icon.setFitHeight(NOTE_ICON_SIZE);

        Label name = new Label(note);
        name.getStyleClass().add("note-item");
        name.setWrapText(true);
        name.setMaxWidth(80);
        name.setAlignment(Pos.CENTER);
        name.setStyle("-fx-text-alignment: center;");

        VBox cell = new VBox(4, icon, name);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setMaxWidth(80);
        return cell;
    }

    private VBox buildAccordBars(List<AccordDto> accords) {
        VBox bars = new VBox(4);
        int maxStrength = accords.stream().mapToInt(AccordDto::strength).max().orElse(1);

        for (AccordDto accord : accords) {
            double ratio = maxStrength == 0 ? 1 : (double) accord.strength() / maxStrength;
            double width = Math.max(ACCORD_BAR_MAX_WIDTH * ratio, 60);

            String color = AccordColors.colorFor(accord.name());
            Rectangle bar = new Rectangle(width, ACCORD_BAR_HEIGHT);
            bar.setArcWidth(8);
            bar.setArcHeight(8);
            bar.setStyle("-fx-fill: " + color + ";");

            Label label = new Label(accord.name());
            label.setStyle("-fx-text-fill: " + AccordColors.textColorFor(color)
                    + "; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 0 0 0 10;");

            StackPane barPane = new StackPane(bar, label);
            barPane.setPrefWidth(ACCORD_BAR_MAX_WIDTH);
            StackPane.setAlignment(bar, Pos.CENTER_LEFT);
            StackPane.setAlignment(label, Pos.CENTER_LEFT);

            bars.getChildren().add(barPane);
        }
        return bars;
    }

    private void openInBrowser(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Не удалось открыть ссылку: " + e.getMessage()).showAndWait();
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
