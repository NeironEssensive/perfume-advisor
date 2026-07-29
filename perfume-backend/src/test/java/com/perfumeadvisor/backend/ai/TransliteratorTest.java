package com.perfumeadvisor.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransliteratorTest {

    @Test
    void transliteratesCommonBrandNamesPhonetically() {
        assertThat(Transliterator.toLatin("форд")).isEqualTo("ford");
        assertThat(Transliterator.toLatin("шанель")).isEqualTo("shanel");
        assertThat(Transliterator.toLatin("гуччи")).isEqualTo("guchchi");
    }

    @Test
    void handlesGenitiveCaseEndingsAsExtraTrailingLetters() {
        assertThat(Transliterator.toLatin("форда")).isEqualTo("forda");
        assertThat(Transliterator.toLatin("тома")).isEqualTo("toma");
    }

    @Test
    void lowercasesInput() {
        assertThat(Transliterator.toLatin("ФОРД")).isEqualTo("ford");
    }

    @Test
    void leavesLatinCharactersUnchanged() {
        assertThat(Transliterator.toLatin("tom ford")).isEqualTo("tom ford");
    }

    @Test
    void dropsSoftAndHardSigns() {
        assertThat(Transliterator.toLatin("рояль")).isEqualTo("royal");
    }
}
