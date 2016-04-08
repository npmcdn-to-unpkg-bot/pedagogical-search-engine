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

# Create a table `existing-uris` with
# a Uri (Varchar(255)) table utf-8-general_ci

# 63 Millions [2016.04.07]
SELECT COUNT(*) FROM `wlm-all`;

# Execute several times on server-disconnection!
# It resumes well :) ~ 3sec / 1M ignored rows
# 
INSERT IGNORE INTO `existing-uris` (Uri)
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
# (2:bash) cp labels_en.nt labels_en.tsv
# (3:bash) sed -ri 's/^<http:\/\/dbpedia\.org\/resource\/(.+)?> <http:\/\/www\.w3\.org\/2000\/01\/rdf-schema#label> "(.+)?"@en \.$/\1\t\2/' labels_en.tsv
# (4:bash) tail -n +2 labels_en.tsv > labels.tsv
# (5:bash) mkfifo labels.fifo
# (6:bash-blocking) pv labels.tsv > labels.fifo
# todo: log the warnings, skip msgs of the following cmd
# (7:bash) mysqlimport -u YOUR_LOGIN -pYOUR_PASSWORD --local YOUR_DB_NAME labels.tsv

# Create table `transitive-redirects`
CREATE TABLE `transitive-redirects` (
  `A` VARCHAR(255) NOT NULL,
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`));

# (1:workbench) Create an index on column `B` manually with mysql-workbench

# (1:manually) Download transitive-redirects_en.nt from the dbpedia project
# (2:bash) cp transitive-redirects_en.nt transitive-redirects.tsv
# (3:bash) sed -ri 's/^<http:\/\/dbpedia\.org\/resource\/(.+)?> <http:\/\/dbpedia\.org\/ontology\/wikiPageRedirects> <http:\/\/dbpedia\.org\/resource\/(.+)?> \.$/\1\t\2/' transitive-redirects.tsv
# (4:bash) mkfifo transitive-redirects.fifo
# (5:bash-blocking) pv transitive-redirects.tsv > transitive-redirects.fifo
# todo: log the warnings, skip msgs of the following cmd
# (6:bash) mysqlimport -u YOUR_LOGIN -pYOUR_PASSWORD --local YOUR_DB_NAME transitive-redirects.fifo

# Create table `transitive-redirects2`
CREATE TABLE `transitive-redirects2` (
  `A` VARCHAR(255) NOT NULL,
  `B` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`A`));
  
