-- Tables added after the original climate_prediction.sql export.
-- Apply after importing climate_prediction.sql.

CREATE TABLE IF NOT EXISTS `grid_data` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255),
  `type` varchar(50),
  `size` bigint,
  `url` varchar(1024),
  `is_delete` tinyint(1) DEFAULT 0,
  `enable` tinyint(1) DEFAULT 1,
  `md5` varchar(64),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `enso_data` LIKE `grid_data`;

CREATE TABLE IF NOT EXISTS `enso_iri_probability` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `season` varchar(20),
  `la_nina` int,
  `neutral` int,
  `el_nino` int,
  `published_at` datetime,
  `source_url` varchar(1024),
  `fetched_at` datetime,
  PRIMARY KEY (`id`),
  KEY `idx_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `enso_cpc_strengths` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `season` varchar(20),
  `le_neg2` int,
  `le_neg15` int,
  `le_neg1` int,
  `le_neg05` int,
  `ge_pos05` int,
  `ge_pos1` int,
  `ge_pos15` int,
  `ge_pos2` int,
  `fetched_at` datetime,
  `source_url` varchar(1024),
  PRIMARY KEY (`id`),
  KEY `idx_fetched_at` (`fetched_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
