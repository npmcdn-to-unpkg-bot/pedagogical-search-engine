# [Title] Prefix search
# 1-letter: 0.15
# 2-letter: 0.065
# 3+letters: 0.045
SET @search = "bri";
SELECT
	d.`Uri` as `Uri`,
	d.`Label` as `Label`,
    d.`In` as `In`
FROM `dictionary-titles` d
WHERE
	d.Label LIKE CONCAT(@search, "%") OR
	d.Label LIKE CONCAT("the ", @search, "%") OR
	d.Label LIKE CONCAT("a ", @search, "%") OR
	d.Label LIKE CONCAT("an ", @search, "%") OR
	d.Label LIKE CONCAT("\"", @search, "%") OR
	d.Label LIKE CONCAT("'", @search, "%")
ORDER BY length(d.Label) ASC, d.`In` DESC
LIMIT 10
;

# [Title] Full Search: 2.7
SET @search = "british";
SET @search2 = "order";
SELECT
	d.`Uri` as `Uri`,
	d.`Label` as `Label`,
    d.`In` as `In`
FROM `dictionary-titles` d
WHERE
	d.Label LIKE CONCAT("", @search, " ", @search2, "") OR
	d.Label LIKE CONCAT("% ", @search, " ", @search2, "") OR
	d.Label LIKE CONCAT("", @search, " ", @search2, " %") OR
	d.Label LIKE CONCAT("% ", @search, " ", @search2, " %") OR
    
	d.Label LIKE CONCAT("", @search, " % ", @search2, "") OR
	d.Label LIKE CONCAT("% ", @search, " % ", @search2, "") OR
	d.Label LIKE CONCAT("", @search, " % ", @search2, " %") OR
	d.Label LIKE CONCAT("% ", @search, " % ", @search2, " %") OR
    
    
	d.Label LIKE CONCAT("", @search2, " ", @search, "") OR
	d.Label LIKE CONCAT("% ", @search2, " ", @search, "") OR
	d.Label LIKE CONCAT("", @search2, " ", @search, " %") OR
	d.Label LIKE CONCAT("% ", @search2, " ", @search, " %") OR
    
	d.Label LIKE CONCAT("", @search2, " % ", @search, "") OR
	d.Label LIKE CONCAT("% ", @search2, " % ", @search, "") OR
	d.Label LIKE CONCAT("", @search2, " % ", @search, " %") OR
	d.Label LIKE CONCAT("% ", @search2, " % ", @search, " %")
ORDER BY length(d.Label) ASC, d.`In` DESC
LIMIT 10
;

# [Title] Dummy full Search: 0.95
SET @search = "british";
SET @search2 = "us";
SELECT
	d.`Uri` as `Uri`,
	d.`Label` as `Label`,
    d.`In` as `In`
FROM `dictionary-titles` d
WHERE
	d.Label LIKE CONCAT("%", @search, "%", @search2, "%") OR
	d.Label LIKE CONCAT("%", @search2, "%", @search, "%")
ORDER BY length(d.Label) ASC, d.`In` DESC
LIMIT 10
;

# [Redirects] Prefix search
# 1-letter: 1.7
# 2-letter: 0.3
# 3-letter: [0.11, 0.05], avg: 0.06
# 4+letters: 0.05
SET @search = "col";
SELECT
	d.`LabelA` as `LabelA`,
	d.`LabelB` as `LabelB`,
	d.`UriB` as `UriB`,
    d.`InB` as `InB`
FROM `dictionary-redirects` d
WHERE
	d.`LabelA` LIKE CONCAT(@search, "%")
ORDER BY length(d.`LabelA`) ASC, d.`InB` DESC
LIMIT 10
;

# [Titles+Redirects] Exact search
# Uniform time: 0.06
SET @search = "Cool";
(
	SELECT
        '1' as `Source`,
		'' as `LabelA`,
		d.`Label` as `LabelB`,
		d.`Uri` as `UriB`,
		d.`In` as `InB`
	FROM `dictionary-titles` d
	WHERE
		d.`Label` LIKE @search OR
		d.`Label` LIKE CONCAT("the ", @search) OR
		d.`Label` LIKE CONCAT("a ", @search) OR
		d.`Label` LIKE CONCAT("an ", @search) OR
		d.`Label` LIKE CONCAT("\"", @search) OR
		d.`Label` LIKE CONCAT("'", @search)
	ORDER BY length(d.`LabelB`) ASC, d.`InB` DESC
	LIMIT 10
) UNION (
	SELECT
        '2' as `Source`,
		d.`LabelA` as `LabelA`,
		d.`LabelB` as `LabelB`,
		d.`UriB` as `UriB`,
		d.`InB` as `InB`
	FROM `dictionary-redirects` d
	WHERE
		d.`LabelA` LIKE @search
	ORDER BY length(d.`LabelA`) ASC, d.`InB` DESC
	LIMIT 10
)
ORDER BY `Source` ASC;






