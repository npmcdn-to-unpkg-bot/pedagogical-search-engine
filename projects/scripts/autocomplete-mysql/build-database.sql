# Notes:
# () This is not a script
# () This is a "journal" to log what was done
# () .. it was written "step-by-step"
#
# What is described here?
# () How to produce the (label -> uri) table
# () It is used notably by the "user-search autocompletion" system

# --------------------------------------------------------------------
If launched, this script will crash because of this line :p

# todo: Read this article to speedup imports
# http://dev.mysql.com/doc/refman/5.5/en/optimizing-innodb-bulk-data-loading.html

# Create a table `existing-uris` with
# a Uri (Varchar(255)) table utf-8-general_ci

# 63 Millions [2016.04.07]
SELECT COUNT(*) FROM `wlm-all`;

# On server-disconnect, you can check status with
# SHOW ENGINE INNODB STATUS;
# You should see the thread active in the "TRANSACTIONS" section
INSERT INTO `existing-uris` (Uri)
	SELECT A
    FROM `wlm-all`
;

# 1.561 Million [2016.04.07]
SELECT COUNT(*) FROM `existing-uris`;

# Just to have a look
SELECT * FROM `existing-uris` LIMIT 10;

# Same for B..
INSERT IGNORE INTO `existing-uris` (Uri)
	SELECT B
    FROM `wlm-all`
;

# 111 rows added.. [2016.04.07]
# these where links with no out-links.. aka dangling nodes
SELECT COUNT(*) FROM `existing-uris`;

# Just to have a look
SELECT * FROM `existing-uris` LIMIT 10;

# Create table `labels`
CREATE TABLE `labels` (
  `Uri` VARCHAR(255) NOT NULL,
  `Label` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`Uri`, `Label`));
  
# Note: Labels are a bit tricky to import because they contain
# "escaped unicodes" (e.g. \u00A3) that need to be converted into
# utf-8 and then imported to mysql with a special utf-8 option.
# It is not dbpedia's fault, it is apparently the recommended
# way to encode triplets in the "semantic web".
# Note that the "uris" are encoded differently, but it is OK
# to let them unchanged since it is only used as an "id" internally
# or may be used to generate wikipedia URLs (it works as it).
# i.e. We will not display "uris" to the user.
#
# Here is an interesting snippet of the labels from dbpedia
# "%22Weird_Al%22_Yankovic_(album)","\"Weird Al\" Yankovic (album)"
# "%60Abdu'l-Bah%C3%A1","`Abdu'l-Bah\u00E1"
# "%C7%83X%C3%B3%C3%B5_language","\u01C3X\u00F3\u00F5 language"
# "!X%C5%A9_mythology","!X\u0169 mythology"
# "$on%C2%A5","$on\u00A5"
# "$teve_Ba%C2%A3%C2%A3mer","$teve Ba\u00A3\u00A3mer"
# "%229%E2%80%939%E2%80%939%22_plan","\"9\u20139\u20139\" plan"
# "%22Christian_T%C4%83ma%C8%99%22","\"Christian T\u0103ma\u0219\""
#
# (1:manually) Download labels_en.nt from the dbpedia project
# (2:bash) cp labels_en.nt labels_en.csv
# (3:bash) sed -ri 's/^<http:\/\/dbpedia\.org\/resource\/(.+)?> <http:\/\/www\.w3\.org\/2000\/01\/rdf-schema#label> "(.+)?"@en \.$/\"\1\",\"\2\"/' labels_en.csv
# (4.1:bash) if necessary(discard first line): tail -n +2 labels_en.csv > labels.csv
# (4.2:bash) if necessary(discard last line): sed -i '$ d' labels.csv
# (5:bash) native2ascii -encoding UTF-8 -reverse labels.csv labels.utf8.csv
# (6:bash) mkfifo labels.utf8.fifo
# (7:bash-blocking) pv labels.utf8.csv > labels.utf8.fifo
# (8:bash) FILE='labels.utf8.fifo'; WORKDIR=`pwd`; mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME --execute="START TRANSACTION; LOAD DATA INFILE '$WORKDIR/$FILE' IGNORE INTO TABLE labels CHARACTER SET UTF8 FIELDS TERMINATED BY ',' ENCLOSED BY '\"'; COMMIT; SHOW WARNINGS" > labels.utf8.warnings.log &

