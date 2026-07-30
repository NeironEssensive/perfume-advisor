package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.client.api.ApiClient;
import com.perfumeadvisor.client.favorites.FavoritesStore;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class QuickSearchPane extends VBox {

    private static final int RESULT_LIMIT = 50;

    private final ApiClient apiClient;
    private final FavoritesStore favoritesStore;
    private final FilterBar filterBar = new FilterBar(true);
    private final VBox resultsBox = new VBox(10);
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortCombo = new ComboBox<>();
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(350));
    private List<PerfumeRecommendationDto> currentResults = List.of();
    private boolean searchMode = false;

    public QuickSearchPane(ApiClient apiClient, FavoritesStore favoritesStore) {
        super(10);
        this.apiClient = apiClient;
        this.favoritesStore = favoritesStore;
        setPadding(new Insets(15));

        Button searchButton = new Button("Найти");
        searchButton.setOnAction(e -> loadRecommendations());

        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(20, 20);

        HBox controls = new HBox(10, searchButton, progressIndicator);

        searchField.setPromptText("Поиск по всему каталогу: бренд или название...");
        searchDebounce.setOnFinished(e -> onSearchTextSettled());
        searchField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());

        sortCombo.getItems().addAll(SortOption.values());
        sortCombo.setValue(SortOption.RELEVANCE);
        sortCombo.valueProperty().addListener((obs, oldValue, newValue) -> onSortChanged());

        HBox refineBar = new HBox(10, searchField, new Label("Сортировка:"), sortCombo);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        resultsBox.setPadding(new Insets(5, 0, 0, 0));
        showPlaceholder("⏳", "Загрузка...");

        ScrollPane scrollPane = new ScrollPane(resultsBox);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(filterBar, controls, refineBar, scrollPane);

        loadRecommendations();

        filterBar.onAnyFilterChanged(() -> {
            if (!searchMode) {
                loadRecommendations();
            }
        });
    }

    private void showPlaceholder(String emoji, String text) {
        resultsBox.getChildren().setAll(UiEffects.emptyState(emoji, text));
    }

    private void onSearchTextSettled() {
        String text = searchField.getText() == null ? "" : searchField.getText().strip();
        if (text.isBlank()) {
            loadRecommendations();
        } else {
            runSearch(text);
        }
    }

    private void loadRecommendations() {
        searchMode = false;
        progressIndicator.setVisible(true);
        showPlaceholder("⏳", "Загрузка...");

        int limit = RESULT_LIMIT;
        var serverSort = SortSupport.toServerSort(sortCombo.getValue());
        Task<List<PerfumeRecommendationDto>> task = new Task<>() {
            @Override
            protected List<PerfumeRecommendationDto> call() {
                return apiClient.recommend(
                        filterBar.getOccasion(), filterBar.getGender(), filterBar.getSeason(), limit, serverSort);
            }
        };
        runTask(task);
    }

    private void runSearch(String query) {
        searchMode = true;
        progressIndicator.setVisible(true);
        showPlaceholder("🔍", "Поиск...");

        int limit = RESULT_LIMIT;
        Task<List<PerfumeRecommendationDto>> task = new Task<>() {
            @Override
            protected List<PerfumeRecommendationDto> call() {
                return apiClient.search(query, limit);
            }
        };
        runTask(task);
    }

    private void onSortChanged() {
        if (!searchMode && SortSupport.isServerSortable(sortCombo.getValue())) {
            loadRecommendations();
        } else {
            render();
        }
    }

    private void runTask(Task<List<PerfumeRecommendationDto>> task) {
        task.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            currentResults = task.getValue();
            render();
        });

        task.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            showPlaceholder("⚠️", "Не удалось загрузить данные");
            showError(task.getException());
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

    private void showError(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Не удалось получить данные");
        alert.setContentText(throwable.getMessage());
        alert.showAndWait();
    }
}
