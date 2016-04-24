/*
 *	$_1 	String	a comma separated list of entities to search for out-links
 */

SELECT A, B, Complete
FROM `wlm-all`
WHERE A IN ($_1)
