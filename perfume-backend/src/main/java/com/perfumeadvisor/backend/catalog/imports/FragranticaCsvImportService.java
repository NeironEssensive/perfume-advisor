package com.perfumeadvisor.backend.catalog.imports;

import com.perfumeadvisor.backend.catalog.domain.Accord;
import com.perfumeadvisor.backend.catalog.domain.Brand;
import com.perfumeadvisor.backend.catalog.domain.Note;
import com.perfumeadvisor.backend.catalog.domain.Perfume;
import com.perfumeadvisor.backend.catalog.domain.PerfumeAccord;
import com.perfumeadvisor.backend.catalog.domain.PerfumeNote;
import com.perfumeadvisor.backend.catalog.repository.AccordRepository;
import com.perfumeadvisor.backend.catalog.repository.BrandRepository;
import com.perfumeadvisor.backend.catalog.repository.NoteRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeAccordRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeNoteRepository;
import com.perfumeadvisor.backend.catalog.repository.PerfumeRepository;
import com.perfumeadvisor.common.enums.Gender;
import com.perfumeadvisor.common.enums.PyramidPosition;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Импорт открытого датасета "Fragrantica.com Fragrance Dataset" (Kaggle, {@code fra_cleaned.csv})
 * в каталог.
 *
 * <p>Реальные колонки файла (разделитель {@code ;}): {@code url}, {@code Perfume}, {@code Brand},
 * {@code Country}, {@code Gender}, {@code Rating Value}, {@code Rating Count}, {@code Year},
 * {@code Top}, {@code Middle}, {@code Base}, {@code Perfumer1}, {@code Perfumer2},
 * {@code mainaccord1}..{@code mainaccord5}. {@code Perfume}/{@code Brand} приходят как URL-слаги
 * ("jean-paul-gaultier") — приводятся к читаемому виду. {@code Rating Value} использует запятую
 * как десятичный разделитель. Описание и фото в этой версии датасета отсутствуют — сохраняем
 * {@code url} страницы, чтобы можно было дозаполнить их позже.
 *
 * <p>Brand/Note/Accord резолвятся через in-memory кэш, а не через {@code findByNameIgnoreCase}
 * на каждой строке: SELECT перед INSERT форсирует flush и полностью ломает батчинг вставок
 * в Hibernate, а бренды/ноты/аккорды — небольшой закрытый набор значений, переиспользуемый
 * в тысячах строк.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FragranticaCsvImportService {

    private static final String[] ACCORD_COLUMNS = {
        "mainaccord1", "mainaccord2", "mainaccord3", "mainaccord4", "mainaccord5"
    };

    /** Файл {@code fra_cleaned.csv} сохранён в Windows-1252, не в UTF-8 (иначе валится на акцентах). */
    private static final Charset CSV_CHARSET = Charset.forName("windows-1252");

    private final BrandRepository brandRepository;
    private final PerfumeRepository perfumeRepository;
    private final NoteRepository noteRepository;
    private final AccordRepository accordRepository;
    private final PerfumeNoteRepository perfumeNoteRepository;
    private final PerfumeAccordRepository perfumeAccordRepository;

    @Transactional
    public ImportResult importFromCsv(Path csvPath) throws IOException {
        Map<String, Brand> brandCache = new HashMap<>();
        brandRepository.findAll().forEach(b -> brandCache.put(key(b.getName()), b));

        Map<String, Note> noteCache = new HashMap<>();
        noteRepository.findAll().forEach(n -> noteCache.put(key(n.getName()), n));

        Map<String, Accord> accordCache = new HashMap<>();
        accordRepository.findAll().forEach(a -> accordCache.put(key(a.getName()), a));

        Set<String> seenPerfumes = new HashSet<>();

        int imported = 0;
        int skipped = 0;

        CSVFormat format = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (Reader reader = Files.newBufferedReader(csvPath, CSV_CHARSET);
             CSVParser parser = format.parse(reader)) {
            for (CSVRecord record : parser) {
                try {
                    importRow(record, brandCache, noteCache, accordCache, seenPerfumes);
                    imported++;
                } catch (Exception e) {
                    skipped++;
                    log.warn("Пропущена строка {}: {}", record.getRecordNumber(), e.getMessage());
                }
            }
        }

        return new ImportResult(imported, skipped);
    }

    private void importRow(
            CSVRecord record,
            Map<String, Brand> brandCache,
            Map<String, Note> noteCache,
            Map<String, Accord> accordCache,
            Set<String> seenPerfumes) {
        String perfumeName = humanize(requireField(record, "Perfume"));
        String brandName = humanize(requireField(record, "Brand"));

        Brand brand = brandCache.computeIfAbsent(key(brandName), k -> brandRepository.save(Brand.builder()
                .name(brandName)
                .country(getOrNull(record, "Country"))
                .build()));

        if (!seenPerfumes.add(key(brandName) + "::" + key(perfumeName))) {
            throw new IllegalStateException("уже импортирован: " + brandName + " / " + perfumeName);
        }

        Perfume perfume = Perfume.builder()
                .brand(brand)
                .name(perfumeName)
                .releaseYear(parseYear(getOrNull(record, "Year")))
                .gender(parseGender(getOrNull(record, "Gender")))
                .description(getOrNull(record, "Description"))
                .imageUrl(getOrNull(record, "Image URL"))
                .sourceUrl(getOrNull(record, "url"))
                .ratingValue(parseDouble(getOrNull(record, "Rating Value")))
                .ratingCount(parseInt(getOrNull(record, "Rating Count")))
                .build();
        perfume = perfumeRepository.save(perfume);

        linkNotes(perfume, getOrNull(record, "Top"), PyramidPosition.TOP, noteCache);
        linkNotes(perfume, getOrNull(record, "Middle"), PyramidPosition.MIDDLE, noteCache);
        linkNotes(perfume, getOrNull(record, "Base"), PyramidPosition.BASE, noteCache);

        linkAccords(perfume, record, accordCache);
    }

    private void linkNotes(
            Perfume perfume, String rawNotes, PyramidPosition position, Map<String, Note> noteCache) {
        for (String noteName : splitListField(rawNotes)) {
            Note note = noteCache.computeIfAbsent(
                    key(noteName), k -> noteRepository.save(Note.builder().name(noteName).build()));
            perfumeNoteRepository.save(PerfumeNote.builder()
                    .perfume(perfume)
                    .note(note)
                    .pyramidPosition(position)
                    .build());
        }
    }

    /**
     * В отличие от нот, аккорды в этом датасете лежат не одним списком, а в отдельных колонках
     * {@code mainaccord1}..{@code mainaccord5}, уже упорядоченных по убыванию силы аккорда.
     */
    private void linkAccords(Perfume perfume, CSVRecord record, Map<String, Accord> accordCache) {
        int rank = 0;
        for (String column : ACCORD_COLUMNS) {
            String accordName = getOrNull(record, column);
            if (accordName == null || accordName.equalsIgnoreCase("unknown")) {
                continue;
            }
            Accord accord = accordCache.computeIfAbsent(
                    key(accordName), k -> accordRepository.save(Accord.builder().name(accordName).build()));
            int strength = Math.max(100 - rank * 15, 10);
            perfumeAccordRepository.save(PerfumeAccord.builder()
                    .perfume(perfume)
                    .accord(accord)
                    .strength(strength)
                    .build());
            rank++;
        }
    }

    /**
     * Разбивает значение ячейки на список: поддерживает как обычную строку через запятую
     * ("Bergamot, Lemon"), так и python-репр списка ("['Bergamot', 'Lemon']"), который
     * встречается в некоторых версиях датасета. Значение-заглушка "unknown" отбрасывается.
     */
    private List<String> splitListField(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String cleaned = raw.strip();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return Arrays.stream(cleaned.split(","))
                .map(s -> s.strip().replaceAll("^['\"]|['\"]$", ""))
                .filter(s -> !s.isBlank() && !s.equalsIgnoreCase("unknown"))
                .distinct()
                .toList();
    }

    /**
     * Приводит URL-слаг ("jean-paul-gaultier") к читаемому виду ("Jean Paul Gaultier").
     * Эвристика — не всегда даёт идеальный регистр (аббревиатуры вроде "YSL"), такие случаи
     * можно поправить вручную после импорта.
     */
    private static String humanize(String slug) {
        String[] words = slug.split("[-_]+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    /** Ключ для in-memory кэшей/множеств — регистронезависимое сравнение имён. */
    private static String key(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private Gender parseGender(String raw) {
        if (raw == null) {
            return Gender.UNISEX;
        }
        String normalized = raw.strip().toLowerCase(Locale.ROOT);
        if (normalized.contains("women") || normalized.contains("female")) {
            return Gender.FEMALE;
        }
        if (normalized.contains("men") || normalized.contains("male")) {
            return Gender.MALE;
        }
        return Gender.UNISEX;
    }

    private Integer parseYear(String raw) {
        Double value = parseDouble(raw);
        return value == null ? null : value.intValue();
    }

    private Integer parseInt(String raw) {
        Double value = parseDouble(raw);
        return value == null ? null : value.intValue();
    }

    /**
     * В датасете десятичный разделитель — запятая ("1,42"), поэтому нормализуем перед парсингом.
     */
    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.strip().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String requireField(CSVRecord record, String column) {
        String value = getOrNull(record, column);
        if (value == null) {
            throw new IllegalArgumentException("нет обязательного поля '" + column + "'");
        }
        return value;
    }

    private static String getOrNull(CSVRecord record, String column) {
        if (!record.isMapped(column)) {
            return null;
        }
        String value = record.get(column);
        return (value == null || value.isBlank()) ? null : value.strip();
    }
}
