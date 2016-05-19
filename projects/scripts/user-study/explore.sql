
If launched, this script will crash because of this line :p
;

SELECT * FROM `messages`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

SELECT * FROM `searches`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

SELECT * FROM `clicks`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

SELECT * FROM `classifications`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

