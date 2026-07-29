package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.client.api.ApiClient;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class MainView extends BorderPane {

    public MainView(ApiClient apiClient) {
        Tab recommendationsTab = new Tab("Рекомендации", new RecommendationsPane(apiClient));
        recommendationsTab.setClosable(false);

        Tab quickSearchTab = new Tab("Быстрый поиск", new QuickSearchPane(apiClient));
        quickSearchTab.setClosable(false);

        Tab aiSearchTab = new Tab("Подбор с ИИ", new AiSearchPane(apiClient));
        aiSearchTab.setClosable(false);

        TabPane tabPane = new TabPane(recommendationsTab, quickSearchTab, aiSearchTab);

        setCenter(tabPane);
    }
}