# Create table `transitive-redirects`
CREATE TABLE `transitive-redirects` (
  `A` VARCHAR(255) NOT NULL,
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`));

# (1:workbench) Create an index on column `B` manually with mysql-workbench

# (1:manually) Download transitive-redirects_en.nt from the dbpedia project
# (2:bash) cp transitive-redirects_en.nt transitive-redirects_en.csv
# (3:bash) sed -ri 's/^<http:\/\/dbpedia\.org\/resource\/(.+)?> <http:\/\/dbpedia\.org\/ontology\/wikiPageRedirects> <http:\/\/dbpedia\.org\/resource\/(.+)?> \.$/\"\1\",\"\2\"/' transitive-redirects_en.csv
# (4.1:bash) if necessary(discard first line): tail -n +2 transitive-redirects_en.csv > transitive-redirects.csv
# (4.2:bash) if necessary(discard last line): sed -i '$ d' transitive-redirects.csv
# (5:bash) mkfifo transitive-redirects.fifo
# (6:bash-blocking) pv transitive-redirects.csv > transitive-redirects.fifo
# (7:bash) FILE='transitive-redirects.fifo'; WORKDIR=`pwd`; mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME --execute="START TRANSACTION; LOAD DATA INFILE '$WORKDIR/$FILE' IGNORE INTO TABLE transitive-redirects FIELDS TERMINATED BY ',' ENCLOSED BY '\"'; COMMIT; SHOW WARNINGS" > transitive-redirects.warnings.log &

# Create table `transitive-redirects2`
CREATE TABLE `transitive-redirects2` (
  `A` VARCHAR(255) NOT NULL,
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`));
  
# On server-disconnect, you can check status with
# SHOW ENGINE INNODB STATUS;
# You should see the thread active in the "TRANSACTIONS" section
# [2016.04.08] 2'551'129 rows "evicted"
# i.e. that do not have their destination in our existing uris
# details: (6'528'806 - 3'977'677)
INSERT INTO `transitive-redirects2` (A, B) 
	SELECT tr.a, tr.b
	FROM `transitive-redirects` tr
		JOIN `existing-uris` eu
			ON eu.Uri = tr.b
;

# Create table `links-degree`
CREATE TABLE `links-degree` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `In` INT NOT NULL,
  `Out` INT NOT NULL,
  PRIMARY KEY (`Uri`));

# (1:manually) Import the links-degree file generated offline..

# Create table `links-degree2`
# Things reduce from 10'713'237 to 1'561'134! [2016.04.10] 
CREATE TABLE `links-degree2` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `In` INT NOT NULL,
  `Out` INT NOT NULL,
  PRIMARY KEY (`Uri`));


INSERT IGNORE INTO `links-degree2` (`Uri`, `In`, `Out`)
	SELECT ld.`Uri`, ld.`In`, ld.`Out`
	FROM `links-degree` ld
		JOIN `existing-uris` eu
			ON ld.Uri = eu.Uri
;

# Create table `dictionary-titles`
CREATE TABLE `dictionary-titles` (
  `Label` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX label_idx (`Label`(10)),
  
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `In` INT NOT NULL,
  PRIMARY KEY (`Uri`))
  CHARACTER SET 'utf8';

Drop
	index `PRIMARY`
	ON `dictionary-titles`;

# [2016.04.11] About 1 minute
INSERT IGNORE INTO `dictionary-titles` (`Uri`, `Label`, `In`) 
	SELECT eu.`Uri`, l.`Label`, ld2.`In`
	FROM `existing-uris` eu
		JOIN `labels` l
			ON eu.Uri = l.Uri
		JOIN `links-degree2` ld2
			ON eu.Uri = ld2.Uri
;

# Create table `dictionary-titles-uri-idx`
CREATE TABLE `dictionary-titles-uri-idx` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8',
  `Label` VARCHAR(255) CHARACTER SET 'utf8',
  `In` INT,
  PRIMARY KEY (`Uri`))
  CHARACTER SET 'utf8';

