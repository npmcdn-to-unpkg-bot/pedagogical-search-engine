
If launched, this script will crash because of this line :p
;

CREATE TABLE `cache-entries` (
	# There will be a lot of inserts. Thus the "clustered-index"
    # (aka the PK) in InnoDb should be consecutive.
	`AutoId` INT NOT NULL AUTO_INCREMENT,
    
    # But we will search by "search hash". Hence there should
    # be an index on this column.
	`SearchHash` INT NOT NULL,
	INDEX searchhash_idx (`SearchHash`),
    
    `EntryId` VARCHAR(36) CHARACTER SET 'utf8',
    `Rank` SMALLINT,
    `Source` VARCHAR(16) CHARACTER SET 'utf8',

	PRIMARY KEY(`AutoId`))
	CHARACTER SET 'utf8';
    
CREATE TABLE `cache-details` (
	# There will be a lot of inserts. Thus the "clustered-index"
    # (aka the PK) in InnoDb should be consecutive.
	`AutoId` INT NOT NULL AUTO_INCREMENT,
    
    # But we will search by "entry id". Hence there should
    # be an index on this column.
	`EntryId` VARCHAR(36) CHARACTER SET 'utf8' NOT NULL,
	INDEX entryid_idx (`EntryId`),
    
    `Title` VARCHAR(255) CHARACTER SET 'utf8',
    `TypeText` VARCHAR(255) CHARACTER SET 'utf8',
    `Url` VARCHAR(255) CHARACTER SET 'utf8',
    `Snippet` TEXT CHARACTER SET 'utf8',
    `Timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

	PRIMARY KEY(`AutoId`))
    CHARACTER SET 'utf8';
