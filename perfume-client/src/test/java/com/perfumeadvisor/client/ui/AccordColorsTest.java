package com.perfumeadvisor.client.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class AccordColorsTest {

    @Test
    void matchesExactAccordNamesFirst() {
        assertThat(AccordColors.colorFor("citrus")).isEqualTo("#f5d90a");
        assertThat(AccordColors.colorFor("Fresh Spicy")).isEqualTo("#7ed957");
    }

    @Test
    void isDeterministicForUnknownAccords() {
        String first = AccordColors.colorFor("some completely unlisted accord");
        String second = AccordColors.colorFor("some completely unlisted accord");

        assertThat(first).isEqualTo(second);
        assertThat(List.of(
                "#f5d90a", "#ff6fae", "#4fd1c5", "#ff8c42", "#38bdf8", "#22c55e", "#a78bfa",
                "#e8543e", "#c4a7e7", "#7ed957", "#f2a900", "#0ea5e9", "#d1274c", "#8b5a2b"))
                .contains(first);
    }

    @Test
    void picksDarkTextForLightBackgroundsAndLightTextForDarkBackgrounds() {
        assertThat(AccordColors.textColorFor("#f5d90a")).isEqualTo("#1c1b22");
        assertThat(AccordColors.textColorFor("#4a2c17")).isEqualTo("#f2f1f7");
    }
}
