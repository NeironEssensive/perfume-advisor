# Perfume Advisor

Десктопное приложение для подбора парфюма: каталог из ~24 тысяч ароматов, рекомендации по поводу и сезону, полнотекстовый поиск, подбор через локальную LLM (Ollama) и избранное. Backend на Spring Boot, клиент — JavaFX.

<p align="center">
  <img src="docs/screenshots/recommendations.png" width="45%" alt="Вкладка «Рекомендации»" />
  <img src="docs/screenshots/detail-card.png" width="45%" alt="Карточка аромата с нотами и аккордами" />
</p>

## Возможности

- **Рекомендации** — топ ароматов по поводу (офис, повседневный, свидание, особый случай, спорт, школа) и текущему сезону, с фильтром по полу и сортировкой.
- **Быстрый поиск** — полнотекстовый поиск по каталогу (бренд/название), включая распространённые аббревиатуры (JPG, YSL, D&G, CK, TF...) и устойчивость к транслитерации.
- **Подбор с ИИ** — свободное текстовое описание ("хочу зимний женский горьковатый аромат") превращается в детерминированный поиск по каталогу, а локальная LLM (Ollama) только объясняет выбор на русском языке.
- **Избранное** — звёздочка на карточке добавляет аромат в отдельную вкладку; хранится локально в `~/.perfume-advisor/favorites.json`, без бэкенда.
- **Карточка аромата** — фото, пирамида нот (верхние/средние/базовые) с иконками, аккорды в виде пропорциональных полос, цена (где есть данные), ссылка на Fragrantica.

## Архитектура

Multi-module Maven проект на Java 21:

| Модуль | Назначение |
|---|---|
| `perfume-common` | Общие DTO и enum'ы, используются и backend, и client |
| `perfume-backend` | Spring Boot REST API: каталог, рекомендации, поиск, AI-подбор |
| `perfume-client` | JavaFX-клиент — тонкий UI поверх REST API |

**Стек:** Java 21, Spring Boot 3.3.5, PostgreSQL + Flyway, Redis, JavaFX 21, Ollama (локальная LLM), Maven.

## Быстрый старт

### 1. Инфраструктура

```bash
docker compose up -d
```

Поднимет Postgres (порт `5433`) и Redis (порт `6379`).

### 2. Ollama (для вкладки «Подбор с ИИ»)

```bash
ollama pull qwen2.5:3b
ollama serve
```

Без Ollama всё остальное приложение (рекомендации, поиск, избранное) работает нормально — сломается только вкладка AI-подбора.

### 3. Данные каталога

Исходный датасет (Fragrantica, ~24k ароматов) не входит в репозиторий из-за размера — положите CSV в `perfume-backend/data/fragrantica_dataset.csv` и запустите backend с включённым импортом и скорингом:

```bash
mvn -pl perfume-backend spring-boot:run -Dspring-boot.run.arguments="--perfume.import.enabled=true --perfume.scoring.enabled=true"
```

Это одноразовая операция: импортёр наполняет БД, скоринг считает совместимость каждого аромата с сезонами и поводами. При последующих обычных запусках эти флаги не нужны.

Опционально — цены подтягиваются из CSV с листингами (например, eBay-датасет) отдельным прогоном:

```bash
mvn -pl perfume-backend spring-boot:run -Dspring-boot.run.arguments="--perfume.price-import.enabled=true --perfume.price-import.paths=/path/to/prices.csv"
```

Можно передать несколько файлов через запятую. Цена проставляется только там, где алгоритм нашёл совпадение по бренду и характерным словам в названии — это не 100% каталога.

### 4. Обычный запуск backend

```bash
mvn -pl perfume-backend spring-boot:run
```

Backend поднимется на `http://localhost:8080`. Swagger UI — `/swagger-ui.html`.

### 5. Клиент

```bash
mvn -pl perfume-client javafx:run
```

По умолчанию клиент ходит на `http://localhost:8080`; адрес можно переопределить через `-Dbackend.url=http://host:8080`.

## Готовый исполняемый файл (Windows)

Клиент можно собрать в самодостаточный `.exe` со встроенной Java — устанавливать JDK на целевой машине не нужно:

```bash
mvn -pl perfume-client -am package
jpackage --type app-image --input perfume-client/target --main-jar perfume-client-<version>-shaded.jar \
  --main-class com.perfumeadvisor.client.Launcher --name "Perfume Advisor" --icon perfume-client/src/main/resources/icon.ico
```

Важно: это только клиент. Он всё равно обращается к backend по сети (по умолчанию `localhost:8080`), поэтому для запуска на другой машине backend должен быть доступен — либо развёрнут там же, либо клиент запущен с `-Dbackend.url=http://<адрес-backend>:8080`.

## REST API

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/api/recommendations` | Рекомендации по `occasion` (обязателен), `gender`, `season`, `limit`, `sort` |
| `GET` | `/api/recommendations/search` | Полнотекстовый поиск по `query`, `limit` |
| `POST` | `/api/ai-recommendations` | Подбор по свободному текстовому описанию через Ollama |

## Тесты

```bash
mvn test
```

Покрыты: транслитерация и извлечение предпочтений из текста для AI-поиска, ранжирование рекомендаций, репозитории (Testcontainers), утилиты клиента (цвета аккордов, иконки нот, сортировка).

## Известные ограничения

- Цены есть не для всех ароматов — источник данных (eBay-листинги) покрывает только часть каталога и только мужские ароматы.
- AI-подбор работает только при поднятом локальном Ollama; ключи внешних LLM-провайдеров не используются.
- Избранное хранится локально в файле на диске — не синхронизируется между устройствами.
