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
;

# todo: Review the index creation!!!
# .. innodb uses the primary key as a "clustered index"
# .. be carefull that inserts with non-consecutive PK is slow
# .. because the records have to be moved on inserts. Does a transaction
# .. solves this? Is it better to create first with an AI PK and then copy
# .. with the real PK?
# .. see https://dev.mysql.com/doc/refman/5.7/en/innodb-table-and-index.html
# todo: Int -> MEDIUMINT or even smallest if possible
# todo: shrink VARCHAR length
# todo: rename the tables in a smart way

# General information
# Check at any time the status of a running operation (e.g. a big insert)
# by running "SHOW ENGINE INNODB STATUS;"
# The "ROW OPERATIONS" section is especially interesting
# with "x inserts/s" and "Number of rows inserted x"
# There is also the "SHOW FULL PROCESSLIST;" command

# Tune MySQL in order to make it faster.
# (1:manually) sudo /etc/init.d/mysql stop
# (2:manually) sudo vim /etc/mysql/mysql.conf.d/mysqld.cnf (or /etc/mysql/my.cnf), there should by a [mysqlld] entry
# (3:manually) add the following lines
#   # From the following guide
#   # https://www.percona.com/blog/2007/11/01/innodb-performance-optimization-basics/
#   innodb_buffer_pool_size = 14G
#   innodb_thread_concurrency = 8
# (4:manually) sudo /etc/init.d/mysql start

# On recent servers [2016.04], the mysql installation package is built
# using "secure_file_priv" flag which forces the load of rows from a
# data-file ("infile") on disk to be located in a particular root-folder.
# For the following commands to work, perform them with a root connection
# in the folder given by the query
# SHOW VARIABLES LIKE "secure_file_priv";

# On recent servers [2016.04], the direct connection to a mysql console
# using "mysql -u User -pPassword" is considered as insecured and yields
# errors. To make the following queries work, create a "login-path" using
# mysql_config_editor set --login-path=local --host=localhost --user=USER --password
# [Type in your "USER" password]
# You can then connect to the mysql console using
# mysql --login-path=local DB_NAME

# Create table
CREATE TABLE `wlm-all` (
  `A` VARCHAR(255) CHARACTER SET 'utf8',
  INDEX a_idx (`A`(10)),
  
  `B` VARCHAR(255) CHARACTER SET 'utf8',
  `Complete` DOUBLE,
  `InLinks` DOUBLE,
  `OutLinks` DOUBLE,
  PRIMARY KEY (`A`, `B`));

Drop
	index `PRIMARY`
	ON `wlm-all`;
    
# (1:manually) Download wlm-all.tsv
# (2:bash) mkfifo wlm-all.fifo
# (3:bash-blocking) pv wlm-all.tsv > wlm-all.fifo
# (4:bash) FILE='wlm-all.fifo'; WORKDIR=`pwd`; mysql --login-path=local DB_NAME --execute="START TRANSACTION; LOAD DATA INFILE '$WORKDIR/$FILE' IGNORE INTO TABLE \`wlm-all\` FIELDS TERMINATED BY '\\t' ENCLOSED BY ''; COMMIT; SHOW WARNINGS" > wlm-all.warnings.log &
# [2016.04] Time: 2h50min, 128'789'870 rows
SELECT COUNT(*) FROM `wlm-all`;

# Create table
CREATE TABLE `existing-uris` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`Uri`));

START TRANSACTION;
INSERT INTO `existing-uris` (Uri) SELECT A FROM `wlm-all`;
COMMIT;

# [2016.04] 1.561 Million
SELECT COUNT(*) FROM `existing-uris`;

# Just to have a look
SELECT * FROM `existing-uris` LIMIT 10;

# Same for B..
START TRANSACTION;
INSERT IGNORE INTO `existing-uris` (Uri) SELECT B FROM `wlm-all`;
COMMIT;

# 111 rows added.. [2016.04.07]
# these where links with no out-links.. aka dangling nodes
SELECT COUNT(*) FROM `existing-uris`;

# Just to have a look
SELECT * FROM `existing-uris` LIMIT 10;

