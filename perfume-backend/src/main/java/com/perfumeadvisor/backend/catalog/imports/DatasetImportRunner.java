package com.perfumeadvisor.backend.catalog.imports;

import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "perfume.import", name = "enabled", havingValue = "true")
@Slf4j
public class DatasetImportRunner implements ApplicationRunner {

    private final FragranticaCsvImportService importService;
    private final String csvPath;

    public DatasetImportRunner(
            FragranticaCsvImportService importService,
            @Value("${perfume.import.csv-path}") String csvPath) {
        this.importService = importService;
        this.csvPath = csvPath;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path path = Path.of(csvPath);
        log.info("Импорт датасета из {}", path.toAbsolutePath());
        ImportResult result = importService.importFromCsv(path);
        log.info("Импорт завершён: добавлено {}, пропущено {}", result.imported(), result.skipped());
    }
}
