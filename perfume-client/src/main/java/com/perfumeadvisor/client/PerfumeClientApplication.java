package com.perfumeadvisor.client;

import com.perfumeadvisor.client.api.ApiClient;
import com.perfumeadvisor.client.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PerfumeClientApplication extends Application {

    @Override
    public void start(Stage stage) {
        String baseUrl = System.getProperty("backend.url", "http://localhost:8080");
        ApiClient apiClient = new ApiClient(baseUrl);

        MainView mainView = new MainView(apiClient);
        Scene scene = new Scene(mainView, 960, 700);
        scene.setFill(Color.web("#1a1723"));
        scene.getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.setTitle("Perfume Advisor");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
