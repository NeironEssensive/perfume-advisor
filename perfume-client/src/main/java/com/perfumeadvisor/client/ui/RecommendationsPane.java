package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.client.api.ApiClient;
import com.perfumeadvisor.client.favorites.FavoritesStore;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import com.perfumeadvisor.common.enums.Occasion;
import java.util.List;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RecommendationsPane extends VBox {

    private static final int SECTION_LIMIT = 20;

    private final ApiClient apiClient;
    private final FavoritesStore favoritesStore;
    private final FilterBar filterBar = new FilterBar(false);
    private final VBox resultsBox = new VBox(10);
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final ComboBox<SortOption> sortCombo = new ComboBox<>();
    private List<PerfumeRecommendationDto> currentResults = List.of();
    private Occasion selectedOccasion;

    public RecommendationsPane(ApiClient apiClient, FavoritesStore favoritesStore) {
        super(10);
        this.apiClient = apiClient;
        this.favoritesStore = favoritesStore;
        setPadding(new Insets(15));

        FlowPane occasionButtons = new FlowPane(10, 10);
        for (Occasion occasion : Occasion.values()) {
            Button button = new Button(Labels.of(occasion));
            button.setOnAction(e -> load(occasion));
            occasionButtons.getChildren().add(button);
        }

        sortCombo.getItems().addAll(SortOption.values());
        sortCombo.setValue(SortOption.RELEVANCE);
        sortCombo.valueProperty().addListener((obs, oldValue, newValue) -> onSortChanged());

        HBox sortBar = new HBox(10, new Label("Сортировка:"), sortCombo, progressIndicator);

        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(20, 20);

        resultsBox.setPadding(new Insets(5, 0, 0, 0));
        showPlaceholder("👆", "Выберите повод выше");

        ScrollPane scrollPane = new ScrollPane(resultsBox);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(filterBar, occasionButtons, sortBar, scrollPane);

        filterBar.onGenderOrSeasonChanged(() -> {
            if (selectedOccasion != null) {
                load(selectedOccasion);
            }
        });
    }

    private void showPlaceholder(String emoji, String text) {
        resultsBox.getChildren().setAll(UiEffects.emptyState(emoji, text));
    }

    private void onSortChanged() {
        if (selectedOccasion != null && SortSupport.isServerSortable(sortCombo.getValue())) {
            load(selectedOccasion);
        } else {
            render();
        }
    }

    private void load(Occasion occasion) {
        selectedOccasion = occasion;
        progressIndicator.setVisible(true);
        showPlaceholder("⏳", "Загрузка...");

        var serverSort = SortSupport.toServerSort(sortCombo.getValue());
        Task<List<PerfumeRecommendationDto>> task = new Task<>() {
            @Override
            protected List<PerfumeRecommendationDto> call() {
                return apiClient.recommend(
                        occasion, filterBar.getGender(), filterBar.getSeason(), SECTION_LIMIT, serverSort);
            }
        };

        task.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            currentResults = task.getValue();
            render();
        });

        task.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            showPlaceholder("⚠️", "Не удалось загрузить");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void render() {
        if (currentResults.isEmpty()) {
            showPlaceholder("🔍", "Ничего не найдено");
            return;
        }

        List<PerfumeRecommendationDto> sorted =
                currentResults.stream().sorted(SortSupport.comparatorFor(sortCombo.getValue())).toList();

        resultsBox.getChildren().setAll(sorted.stream()
                .map(p -> new PerfumeRow(p, perfume -> PerfumeDetailDialog.show(perfume, favoritesStore),
                        favoritesStore, null))
                .toList());
        UiEffects.fadeIn(resultsBox);
    }
}
