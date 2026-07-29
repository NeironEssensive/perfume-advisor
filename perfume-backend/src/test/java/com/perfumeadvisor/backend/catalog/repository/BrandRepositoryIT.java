package com.perfumeadvisor.backend.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.perfumeadvisor.backend.catalog.domain.Brand;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class BrandRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private BrandRepository brandRepository;

    @Test
    void savesAndFindsBrandByNameIgnoringCase() {
        brandRepository.save(Brand.builder()
                .name("Maison Francis Kurkdjian")
                .country("France")
                .build());

        assertThat(brandRepository.findByNameIgnoreCase("maison francis kurkdjian")).isPresent();
    }
}
