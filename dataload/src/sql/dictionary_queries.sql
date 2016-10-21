command=\dt

[MAIN]

SELECT n.nspname as "Schema",
  c.relname as "Name",
  CASE c.relkind WHEN 'r' THEN 'table' WHEN 'v' THEN 'view' WHEN 'i' THEN 'index' WHEN 'S' THEN 'sequence' WHEN 's' THEN 'special' END as "Type",
  pg_catalog.pg_get_userbyid(c.relowner) as "Owner", CASE c.relstorage WHEN 'h' THEN 'heap' WHEN 'x' THEN 'external' WHEN 'a' THEN 'append only' WHEN 'v' THEN 'none' WHEN 'c' THEN 'append only columnar' WHEN 'f' THEN 'foreign' END as "Storage"

FROM pg_catalog.pg_class c
     LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
WHERE c.relkind IN ('r','s','')
AND c.relstorage IN ('h', 'a', 'c','')
      AND n.nspname !~ '^pg_toast'
--  @schema_and_table_filter
ORDER BY 1,2;

[MAIN2]

SELECT n.nspname as "Schema",
  c.relname as "Name",
  CASE c.relkind WHEN 'r' THEN 'table' WHEN 'v' THEN 'view' WHEN 'i' THEN 'index' WHEN 'S' THEN 'sequence' WHEN 's' THEN 'special' END as "Type",
  pg_catalog.pg_get_userbyid(c.relowner) as "Owner", CASE c.relstorage WHEN 'h' THEN 'heap' WHEN 'x' THEN 'external' WHEN 'a' THEN 'append only' WHEN 'v' THEN 'none' WHEN 'c' THEN 'append only columnar' WHEN 'f' THEN 'foreign' END as "Storage"

FROM pg_catalog.pg_class c
     LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
WHERE c.relkind IN ('r','s','')
AND c.relstorage IN ('h', 'a', 'c','')
      AND n.nspname !~ '^pg_toast'
--  @schema_and_table_filter
ORDER BY 1,2;

command=\d

[OID]

SELECT c.oid,
  n.nspname,
  c.relname
FROM pg_catalog.pg_class c
     LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
WHERE c.relname ~ '^(fdic_a1)$'
  AND n.nspname ~ '^(workspace)$'
ORDER BY 2, 3;

[ENCODING]

select oid from pg_catalog.pg_class where relnamespace = 11 and relname  = 'pg_attribute_encoding';

[TABLEPROPS]

SELECT relchecks, relkind, relhasindex, relhasrules, reltriggers <> 0, relhasoids, '', reltablespace, relstorage
FROM pg_catalog.pg_class WHERE oid = '10639684';

[COLUMNS]

SELECT a.attname,
  pg_catalog.format_type(a.atttypid, a.atttypmod),
  (SELECT substring(pg_catalog.pg_get_expr(d.adbin, d.adrelid) for 128)
   FROM pg_catalog.pg_attrdef d
   WHERE d.adrelid = a.attrelid AND d.adnum = a.attnum AND a.atthasdef),
  a.attnotnull, a.attnum
FROM pg_catalog.pg_attribute a
LEFT OUTER JOIN pg_catalog.pg_attribute_encoding e
ON   e.attrelid = a .attrelid AND e.attnum = a.attnum
WHERE a.attrelid = '10639684' AND a.attnum > 0 AND NOT a.attisdropped
ORDER BY a.attnum;


[INHERIT]

SELECT c.oid::pg_catalog.regclass FROM pg_catalog.pg_class c, pg_catalog.pg_inherits i WHERE c.oid=i.inhparent AND i.inhrelid = '10639684' ORDER BY inhseqno;

[INHERIT2]
SELECT c.oid::pg_catalog.regclass FROM pg_catalog.pg_class c, pg_catalog.pg_inherits i WHERE c.oid=i.inhrelid AND i.inhparent = '10639684' ORDER BY c.relname;

[DISTRIBUTION]
SELECT attrnums
FROM pg_catalog.gp_distribution_policy t
WHERE localoid = '10639684';
