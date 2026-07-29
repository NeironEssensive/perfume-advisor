package com.perfumeadvisor.backend.scoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "perfume.scoring", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ScoringRunner implements ApplicationRunner {

    private final ScoringService scoringService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Пересчёт season/occasion score для всех парфюмов");
        int scored = scoringService.scoreAll();
        log.info("Пересчёт завершён: обработано {} парфюмов", scored);
    }
}