# Create table
CREATE TABLE `labels` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX uri_idx (`Uri`(10)),
  
  `Label` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`Uri`, `Label`));
  
Drop
	index `PRIMARY`
	ON `labels`;
  
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
# (8:bash) FILE='labels.utf8.fifo'; WORKDIR=`pwd`; mysql --login-path=local DB_NAME --execute="START TRANSACTION; LOAD DATA INFILE '$WORKDIR/$FILE' IGNORE INTO TABLE labels CHARACTER SET UTF8 FIELDS TERMINATED BY ',' ENCLOSED BY '\"'; COMMIT; SHOW WARNINGS" > labels.utf8.warnings.log &
# [2016.04] It takes ~[33,53]min

# Create table
CREATE TABLE `transitive-redirects` (
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX b_idx (`B`(10)),
  
  `A` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`));

Drop
	index `PRIMARY`
	ON `transitive-redirects`;

# (1:manually) Download transitive-redirects_en.nt from the dbpedia project
# (2:bash) cp transitive-redirects_en.nt transitive-redirects_en.csv
# (3:bash) sed -ri 's/^<http:\/\/dbpedia\.org\/resource\/(.+)?> <http:\/\/dbpedia\.org\/ontology\/wikiPageRedirects> <http:\/\/dbpedia\.org\/resource\/(.+)?> \.$/\"\1\",\"\2\"/' transitive-redirects_en.csv
# (4.1:bash) if necessary(discard first line): tail -n +2 transitive-redirects_en.csv > transitive-redirects.csv
# (4.2:bash) if necessary(discard last line): sed -i '$ d' transitive-redirects.csv
# (5:bash) mkfifo transitive-redirects.fifo
# (6:bash-blocking) pv transitive-redirects.csv > transitive-redirects.fifo
# (7:bash) FILE='transitive-redirects.fifo'; WORKDIR=`pwd`; mysql --login-path=local DB_NAME --execute="START TRANSACTION; LOAD DATA INFILE '$WORKDIR/$FILE' IGNORE INTO TABLE \`transitive-redirects\` FIELDS TERMINATED BY ',' ENCLOSED BY '\"'; COMMIT; SHOW WARNINGS" > transitive-redirects.warnings.log &
# [2016.04] It takes ~23min

# Create table
CREATE TABLE `transitive-redirects2` (
  `A` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`));
  
# [2016.04] Time: ~??, 2'551'129 rows "evicted"
# i.e. that do not have their destination in our existing uris
# details: (6'528'806 - 3'977'677)
START TRANSACTION;
INSERT INTO `transitive-redirects2` (A, B) 
	SELECT tr.a, tr.b
	FROM `transitive-redirects` tr
		JOIN `existing-uris` eu
			ON eu.Uri = tr.b
;
COMMIT;

# Create table
CREATE TABLE `links-degree` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `In` INT NOT NULL,
  `Out` INT NOT NULL,
  PRIMARY KEY (`Uri`));

# (1:manually) Import the links-degree file generated offline..

# Create table
# Things reduce from 10'713'237 to 1'561'134! [2016.04.10] 
CREATE TABLE `links-degree2` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `In` INT NOT NULL,
  `Out` INT NOT NULL,
  PRIMARY KEY (`Uri`));

START TRANSACTION;
INSERT IGNORE INTO `links-degree2` (`Uri`, `In`, `Out`)
	SELECT ld.`Uri`, ld.`In`, ld.`Out`
	FROM `links-degree` ld
		JOIN `existing-uris` eu
			ON ld.Uri = eu.Uri
;
COMMIT;

# Create table
CREATE TABLE `dictionary-titles-init` (
  `Label` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX label_idx (`Label`(10)),
  
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `In` INT NOT NULL,
  PRIMARY KEY (`Uri`))
  CHARACTER SET 'utf8';

Drop
	index `PRIMARY`
	ON `dictionary-titles-init`;

# [2016.04] About 1 minute
INSERT IGNORE INTO `dictionary-titles-init` (`Uri`, `Label`, `In`) 
	SELECT eu.`Uri`, l.`Label`, ld2.`In`
	FROM `existing-uris` eu
		JOIN `labels` l
			ON eu.Uri = l.Uri
		JOIN `links-degree2` ld2
			ON eu.Uri = ld2.Uri
