
If launched, this script will crash because of this line :p
;

CREATE TABLE `clicks` (
	# There will be a lot of inserts. Thus the "clustered-index"
    # (aka the PK) in InnoDb should be consecutive.
	`AutoId` INT NOT NULL AUTO_INCREMENT,
    
    # But we want to group by entry-id to get the sum of clicks
    # per entry. Hence we create an index on this column.
    # The records will not be consecutive on this column,
    # but it is not really a problem if there are not too many
    # click-entries per entry-id. e.g. We could compact/aggregate/sum
    # the table when it reaches a some size.
    `EntryId` VARCHAR(36) CHARACTER SET 'utf8',
	INDEX entryid_idx (`EntryId`(10)),
    
	`Uris` TEXT CHARACTER SET 'utf8',
	`Href` TEXT CHARACTER SET 'utf8',
	`Source` TINYINT,
	`Rank` TINYINT,
	`Quality` VARCHAR(32) CHARACTER SET 'utf8',
    
	PRIMARY KEY (`AutoId`))
    CHARACTER SET 'utf8';
    
CREATE TABLE `classification` (
	# There will be a lot of inserts. Thus the "clustered-index"
    # (aka the PK) in InnoDb should be consecutive.
	`AutoId` INT NOT NULL AUTO_INCREMENT,
    
    # But we want to group by uris to know the classifications
    # associated with their results.
    # Hence we create an index on this column.
    # The records will not be consecutive on this column, but it
    # is not really a problem if there are not too many classification
    # entries per uris. e.g. We could compact/aggregate/sum
    # the table when it reaches a some size.
    `Uris` TEXT CHARACTER SET 'utf8',
	INDEX uris_idx (`Uris`(64)),
    
    `EntryId` VARCHAR(36) CHARACTER SET 'utf8',
    `Classification` VARCHAR(32) CHARACTER SET 'utf8',
    
	PRIMARY KEY (`AutoId`))
    CHARACTER SET 'utf8';
    
    