package com.perfumeadvisor.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PerfumeClientApplication extends Application {

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane(new Label("Perfume Advisor"));
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Perfume Advisor");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