;

# Create table
CREATE TABLE `dictionary-titles` (
  `Label` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX label_idx (`Label`(10)),
  
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `In` INT NOT NULL,
  `available` TINYINT,
  PRIMARY KEY (`Uri`))
  CHARACTER SET 'utf8';

Drop
	index `PRIMARY`
	ON `dictionary-titles`;

# Performance: unknown
INSERT IGNORE INTO `dictionary-titles`
	(`Uri`, `Label`, `In`, `available`) 
	SELECT eu.`Uri`, l.`Label`, ld2.`In`, 0
	FROM `existing-uris` eu
		JOIN `labels` l
			ON eu.Uri = l.Uri
		JOIN `links-degree2` ld2
			ON eu.Uri = ld2.Uri
;

# Create table
CREATE TABLE `dictionary-titles-uri-idx` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8',
  `Label` VARCHAR(255) CHARACTER SET 'utf8',
  `In` INT,
  PRIMARY KEY (`Uri`))
  CHARACTER SET 'utf8';

# [2016.04.11] About 4 minutes
START TRANSACTION;
INSERT IGNORE INTO `dictionary-titles-uri-idx` (`Uri`, `Label`, `In`) 
	SELECT `Uri`, `Label`, `In`
	FROM `dictionary-titles-init`
;
COMMIT;


# Create table
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

# Create table
CREATE TABLE `dictionary-redirects-init` (
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX label_idx (`LabelA`(10)),
  
  `LabelB` VARCHAR(255) CHARACTER SET 'utf8',
  `UriB` VARCHAR(255) CHARACTER SET 'utf8',
  `InB` INT,
  PRIMARY KEY (`LabelA`, `UriB`))
  CHARACTER SET 'utf8';

Drop
	index `PRIMARY`
	ON `dictionary-redirects-init`;

# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
#   Performance: [2016.04.11]
#   Query OK, 3'879'979 rows affected (17 min 11.27 sec)
#   Records: 3'879'979  Duplicates: 0  Warnings: 0

START TRANSACTION;
INSERT IGNORE INTO `dictionary-redirects-init`
	(`LabelA`, `LabelB`, `UriB`, `InB`) 
	SELECT drs.LabelA, dtui.Label, drs.UriB, dtui.`In`
	FROM `dictionary-redirects-seed` drs
		JOIN `dictionary-titles-uri-idx` dtui
			ON drs.UriB = dtui.Uri
;
COMMIT;


# Create table
CREATE TABLE `dictionary-redirects` (
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX label_idx (`LabelA`(10)),
  
  `LabelB` VARCHAR(255) CHARACTER SET 'utf8',
  `UriB` VARCHAR(255) CHARACTER SET 'utf8',
  `InB` INT,
  `available` TINYINT,
  PRIMARY KEY (`LabelA`, `UriB`))
  CHARACTER SET 'utf8';

Drop
	index `PRIMARY`
	ON `dictionary-redirects`;
    
# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
#   Performance: unknown

START TRANSACTION;
INSERT IGNORE INTO `dictionary-redirects`
	(`LabelA`, `LabelB`, `UriB`, `InB`, `available`) 
	SELECT `LabelA`, `LabelB`, `UriB`, `InB`, `available`, 0
	FROM `dictionary-redirects-init`
;
COMMIT;

# Create table
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
# [2016.04] Time: 3min

# Create table
CREATE TABLE `dictionary-disambiguation-seed1` (
  `A` VARCHAR(255) CHARACTER SET 'utf8',
  `B` VARCHAR(255) CHARACTER SET 'utf8',
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8',
  PRIMARY KEY (`A`, `B`))
;

# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
# [2016.04] Time: 12min, 1'419'848 rows
START TRANSACTION;
INSERT IGNORE INTO `dictionary-disambiguation-seed1` (`A`, `B`, `LabelA`) 
	SELECT d.A, d.B, l.Label
	FROM `disambiguations` d
		JOIN `labels` l
			ON d.A = l.Uri
