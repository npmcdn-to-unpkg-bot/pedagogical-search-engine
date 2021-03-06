
# --------------------------------------------------------------------
If launched, this script will crash because of this line :p
;

TRUNCATE TABLE `active-uris-tmp`;

# Slick-based availability-update of uris from indices of 127 resources
# time:
#   34s    mark all with a single SQL query(10s) & [insert indices & details](24s)
# 	242s   mark all & insert indices & details
#   256s   mark the ones not already marked

# To reset all availibilities
SET SQL_SAFE_UPDATES=0;
# 14 sec
UPDATE `dictionary-disambiguation`
SET available = 0;
# 84 sec
UPDATE `dictionary-redirects`
SET available = 0;
# 27 sec
UPDATE `dictionary-titles`
SET available = 0;
SET SQL_SAFE_UPDATES=1;

# 1'186 are available
# todo: Search "Apollo_11" indices
SELECT COUNT(*) FROM `dictionary-disambiguation`
WHERE available = 1;

# 14'528 are available
# todo: Search "Arab_Spring"
SELECT COUNT(*) FROM `dictionary-redirects`
WHERE available = 1;

# 798 are available
# todo: Search "Wisdom"
SELECT COUNT(*) FROM `dictionary-titles`
WHERE available = 1;





# Mysql-based update
# Create tables
CREATE TEMPORARY table ActiveUris (
	SELECT DISTINCT Uri as Uri
    FROM `indices`
);

CREATE TABLE `active-uris-tmp` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
PRIMARY KEY (`Uri`));

# [2016.04] 5'090 resources, time: 3.5sec, 11,247 active uris
# [2016.04] 24,293 resources, time: 9sec, 22,793 active uris
START TRANSACTION;
INSERT IGNORE INTO `active-uris-tmp`
	(`Uri`) 
	SELECT DISTINCT Uri as Uri
    FROM `indices`
;
COMMIT;

# 124s
SET SQL_SAFE_UPDATES=0;

UPDATE `dictionary-disambiguation` dd
SET dd.available = 1
WHERE dd.B IN (
	SELECT au.Uri
    FROM ActiveUris au
);

SET SQL_SAFE_UPDATES=1;

# 124s
SET SQL_SAFE_UPDATES=0;

START TRANSACTION;

UPDATE `dictionary-disambiguation` dd
SET dd.available = 1
WHERE dd.B IN (
	SELECT au.Uri
    FROM ActiveUris au
);

COMMIT;

SET SQL_SAFE_UPDATES=1;

# [2016.04] Benchmark for 800 distinct uris in "active-uris-tmp"
# disambiguations: 1.3s
# redirects: 6.5s
# titles: 2.6s
# ------------------------
#
# [2016.04] 11,247 active uris
# disambiguations: 3s
# redirects: 29s
# titles: 6s
# ------------------------
#
# [2016.04] 22,793 active uris
# disambiguations: 4s
# redirects: 48s
# titles: 8s
# ------------------------
SET SQL_SAFE_UPDATES=0;

UPDATE `dictionary-disambiguation` dd
SET dd.available = 1
WHERE dd.B IN (
	SELECT aut.Uri
    FROM `active-uris-tmp` aut
);

UPDATE `dictionary-redirects` dr
SET dr.available = 1
WHERE dr.UriB IN (
	SELECT aut.Uri
    FROM `active-uris-tmp` aut
);

UPDATE `dictionary-titles` dt
SET dt.available = 1
WHERE dt.Uri IN (
	SELECT aut.Uri
    FROM `active-uris-tmp` aut
);

SET SQL_SAFE_UPDATES=1;




