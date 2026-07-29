package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.client.api.ApiClient;
import com.perfumeadvisor.common.dto.AiRecommendationRequest;
import com.perfumeadvisor.common.dto.AiRecommendationResponse;
import com.perfumeadvisor.common.enums.Occasion;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AiSearchPane extends VBox {

    private final ApiClient apiClient;
    private final TextArea descriptionArea = new TextArea();
    private final Button askButton = new Button("Спросить ИИ");
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final Label statusLabel = new Label();
    private final VBox resultBox = new VBox(10);

    public AiSearchPane(ApiClient apiClient) {
        super(10);
        this.apiClient = apiClient;
        setPadding(new Insets(15));

        descriptionArea.setPromptText("Опишите человека и повод, для кого подбираем аромат, "
                + "или назовите конкретный аромат/бренд...");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);

        askButton.setOnAction(e -> ask());

        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(20, 20);
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setText("");

        HBox controls = new HBox(10, askButton, progressIndicator, statusLabel);

        resultBox.setPadding(new Insets(10, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(resultBox);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(new Label("Описание:"), descriptionArea, controls, scrollPane);
    }

    private void ask() {
        String description = descriptionArea.getText();
        if (description == null || description.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Опишите, для кого подбираем аромат");
            alert.showAndWait();
            return;
        }

        askButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Ollama подбирает аромат...");
        resultBox.getChildren().clear();

        AiRecommendationRequest request = new AiRecommendationRequest(description, Occasion.EVERYDAY, null, null);

        Task<AiRecommendationResponse> task = new Task<>() {
            @Override
            protected AiRecommendationResponse call() {
                return apiClient.aiRecommend(request);
            }
        };

        task.setOnSucceeded(e -> {
            askButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("");
            showResult(task.getValue());
        });

        task.setOnFailed(e -> {
            askButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("");
            showError(task.getException());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showResult(AiRecommendationResponse response) {
        Label explanationLabel = new Label(response.explanation());
        explanationLabel.setWrapText(true);
        explanationLabel.getStyleClass().add("pyramid-line");

        resultBox.getChildren().add(explanationLabel);
        for (var perfume : response.perfumes()) {
            resultBox.getChildren().add(new PerfumeCard(perfume, false));
        }
    }

    private void showError(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Не удалось получить AI-рекомендацию");
        alert.setContentText(throwable.getMessage());
        alert.showAndWait();
    }
}