# Final Strategy Benchmark
# For searches with length >= 4
# Performance: [0.045, 0.06]
SET @search = "us pr";
SET @sep = ',,,';
(
	SELECT
		1 as `Source`,
		MIN(d.`LabelA`) as `LabelA`,
		GROUP_CONCAT(d.`LabelB`, @sep) as `LabelB`,
		d.`A` as `UriA`,
		GROUP_CONCAT(d.`B`, @sep) as `UriB`,
		GROUP_CONCAT(d.`InB`, @sep) as `InB`,
		length(MIN(d.`LabelA`)) as `Length`
	FROM `dictionary-disambiguation` d
	WHERE
		d.`LabelA` LIKE @search
	GROUP BY d.`A`
	LIMIT 10
) UNION (
	SELECT
        2 as `Source`,
		NULL as `LabelA`,
		d.`Label` as `LabelB`,
        NULL as `UriA`,
		d.`Uri` as `UriB`,
		d.`In` as `InB`,
        length(d.`Label`) as `Length`
	FROM `dictionary-titles` d
	WHERE
		d.`Label` LIKE @search
	ORDER BY d.`In` DESC
	LIMIT 10
) UNION (
	SELECT
        3 as `Source`,
		d.`LabelA` as `LabelA`,
		d.`LabelB` as `LabelB`,
		NULL as `UriA`,
		d.`UriB` as `UriB`,
		d.`InB` as `InB`,
        length(d.`LabelA`) as `Length`
	FROM `dictionary-redirects` d
	WHERE
		d.`LabelA` LIKE @search
	ORDER BY d.`InB` DESC
	LIMIT 10
) UNION (
	SELECT
		4 as `Source`,
		MIN(d.`LabelA`) as `LabelA`,
		GROUP_CONCAT(d.`LabelB`, @sep) as `LabelB`,
		d.`A` as `UriA`,
		GROUP_CONCAT(d.`B`, @sep) as `UriB`,
		GROUP_CONCAT(d.`InB`, @sep) as `InB`,
		length(MIN(d.`LabelA`)) as `Length`
	FROM `dictionary-disambiguation` d
	WHERE
		d.`LabelA` LIKE CONCAT(@search, "%")
	GROUP BY d.`A`
	ORDER BY length(MIN(d.`LabelA`)) ASC
	LIMIT 10
) UNION (
	SELECT
        5 as `Source`,
		NULL as `LabelA`,
		d.`Label` as `LabelB`,
        NULL as `UriA`,
		d.`Uri` as `UriB`,
		d.`In` as `InB`,
        length(d.`Label`) as `Length`
	FROM `dictionary-titles` d
	WHERE
		d.`Label` LIKE CONCAT(@search, "%")
	ORDER BY length(d.`Label`) ASC, d.`In` DESC
	LIMIT 10
) UNION (
	SELECT
        6 as `Source`,
		d.`LabelA` as `LabelA`,
		d.`LabelB` as `LabelB`,
		NULL as `UriA`,
		d.`UriB` as `UriB`,
		d.`InB` as `InB`,
        length(d.`LabelA`) as `Length`
	FROM `dictionary-redirects` d
	WHERE
		d.`LabelA` LIKE CONCAT(@search, "%")
	ORDER BY length(d.`LabelA`) ASC, d.`InB` DESC
	LIMIT 10
) ORDER BY `Length` ASC, `InB` DESC, `Source` ASC;


