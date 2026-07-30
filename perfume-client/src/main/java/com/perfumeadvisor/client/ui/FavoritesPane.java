package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.client.favorites.FavoritesStore;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FavoritesPane extends VBox {

    private final FavoritesStore favoritesStore;
    private final VBox resultsBox = new VBox(10);

    public FavoritesPane(FavoritesStore favoritesStore) {
        super(10);
        this.favoritesStore = favoritesStore;
        setPadding(new Insets(15));

        resultsBox.setPadding(new Insets(5, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(resultsBox);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().add(scrollPane);
        refresh();
    }

    public void refresh() {
        var favorites = favoritesStore.all();
        if (favorites.isEmpty()) {
            resultsBox.getChildren().setAll(UiEffects.emptyState("💔", "Пока нет избранных ароматов"));
            return;
        }

        resultsBox.getChildren().setAll(favorites.stream()
                .map(p -> new PerfumeRow(p, perfume -> PerfumeDetailDialog.show(perfume, favoritesStore),
                        favoritesStore, this::refresh))
                .toList());
        UiEffects.fadeIn(resultsBox);
    }
}
