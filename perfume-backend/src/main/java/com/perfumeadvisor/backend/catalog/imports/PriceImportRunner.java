package com.perfumeadvisor.backend.catalog.imports;

import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "perfume.price-import", name = "enabled", havingValue = "true")
@Slf4j
public class PriceImportRunner implements ApplicationRunner {

    private final PriceImportService priceImportService;
    private final String[] csvPaths;

    public PriceImportRunner(
            PriceImportService priceImportService,
            @Value("${perfume.price-import.paths}") String csvPaths) {
        this.priceImportService = priceImportService;
        this.csvPaths = csvPaths.split(",");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Path> paths = List.of(csvPaths).stream().map(String::strip).map(Path::of).toList();
        log.info("Импорт цен из {}", paths);
        PriceImportResult result = priceImportService.importFromCsv(paths);
        log.info("Импорт цен завершён: обработано строк {}, проставлена цена для {} парфюмов",
                result.totalRows(), result.pricedPerfumes());
    }
}
