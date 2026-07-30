package com.perfumeadvisor.client.favorites;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfumeadvisor.common.dto.PerfumeRecommendationDto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FavoritesStore {

    private static final Path FILE = Path.of(System.getProperty("user.home"), ".perfume-advisor", "favorites.json");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, PerfumeRecommendationDto> favorites = new LinkedHashMap<>();

    public FavoritesStore() {
        load();
    }

    private void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            List<PerfumeRecommendationDto> stored =
                    objectMapper.readValue(FILE.toFile(), new TypeReference<List<PerfumeRecommendationDto>>() {});
            for (PerfumeRecommendationDto perfume : stored) {
                favorites.put(perfume.id(), perfume);
            }
        } catch (IOException e) {
            favorites.clear();
        }
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), List.copyOf(favorites.values()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isFavorite(long id) {
        return favorites.containsKey(id);
    }

    public void toggle(PerfumeRecommendationDto perfume) {
        if (favorites.containsKey(perfume.id())) {
            favorites.remove(perfume.id());
        } else {
            favorites.put(perfume.id(), perfume);
        }
        save();
    }

    public List<PerfumeRecommendationDto> all() {
        return List.copyOf(favorites.values());
    }
}
