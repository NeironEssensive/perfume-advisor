package com.perfumeadvisor.client.ui;

public enum SortOption {
    RELEVANCE("Как на сервере"),
    RATING_DESC("Рейтинг: сначала высокий"),
    RATING_ASC("Рейтинг: сначала низкий"),
    PRICE_DESC("Цена: сначала дорогие"),
    PRICE_ASC("Цена: сначала дешёвые"),
    SEASON_SCORE("По сезону"),
    OCCASION_SCORE("По поводу"),
    NAME("По названию");

    private final String label;

    SortOption(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
