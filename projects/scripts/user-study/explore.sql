
If launched, this script will crash because of this line :p
;


SELECT COUNT(distinct sid) FROM `searches`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s');




SELECT * FROM `messages`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC
LIMIT 100;

SELECT * FROM `searches`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC
LIMIT 100;

SELECT *  FROM `clicks`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC
LIMIT 100;

SELECT *  FROM `classifications`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC
LIMIT 100;




SELECT c.SearchHash, c.Sid, MAX(s.SearchLog)
FROM `classifications` c
	JOIN `searches` s
		ON c.SearchHash = s.SearchHash AND c.Sid = s.Sid
WHERE c.Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
GROUP BY c.SearchHash, c.Sid;




SELECT COUNT(*) FROM `messages`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

SELECT COUNT(*) FROM `searches`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

SELECT COUNT(*)  FROM `clicks`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

SELECT COUNT(*)  FROM `classifications`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;




SELECT COUNT(*)  
FROM `clicks` c
	JOIN `cache-entries` ce
		ON c.EntryId = ce.EntryId
WHERE
	Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

SELECT COUNT(*) 
FROM `classifications` c
	JOIN `cache-entries` ce
		ON c.EntryId = ce.EntryId
WHERE
	Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
ORDER BY Timestamp DESC;

SELECT COUNT(*), Category, Content
FROM `messages`
WHERE Timestamp > STR_TO_DATE('18/05/2016 20:00:00', '%d/%m/%Y %H:%i:%s')
GROUP BY Category, Content;