# Final Strategy Benchmark
# For searches with length <[2,3]
# Performance: [0.05]
SET @search = "us";
SET @sep = ',,,';
(
	SELECT
		1 as `Source`,
		MIN(d.`LabelA`) as `LabelA`,
		GROUP_CONCAT(d.`LabelB`, @sep) as `LabelB`,
		d.`A` as `UriA`,
		GROUP_CONCAT(d.`B`, @sep) as `UriB`,
		GROUP_CONCAT(d.`InB`, @sep) as `InB`,
		length(MIN(d.`LabelA`)) as `Length`
	FROM `dictionary-disambiguation` d
	WHERE
		d.`LabelA` LIKE @search
	GROUP BY d.`A`
	LIMIT 10
) UNION (
	SELECT
        2 as `Source`,
		NULL as `LabelA`,
		d.`Label` as `LabelB`,
        NULL as `UriA`,
		d.`Uri` as `UriB`,
		d.`In` as `InB`,
        length(d.`Label`) as `Length`
	FROM `dictionary-titles` d
	WHERE
		d.`Label` LIKE @search
	ORDER BY d.`In` DESC
	LIMIT 10
) UNION (
	SELECT
        3 as `Source`,
		d.`LabelA` as `LabelA`,
		d.`LabelB` as `LabelB`,
		NULL as `UriA`,
		d.`UriB` as `UriB`,
		d.`InB` as `InB`,
        length(d.`LabelA`) as `Length`
	FROM `dictionary-redirects` d
	WHERE
		d.`LabelA` LIKE @search
	ORDER BY d.`InB` DESC
	LIMIT 10
) UNION (
	SELECT
		4 as `Source`,
		MIN(d.`LabelA`) as `LabelA`,
		GROUP_CONCAT(d.`LabelB`, @sep) as `LabelB`,
		d.`A` as `UriA`,
		GROUP_CONCAT(d.`B`, @sep) as `UriB`,
		GROUP_CONCAT(d.`InB`, @sep) as `InB`,
		length(MIN(d.`LabelA`)) as `Length`
	FROM `dictionary-disambiguation` d
	WHERE
		d.`LabelA` LIKE CONCAT(@search, "%")
	GROUP BY d.`A`
	LIMIT 10
) UNION (
	SELECT
        5 as `Source`,
		NULL as `LabelA`,
		d.`Label` as `LabelB`,
        NULL as `UriA`,
		d.`Uri` as `UriB`,
		d.`In` as `InB`,
        length(d.`Label`) as `Length`
	FROM `dictionary-titles` d
	WHERE
		d.`Label` LIKE CONCAT(@search, "%")
	LIMIT 10
) UNION (
	SELECT
        6 as `Source`,
		d.`LabelA` as `LabelA`,
		d.`LabelB` as `LabelB`,
		NULL as `UriA`,
		d.`UriB` as `UriB`,
		d.`InB` as `InB`,
        length(d.`LabelA`) as `Length`
	FROM `dictionary-redirects` d
	WHERE
		d.`LabelA` LIKE CONCAT(@search, "%")
	LIMIT 10
) ORDER BY `Length` ASC, `InB` DESC, `Source` ASC;

# Final Strategy Benchmark
# For searches with length = 1
# Performance: [0.05]
SET @search = "c";
SET @sep = ',,,';
(
	SELECT
		1 as `Source`,
		MIN(d.`LabelA`) as `LabelA`,
		GROUP_CONCAT(d.`LabelB`, @sep) as `LabelB`,
		d.`A` as `UriA`,
		GROUP_CONCAT(d.`B`, @sep) as `UriB`,
		GROUP_CONCAT(d.`InB`, @sep) as `InB`,
		length(MIN(d.`LabelA`)) as `Length`
	FROM `dictionary-disambiguation` d
	WHERE
		d.`LabelA` LIKE @search
	GROUP BY d.`A`
	LIMIT 10
) UNION (
	SELECT
        2 as `Source`,
		NULL as `LabelA`,
		d.`Label` as `LabelB`,
        NULL as `UriA`,
		d.`Uri` as `UriB`,
		d.`In` as `InB`,
        length(d.`Label`) as `Length`
	FROM `dictionary-titles` d
	WHERE
		d.`Label` LIKE @search
	ORDER BY d.`In` DESC
	LIMIT 10
) UNION (
	SELECT
        3 as `Source`,
		d.`LabelA` as `LabelA`,
		d.`LabelB` as `LabelB`,
		NULL as `UriA`,
		d.`UriB` as `UriB`,
		d.`InB` as `InB`,
        length(d.`LabelA`) as `Length`
	FROM `dictionary-redirects` d
	WHERE
		d.`LabelA` LIKE @search
	ORDER BY d.`InB` DESC
	LIMIT 10
) ORDER BY `InB` DESC, `Source` ASC;
