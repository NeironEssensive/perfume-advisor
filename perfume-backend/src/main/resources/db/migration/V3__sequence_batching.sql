-- Переводим генерацию id с IDENTITY на SEQUENCE (allocationSize=50 в JPA), чтобы Hibernate
-- мог батчить INSERT'ы вместо одного round-trip к БД на каждую строку. Без этого массовый
-- импорт CSV (десятки тысяч строк) занимает часы вместо минут.
ALTER SEQUENCE brand_id_seq INCREMENT BY 50;
ALTER SEQUENCE note_id_seq INCREMENT BY 50;
ALTER SEQUENCE accord_id_seq INCREMENT BY 50;
ALTER SEQUENCE occasion_tag_id_seq INCREMENT BY 50;
ALTER SEQUENCE perfume_id_seq INCREMENT BY 50;
ALTER SEQUENCE perfume_note_id_seq INCREMENT BY 50;
ALTER SEQUENCE perfume_accord_id_seq INCREMENT BY 50;
ALTER SEQUENCE perfume_season_score_id_seq INCREMENT BY 50;
