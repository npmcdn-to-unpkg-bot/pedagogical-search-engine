# Notes:
# () This is not a script
# () This is a "journal" to log what was done
# () .. it was written "step-by-step"
#
# What is described here?
# () How to produce the (label -> uri) table
# () It is used notably by the "user-search autocompletion" system

# --------------------------------------------------------------------
stop

# todo: put CHARACTER SET 'utf8' everywhere!

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
  
# (1:manually) Download labels_en.nt from the dbpedia project
# (2:bash) cp labels_en.nt labels_en.csv
# (3:bash) sed -ri 's/^<http:\/\/dbpedia\.org\/resource\/(.+)?> <http:\/\/www\.w3\.org\/2000\/01\/rdf-schema#label> "(.+)?"@en \.$/\"\1\",\"\2\"/' labels_en.csv
# (4.1:bash) if necessary: tail -n +2 labels_en.csv > labels.csv
# (4.2:bash) if necessary: sed -i '$ d' labels.csv
# (5:bash) mkfifo labels.fifo
# (6:bash-blocking) pv labels.csv > labels.fifo
# (7:bash) FILE='labels.fifo'; WORKDIR=`pwd`; mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME --execute="LOAD DATA INFILE '$WORKDIR/$FILE' IGNORE INTO TABLE labels FIELDS TERMINATED BY ',' ENCLOSED BY '\"'; SHOW WARNINGS" > labels.warnings.log &

# Create table `transitive-redirects`
CREATE TABLE `transitive-redirects` (
  `A` VARCHAR(255) NOT NULL,
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`));

# (1:workbench) Create an index on column `B` manually with mysql-workbench

# (1:manually) Download transitive-redirects_en.nt from the dbpedia project
# (2:bash) cp transitive-redirects_en.nt transitive-redirects_en.csv
# (3:bash) sed -ri 's/^<http:\/\/dbpedia\.org\/resource\/(.+)?> <http:\/\/dbpedia\.org\/ontology\/wikiPageRedirects> <http:\/\/dbpedia\.org\/resource\/(.+)?> \.$/\"\1\",\"\2\"/' transitive-redirects_en.csv
# (4.1:bash) if necessary: tail -n +2 transitive-redirects_en.csv > transitive-redirects.csv
# (4.2:bash) if necessary: sed -i '$ d' transitive-redirects.csv
# (5:bash) mkfifo transitive-redirects.fifo
# (6:bash-blocking) pv transitive-redirects.csv > transitive-redirects.fifo
# todo: log the warnings, skip msgs of the following cmd
# (7:bash) FILE='transitive-redirects.fifo'; WORKDIR=`pwd`; mysql -u YOUR_USER -pYOUR_PASSWORD YOUR_DB_NAME --execute="LOAD DATA INFILE '$WORKDIR/$FILE' IGNORE INTO TABLE labels FIELDS TERMINATED BY ',' ENCLOSED BY '\"'; SHOW WARNINGS" > transitive-redirects.warnings.log &

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

# Create table `dictionary`
CREATE TABLE `dictionary` (
  `Uri` VARCHAR(255) NOT NULL,
  `Label` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`Uri`, `Label`));
  
# Insert the labels of the redirects
# On server-disconnect, you can check status with
# SHOW ENGINE INNODB STATUS;
# You should see the thread active in the "TRANSACTIONS" section
# Note: The "Ignore" Keywork is because we might have two times 
#       the same label for one uri
INSERT IGNORE INTO `dictionary` (Uri, Label) 
	SELECT tr2.b, l.Label
	FROM `transitive-redirects2` tr2
		JOIN `labels` l
			ON tr2.a = l.Uri
;

# Insert the labels of the existing-"accessible" uri 
INSERT IGNORE INTO `dictionary` (Uri, Label) 
	SELECT eu.Uri, l.Label
	FROM `existing-uris` eu
		JOIN `labels` l
			ON eu.Uri = l.Uri
;

# Create table `disambiguation`
CREATE TABLE `disambiguation` (
  `A` VARCHAR(255) NOT NULL,
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`, `B`))
;

# todo: Check URI: !Qu%C3%A9_Clase_de_Amor! is correctly encoded
# todo: Check URI: %60Abdu'l-Bah%C3%A1 is correctly encoded
# todo: Check URI: %22Weird_Al%22_Yankovic_(album) is correctly encoded
# todo: Check URI: %22Devious%22_Diesel -> "Devious" Diesel





