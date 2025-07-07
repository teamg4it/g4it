---
title: "Rename randomly generated terminal name"
description: "How to rename all the terminals with randomly generated name to Terminal {number} format"
date: 2025-06-12T14:28:06+01:00
weight: 3
---

1. Execute the procedure

```sql
create or replace procedure rename_randomly_generated_terminal_name()
language plpgsql
as $$
declare
	rec record;
	new_terminal_name varchar;
begin
	for rec in
		select id, name, type,
		row_number() over (partition by digital_service_uid order by digital_service_uid) as counter,
		digital_service_uid from in_physical_equipment ipe
		where name ~ '........-....-....-....-............'
		and type = 'Terminal'
		and digital_service_uid notnull
	loop
		new_terminal_name := format('Terminal %s',rec.counter);
		update in_physical_equipment
		set "name" = new_terminal_name
		where id = rec.id;

		update out_physical_equipment
		set "name" = new_terminal_name
		where "name" = rec.name;

		raise notice 'Terminal name % from digital service % renamed to %', rec.name, rec.digital_service_uid, new_terminal_name;
	end loop;
end;
$$
```

2. Run the procedure

```sql
call rename_randomly_generated_terminal_name();
```