;
COMMIT;

# Create table
CREATE TABLE `dictionary-disambiguation-seed2` (
  `B` VARCHAR(255) CHARACTER SET 'utf8',
  `A` VARCHAR(255) CHARACTER SET 'utf8',
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8',
  PRIMARY KEY (`B`, `A`))
;

# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
# [2016.04.11] Time: 4min
START TRANSACTION;
INSERT IGNORE INTO `dictionary-disambiguation-seed2` (`B`, `A`, `LabelA`) 
	SELECT ds1.B, ds1.A, ds1.LabelA
	FROM `dictionary-disambiguation-seed1` ds1
;
COMMIT;


# Create table
CREATE TABLE `dictionary-disambiguation-seed3` (
  `B` VARCHAR(255) CHARACTER SET 'utf8',
  `A` VARCHAR(255) CHARACTER SET 'utf8',
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8',
  `LabelB` VARCHAR(255) CHARACTER SET 'utf8',
  PRIMARY KEY (`B`, `A`))
;

# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
# [2016.04.11] Performance
#   Query OK, 1315398 rows affected (1 min 16.63 sec)
#   Records: 1315398  Duplicates: 0  Warnings: 0
# Note: Some rows have been lost because some pages b
# of disamb. a->b do not appear in the `labels`
START TRANSACTION;
INSERT IGNORE INTO `dictionary-disambiguation-seed3` (`B`, `A`, `LabelA`, `LabelB`) 
	SELECT ds2.B, ds2.A, ds2.LabelA, l.Label
	FROM `dictionary-disambiguation-seed2` ds2
    FORCE INDEX (`PRIMARY`) 
		JOIN `labels` l
			ON ds2.B = l.Uri
;
COMMIT;


# Create table
CREATE TABLE `dictionary-disambiguation-init` (
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8',
  INDEX label_idx (`LabelA`(10)),
  
  `A` VARCHAR(255) CHARACTER SET 'utf8',
  `B` VARCHAR(255) CHARACTER SET 'utf8',
  `LabelB` VARCHAR(255) CHARACTER SET 'utf8',
  `InB` INT,
  PRIMARY KEY (`A`, `B`))
  CHARACTER SET 'utf8';

Drop
	index `PRIMARY`
	ON `dictionary-disambiguation-init`;

# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
# [2016.04.11] Performance
#   Query OK, 709951 rows affected (58.79 sec)
#   Records: 709951  Duplicates: 0  Warnings: 0
# Note: The number of records has once again dropped because
# links-degree2 contains only uris from `existing-uris` and
# are making a JOIN with it.
START TRANSACTION;
INSERT IGNORE INTO `dictionary-disambiguation-init`
	(`LabelA`, `A`, `B`, `LabelB`, `InB`) 
	SELECT ds3.LabelA, ds3.A, ds3.B, ds3.LabelB, ld2.`In`
	FROM `dictionary-disambiguation-seed3` ds3
		JOIN `links-degree2` ld2
			ON ds3.B = ld2.Uri
;
COMMIT;
    
    
# Create table
CREATE TABLE `dictionary-disambiguation` (
  `LabelA` VARCHAR(255) CHARACTER SET 'utf8',
  INDEX label_idx (`LabelA`(10)),
  
  `A` VARCHAR(255) CHARACTER SET 'utf8',
  `B` VARCHAR(255) CHARACTER SET 'utf8',
  `LabelB` VARCHAR(255) CHARACTER SET 'utf8',
  `InB` INT,
  `available` TINYINT,
  PRIMARY KEY (`A`, `B`))
  CHARACTER SET 'utf8';

Drop
	index `PRIMARY`
	ON `dictionary-disambiguation`;
    
# Perform the following commands in a mysql console:
# mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME
# Performance: Unknown
START TRANSACTION;
INSERT IGNORE INTO `dictionary-disambiguation`
	(`LabelA`, `A`, `B`, `LabelB`, `InB`, `available`) 
	SELECT `LabelA`, `A`, `B`, `LabelB`, `InB`, `available`, 0
	FROM `dictionary-disambiguation-init`
;
COMMIT;
