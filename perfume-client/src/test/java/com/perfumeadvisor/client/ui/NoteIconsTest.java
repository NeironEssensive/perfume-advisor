package com.perfumeadvisor.client.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NoteIconsTest {

    @Test
    void matchesKeywordAnywhereInsideTheNoteName() {
        assertThat(NoteIcons.iconFor("Sicilian Mandarin")).isEqualTo("🍊");
        assertThat(NoteIcons.iconFor("Madagascar Vanilla")).isEqualTo("🌼");
        assertThat(NoteIcons.iconFor("Bergamot")).isEqualTo("🍋");
    }

    @Test
    void isCaseInsensitive() {
        assertThat(NoteIcons.iconFor("VANILLA")).isEqualTo(NoteIcons.iconFor("vanilla"));
    }

    @Test
    void fallsBackToAGenericLeafForUnknownNotes() {
        assertThat(NoteIcons.iconFor("Zzyzx Unknown Note")).isEqualTo("🌿");
    }
}
