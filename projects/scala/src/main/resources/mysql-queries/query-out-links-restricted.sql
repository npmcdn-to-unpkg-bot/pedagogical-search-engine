/*
 *	$_1 	String	a comma separated list of entities to search for out-links
 * 	$_2 	String 	a comma separated list of entities that the links can point to
 */

SELECT A, B, Complete
FROM `wlm-all`
WHERE A IN ($_1) AND B IN ($_2)
