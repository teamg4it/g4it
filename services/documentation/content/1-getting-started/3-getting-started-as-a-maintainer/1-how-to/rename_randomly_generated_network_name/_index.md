---
title: "Rename randomly generated network name"
description: "How to rename all the networks with randomly generated name to Network {number} format"
date: 2025-06-18T07:36:53+01:00
weight: 6
---

1. Execute the procedure

```sql
create or replace procedure rename_randomly_generated_network_name()
language plpgsql
as $$
declare
	rec record;
	new_network_name varchar;
begin
	for rec in
		select id, name, type,
		row_number() over (partition by digital_service_uid order by digital_service_uid) as counter,
		digital_service_uid from in_physical_equipment ipe
		where name ~ '........-....-....-....-............'
		and type = 'Network'
		and digital_service_uid notnull
	loop
		new_network_name := format('Network %s',rec.counter);
		update in_physical_equipment
		set "name" = new_network_name
		where id = rec.id;

		update out_physical_equipment
		set "name" = new_network_name
		where "name" = rec.name;

		raise notice 'Network name % from digital service % renamed to %', rec.name, rec.digital_service_uid, new_network_name;
	end loop;
end;
$$
```

2. Run the procedure

```sql
call rename_randomly_generated_network_name();
```
