package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.client.api.ApiClient;
import com.perfumeadvisor.client.favorites.FavoritesStore;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class MainView extends BorderPane {

    public MainView(ApiClient apiClient, FavoritesStore favoritesStore) {
        Tab recommendationsTab = new Tab("Рекомендации", new RecommendationsPane(apiClient, favoritesStore));
        recommendationsTab.setClosable(false);

        Tab quickSearchTab = new Tab("Быстрый поиск", new QuickSearchPane(apiClient, favoritesStore));
        quickSearchTab.setClosable(false);

        Tab aiSearchTab = new Tab("Подбор с ИИ", new AiSearchPane(apiClient, favoritesStore));
        aiSearchTab.setClosable(false);

        FavoritesPane favoritesPane = new FavoritesPane(favoritesStore);
        Tab favoritesTab = new Tab("Избранное", favoritesPane);
        favoritesTab.setClosable(false);

        TabPane tabPane = new TabPane(recommendationsTab, quickSearchTab, aiSearchTab, favoritesTab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == favoritesTab) {
                favoritesPane.refresh();
            }
        });

        setCenter(tabPane);
    }
}
