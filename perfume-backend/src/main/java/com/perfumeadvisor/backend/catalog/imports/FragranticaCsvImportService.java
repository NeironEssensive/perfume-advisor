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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Импорт открытого датасета "Fragrantica.com Fragrance Dataset" (Kaggle) в каталог.
 *
 * <p>Ожидаемые колонки CSV: {@code Perfume}, {@code Brand}, {@code Country}, {@code Gender},
 * {@code Year}, {@code Rating Value}, {@code Rating Count}, {@code Top}, {@code Middle},
 * {@code Base}, {@code Perfume Accords}, {@code Description}, {@code Image URL}. Названия колонок
 * могут отличаться в конкретной версии датасета на Kaggle — при расхождении нужно свериться
 * с реальным заголовком файла и поправить константы ниже.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FragranticaCsvImportService {

    private final BrandRepository brandRepository;
    private final PerfumeRepository perfumeRepository;
    private final NoteRepository noteRepository;
    private final AccordRepository accordRepository;
    private final PerfumeNoteRepository perfumeNoteRepository;
    private final PerfumeAccordRepository perfumeAccordRepository;

    @Transactional
    public ImportResult importFromCsv(Path csvPath) throws IOException {
        int imported = 0;
        int skipped = 0;

        CSVFormat format = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (Reader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            for (CSVRecord record : parser) {
                try {
                    importRow(record);
                    imported++;
                } catch (Exception e) {
                    skipped++;
                    log.warn("Пропущена строка {}: {}", record.getRecordNumber(), e.getMessage());
                }
            }
        }

        return new ImportResult(imported, skipped);
    }

    private void importRow(CSVRecord record) {
        String perfumeName = requireField(record, "Perfume");
        String brandName = requireField(record, "Brand");

        Brand brand = brandRepository.findByNameIgnoreCase(brandName)
                .orElseGet(() -> brandRepository.save(Brand.builder()
                        .name(brandName)
                        .country(getOrNull(record, "Country"))
                        .build()));

        if (perfumeRepository.findByNameIgnoreCaseAndBrand(perfumeName, brand).isPresent()) {
            throw new IllegalStateException("уже импортирован: " + brandName + " / " + perfumeName);
        }

        Perfume perfume = Perfume.builder()
                .brand(brand)
                .name(perfumeName)
                .releaseYear(parseYear(getOrNull(record, "Year")))
                .gender(parseGender(getOrNull(record, "Gender")))
                .description(getOrNull(record, "Description"))
                .imageUrl(getOrNull(record, "Image URL"))
                .ratingValue(parseDouble(getOrNull(record, "Rating Value")))
                .ratingCount(parseInt(getOrNull(record, "Rating Count")))
                .build();
        perfume = perfumeRepository.save(perfume);

        linkNotes(perfume, getOrNull(record, "Top"), PyramidPosition.TOP);
        linkNotes(perfume, getOrNull(record, "Middle"), PyramidPosition.MIDDLE);
        linkNotes(perfume, getOrNull(record, "Base"), PyramidPosition.BASE);

        linkAccords(perfume, getOrNull(record, "Perfume Accords"));
    }

    private void linkNotes(Perfume perfume, String rawNotes, PyramidPosition position) {
        for (String noteName : splitListField(rawNotes)) {
            Note note = noteRepository.findByNameIgnoreCase(noteName)
                    .orElseGet(() -> noteRepository.save(Note.builder().name(noteName).build()));
            perfumeNoteRepository.save(PerfumeNote.builder()
                    .perfume(perfume)
                    .note(note)
                    .pyramidPosition(position)
                    .build());
        }
    }

    private void linkAccords(Perfume perfume, String rawAccords) {
        List<String> accordNames = splitListField(rawAccords);
        int rank = 0;
        for (String accordName : accordNames) {
            Accord accord = accordRepository.findByNameIgnoreCase(accordName)
                    .orElseGet(() -> accordRepository.save(Accord.builder().name(accordName).build()));
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
     * встречается в некоторых версиях датасета.
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
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private Gender parseGender(String raw) {
        if (raw == null) {
            return Gender.UNISEX;
        }
        String normalized = raw.strip().toLowerCase();
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

    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.strip());
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
