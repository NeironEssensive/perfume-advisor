package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.client.favorites.FavoritesStore;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class PerfumeDetailDialog {

    private PerfumeDetailDialog() {
    }

    public static void show(PerfumeRecommendationDto perfume, FavoritesStore favoritesStore) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(perfume.brand() + " — " + perfume.name());

        PerfumeCard card = new PerfumeCard(perfume, true, favoritesStore);

        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> stage.close());

        VBox root = new VBox(15, card, closeButton);
        root.setPadding(new Insets(15));
        root.getStyleClass().add("root");

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("root");

        Scene scene = new Scene(scrollPane, 760, 560);
        scene.setFill(Color.web("#1a1723"));
        scene.getStylesheets().add(PerfumeDetailDialog.class.getResource("/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().add(new Image(PerfumeDetailDialog.class.getResourceAsStream("/icon.png")));
        stage.setResizable(false);
        stage.show();
    }
}
