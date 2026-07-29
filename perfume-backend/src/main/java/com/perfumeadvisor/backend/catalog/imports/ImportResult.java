package com.perfumeadvisor.backend.catalog.imports;

/**
 * Итог импорта: сколько парфюмов добавлено и сколько строк пропущено (дубликаты, нет обязательных полей).
 */
public record ImportResult(int imported, int skipped) {
}
