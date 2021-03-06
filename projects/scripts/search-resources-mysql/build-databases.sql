# Notes:
# () This is not a script
# () This is a "journal" to log what was done
# () .. it was written "step-by-step"
#
# What is described here?
# () How to produce the tables used to search the resources

# --------------------------------------------------------------------
If launched, this script will crash because of this line :p
;


# Create a table
CREATE TABLE `indices` (
  `Uri` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  INDEX uri_idx (`Uri`(20)),
  
  `EntryId` CHAR(36) CHARACTER SET 'utf8' NOT NULL,
  `Score` DOUBLE NULL,
  `ResourceId` CHAR(36) CHARACTER SET 'utf8' NOT NULL,
  `TypeText` VARCHAR(255) CHARACTER SET 'utf8' NULL,
PRIMARY KEY (`Uri`, `EntryId`));

Drop
	index `PRIMARY`
	ON `indices`;

# Create a table
CREATE TABLE `details` (
  `EntryId` CHAR(36) CHARACTER SET 'utf8' NOT NULL,
  `Title` VARCHAR(255) CHARACTER SET 'utf8' NULL,
  `Href` TEXT CHARACTER SET 'utf8' NULL,
  `Snippet` TEXT CHARACTER SET 'utf8' NULL,
  `TopIndicesJson` TEXT CHARACTER SET 'utf8' NULL,
PRIMARY KEY (`EntryId`));
