---
title: "Rollback automatic digital services migration from DEMO workspace to new workspace"
description: "How to rollback the automatic migration of digital services from DEMO workspace to new workspace"
date: 2025-05-20T14:28:06+01:00
weight: 3
---

### Rollback script

1. Execute the rollback procedure

```sql
create or replace procedure rollback_ds_migration_to_new_workspace()
language plpgsql
as $$
declare
	org_rec record;
begin
	raise notice 'Launch of digital services migration rollback script';

	--update digital_service to bring back the old link with DEMO organization
	update digital_service ds
	set organization_id = dsrollback.old_organization_id
	from ds_migration_rollback dsrollback
	where ds.uid = dsrollback.digital_service_id;

	raise notice 'Organizations linked with digital services rollbacked - DEMO is now the new organization linked as before the migration';
	
	--update digital_service_shared to bring back the old link with DEMO organization
	update digital_service_shared dss 
	set organization_id = dssrollback.old_organization_id
	from dss_migration_rollback dssrollback
	where dss.id = dssrollback.digital_service_shared_id;

	raise notice 'Organizations linked with digital services shared rollbacked - DEMO is now the new organization linked as before the migration';

	-- for each organization newly created we check if there is still a ds or an inventory (which has been created after the migration)
	for org_rec in
		select new_organization_id from ds_migration_new_organization_created_rollback
	loop
		if exists (
			select 1 from digital_service ds
			where ds.organization_id = org_rec.new_organization_id
		) or exists (
			select 1 from inventory inv
			where inv.organization_id = org_rec.new_organization_id
		) then
			raise notice 'Organization % cannot be deleted because a digital service or an inventory is still linked to it', org_rec.new_organization_id;
		else 
	
			--delete new user_role_organization rows
			delete from g4it_user_role_organization giuro
			using g4it_user_organization giuo
			where giuo.id = giuro.user_organization_id 
			and giuo.organization_id = org_rec.new_organization_id;
			raise notice 'New users role for organization % deleted', org_rec.new_organization_id;
			
			--delete new g4it_user_organization rows
			delete from g4it_user_organization giuo 
			where giuo.organization_id = org_rec.new_organization_id;
			raise notice 'New users link with organization % deleted', org_rec.new_organization_id;
			
			--delete the new organizations
			delete from g4it_organization gio
			where gio.id = org_rec.new_organization_id;
			raise notice 'New organizations % created with the migration deleted', org_rec.new_organization_id;

			--delete the new organization created from the rollback table
			delete from ds_migration_new_organization_created_rollback 
			where new_organization_id = org_rec.new_organization_id;
			raise notice 'New organization % deleted from rollback table', org_rec.new_organization_id;

		end if;
	end loop;

	--truncate rollback tables
	truncate table ds_migration_rollback;
	truncate table dss_migration_rollback;

	raise notice 'Rollback ended with success';
	end;
$$
```

2. Run the procedure

```sql
call rollback_ds_migration_to_new_workspace();
```
