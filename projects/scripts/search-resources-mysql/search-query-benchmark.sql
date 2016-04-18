# [only for few indices] Have a look at the data
SELECT Uri, COUNT(*) 
FROM tocpic.indices
GROUP BY Uri;

# final query
# Benchmark on 800 indices, 40 matchings: 0.04s
SELECT
	i.`EntryId` as `EntryId`,
	SUM(i.`Score`) as `TotalScore`
FROM `indices` i
WHERE i.`Uri` IN ("Music")
GROUP BY i.`EntryId`
ORDER BY `TotalScore` DESC
LIMIT 0, 10
;


# friendly-adaptation for development only of the final query
SELECT
	i.`EntryId` as `EntryId`,
    MIN(d.`Title`) as `Title`,
    MIN(d.`Type`) as `Type`,
    MIN(d.`Href`) as `Href`,
	SUM(i.`Score`) as `TotalScore`
FROM `indices` i
	JOIN `details` d
		ON d.`EntryId` = i.`EntryId`
WHERE i.`Uri` IN ("Music", "Music_industry")
GROUP BY i.`EntryId`
ORDER BY `TotalScore` DESC
LIMIT 0, 10
;