# [2016.04.11] About 4 minutes
START TRANSACTION;
INSERT IGNORE INTO `dictionary-titles-uri-idx` (`Uri`, `Label`, `In`) 
	SELECT dt.`Uri`, dt.`Label`, dt.`In`
	FROM `dictionary-titles` dt
;
COMMIT;


# Note: The following queries can be quiet long
# You can check the status using `sudo top` in a console
# Or issue these mysql commands in another mysql-process
# The "ROW OPERATIONS" section is especially interesting
# with "x inserts/s" and "Number of rows inserted x"
SHOW ENGINE INNODB STATUS;
SHOW FULL PROCESSLIST;

# Create table `dictionary-redirects-seed`
CREATE TABLE `dictionary-redirects-seed` (
  `UriB` VARCHAR(255) CHARACTER SET 'utf8',
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8',
  PRIMARY KEY (`UriB`, `LabelA`))
  CHARACTER SET 'utf8';

# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
#   Performance: [2016.04.11]
#   Query OK, 3'879'979 rows affected (32 min 30.96 sec)
#   Records: 3'977'307  Duplicates: 97'328  Warnings: 0
START TRANSACTION;
INSERT IGNORE INTO `dictionary-redirects-seed` (`UriB`, `LabelA`) 
	SELECT tr2.B, l.Label
	FROM `transitive-redirects2` tr2
		JOIN `labels` l
			ON tr2.A = l.Uri
;
COMMIT;

# Create table `dictionary-redirects`
CREATE TABLE `dictionary-redirects` (
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX label_idx (`LabelA`(10)),
  
  `LabelB` VARCHAR(255) CHARACTER SET 'utf8',
  `UriB` VARCHAR(255) CHARACTER SET 'utf8',
  `InB` INT,
  PRIMARY KEY (`LabelA`, `UriB`))
  CHARACTER SET 'utf8';

Drop
	index `PRIMARY`
	ON `dictionary-redirects`;

# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
#   Performance: [2016.04.11]
#   Query OK, 3'879'979 rows affected (17 min 11.27 sec)
#   Records: 3'879'979  Duplicates: 0  Warnings: 0

START TRANSACTION;
INSERT IGNORE INTO `dictionary-redirects` (`LabelA`, `LabelB`, `UriB`, `InB`) 
	SELECT drs.LabelA, dtui.Label, drs.UriB, dtui.`In`
	FROM `dictionary-redirects-seed` drs
		JOIN `dictionary-titles-uri-idx` dtui
			ON drs.UriB = dtui.Uri
;
COMMIT;

# Create table `disambiguation`
CREATE TABLE `disambiguations` (
  `A` VARCHAR(255) NOT NULL,
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`, `B`))
;

# (1:manually) Download disambiguations_en.nt from the dbpedia project
# (2:bash) cp disambiguations_en.nt disambiguations_en.csv
# (3:bash) sed -ri 's/^<http:\/\/dbpedia\.org\/resource\/(.+)?> <http:\/\/dbpedia\.org\/ontology\/wikiPageDisambiguates> <http:\/\/dbpedia\.org\/resource\/(.+)?> \.$/\"\1\",\"\2\"/' disambiguations_en.csv
# (4.1:bash) if necessary(discard first line): tail -n +2 disambiguations_en.csv > disambiguations.csv
# (4.2:bash) if necessary(discard last line): sed -i '$ d' disambiguations.csv
# (5:bash) mkfifo disambiguations.fifo
# (6:bash-blocking) pv disambiguations.csv > disambiguations.fifo
# (7:bash) FILE='disambiguations.fifo'; WORKDIR=`pwd`; mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME --execute="START TRANSACTION; LOAD DATA INFILE '$WORKDIR/$FILE' IGNORE INTO TABLE disambiguations FIELDS TERMINATED BY ',' ENCLOSED BY '\"'; COMMIT; SHOW WARNINGS" > disambiguations.warnings.log &



    
    
    
    
