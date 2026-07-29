package com.perfumeadvisor.client.ui;

import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.Occasion;
import com.perfumeadvisor.common.enums.Season;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class FilterBar extends HBox {

    private final ComboBox<Occasion> occasionCombo = new ComboBox<>();
    private final ComboBox<Gender> genderCombo = new ComboBox<>();
    private final ComboBox<Season> seasonCombo = new ComboBox<>();

    public FilterBar() {
        this(true);
    }

    public FilterBar(boolean showOccasion) {
        super(10);
        getStyleClass().add("filter-bar");
        setPadding(new Insets(10));

        occasionCombo.getItems().addAll(Occasion.values());
        occasionCombo.setValue(Occasion.EVERYDAY);
        occasionCombo.setConverter(converter(Labels::of, null));

        genderCombo.getItems().add(null);
        genderCombo.getItems().addAll(Gender.values());
        genderCombo.setValue(null);
        genderCombo.setConverter(converter(Labels::of, "Любой пол"));

        seasonCombo.getItems().add(null);
        seasonCombo.getItems().addAll(Season.values());
        seasonCombo.setValue(null);
        seasonCombo.setConverter(converter(Labels::of, "Текущий сезон"));

        if (showOccasion) {
            getChildren().addAll(new Label("Повод:"), occasionCombo);
        }
        getChildren().addAll(
                new Label("Пол:"), genderCombo,
                new Label("Сезон:"), seasonCombo);
    }

    private <T> StringConverter<T> converter(java.util.function.Function<T, String> toText, String nullText) {
        return new StringConverter<>() {
            @Override
            public String toString(T value) {
                return value == null ? nullText : toText.apply(value);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }

    public Occasion getOccasion() {
        return occasionCombo.getValue();
    }

    public Gender getGender() {
        return genderCombo.getValue();
    }

    public Season getSeason() {
        return seasonCombo.getValue();
    }

    public void onGenderOrSeasonChanged(Runnable callback) {
        genderCombo.valueProperty().addListener((obs, oldValue, newValue) -> callback.run());
        seasonCombo.valueProperty().addListener((obs, oldValue, newValue) -> callback.run());
    }

    public void onAnyFilterChanged(Runnable callback) {
        onGenderOrSeasonChanged(callback);
        occasionCombo.valueProperty().addListener((obs, oldValue, newValue) -> callback.run());
    }
}
