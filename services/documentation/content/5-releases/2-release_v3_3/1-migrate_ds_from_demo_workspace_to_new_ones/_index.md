---
title: "Automatically migrate digital services from DEMO workspace to new workspace"
description: "How to automatically migrate digital services from DEMO workspace to new workspace"
date: 2025-05-20T14:28:06+01:00
weight: 3
---

### Deletion of empty digital services

If you want to manually migrate digital services from DEMO workspace to new workspace, you first have to delete empty digital services which are useless.
To do this you need to execute the following script :

```sql
-- Step 0 : Check the number of digital services to be deleted
SELECT count(*)
FROM digital_service ds
WHERE NOT EXISTS (
    SELECT 1
    FROM in_physical_equipment ipe
    WHERE ipe.digital_service_uid = ds.uid
)
AND NOT EXISTS (
    SELECT 1
    FROM in_virtual_equipment ive
    WHERE ive.digital_service_uid = ds.uid
);

-- Step 1 : Delete the link associated to digital services with no equipment created
DELETE FROM digital_service_link
WHERE digital_service_uid IN (
    SELECT ds.uid
    FROM digital_service ds
    WHERE NOT EXISTS (
        SELECT 1 FROM in_physical_equipment ipe WHERE ipe.digital_service_uid = ds.uid
    )
    AND NOT EXISTS (
        SELECT 1 FROM in_virtual_equipment ive WHERE ive.digital_service_uid = ds.uid
    )
);

-- Step 2 : Delete the shared link associated to digital services with no equipment created
DELETE FROM digital_service_shared
WHERE digital_service_uid IN (
    SELECT ds.uid
    FROM digital_service ds
    WHERE NOT EXISTS (
        SELECT 1 FROM in_physical_equipment ipe WHERE ipe.digital_service_uid = ds.uid
    )
    AND NOT EXISTS (
        SELECT 1 FROM in_virtual_equipment ive WHERE ive.digital_service_uid = ds.uid
    )
);

-- Step 3 : Delete the task associated to digital services with no equipment created
DELETE FROM task
WHERE digital_service_uid IN (
    SELECT ds.uid
    FROM digital_service ds
    WHERE NOT EXISTS (
        SELECT 1 FROM in_physical_equipment ipe WHERE ipe.digital_service_uid = ds.uid
    )
    AND NOT EXISTS (
        SELECT 1 FROM in_virtual_equipment ive WHERE ive.digital_service_uid = ds.uid
    )
);

-- Step 4 : Delete datacenter associated to the digital services with no equipment created

DELETE FROM in_datacenter 
WHERE digital_service_uid IN (
    SELECT ds.uid
    FROM digital_service ds
    WHERE NOT EXISTS (
        SELECT 1 FROM in_physical_equipment ipe WHERE ipe.digital_service_uid = ds.uid
    )
    AND NOT EXISTS (
        SELECT 1 FROM in_virtual_equipment ive WHERE ive.digital_service_uid = ds.uid
    )
);

-- Step 5 : Delete the digital services with no equipment created
DELETE FROM digital_service
WHERE NOT EXISTS (
    SELECT 1 FROM in_physical_equipment ipe WHERE ipe.digital_service_uid = digital_service.uid
)
AND NOT EXISTS (
    SELECT 1 FROM in_virtual_equipment ive WHERE ive.digital_service_uid = digital_service.uid
);

-- Step 6 : Delete the note associated to digital services with no equipment created

DELETE FROM note
WHERE NOT EXISTS (
    SELECT 1 FROM digital_service ds
    WHERE ds.note_id = note.id
)
AND NOT EXISTS (
    SELECT 1 FROM inventory inv
    WHERE inv.note_id = note.id
);

-- Step 7 : The result should be 0
SELECT count(*)
FROM digital_service ds
WHERE NOT EXISTS (
    SELECT 1
    FROM in_physical_equipment ipe
    WHERE ipe.digital_service_uid = ds.uid
)
AND NOT EXISTS (
    SELECT 1
    FROM in_virtual_equipment ive
    WHERE ive.digital_service_uid = ds.uid
);
```

### Migration script

1. Execute these three table creation scripts : 

```sql
create table if not exists ds_migration_rollback(
	digital_service_id varchar,
	old_organization_id int
);
```

```sql
create table if not exists dss_migration_rollback(
	digital_service_shared_id int,
	old_organization_id int
);
```

```sql
create table if not exists ds_migration_new_organization_created_rollback(
	new_organization_id int
);
```

2. Execute the migrate procedure script :

```sql
create or replace procedure migrate_ds_to_new_workspace()
language plpgsql
as $$
declare
	rec record;
	ds_rec record;
	ds_backup_rec record;
	shared_user_rec record;
	dss_backup_rec record;
	new_organization_id int;
	new_organization_name varchar;
	new_user_organization_id int;
	role_org_admin_id int;
	role_ds_write_id int;
	last_name_suffix int;
begin
	-- creation of a tmp table to increment number if there are multiple users with same lastname
	create temporary table if not exists tmp_last_name_count (
		last_name varchar,
		counter int
	);

	truncate table tmp_last_name_count;

	select id into role_org_admin_id from g4it_role where "name" = 'ROLE_ORGANIZATION_ADMINISTRATOR';
	select id into role_ds_write_id from g4it_role where "name" = 'ROLE_DIGITAL_SERVICE_WRITE';

	raise notice 'Launch of digital services migration script from DEMO workspace to new workspace';

	-- check number of users concerned by the migration, if 0 then no migration needed
	if not exists ( 
		select 1 
		from (
			select distinct ds.user_id as creator_id, gio.subscriber_id, u.last_name, u.email
			from digital_service ds 
			inner join g4it_organization gio on ds.organization_id = gio.id and gio."name" like 'DEMO' 
			inner join g4it_user u on ds.user_id = u.id
		) check_nb_users
	) then
		raise notice 'No user linked to DEMO workspace. Script ended';
		return;
	end if;

	for rec in
		select distinct ds.user_id as creator_id, organization_id, gio.subscriber_id, u.last_name, u.email
		from digital_service ds 
		inner join g4it_organization gio on ds.organization_id = gio.id and gio."name" like 'DEMO' 
		inner join g4it_user u on ds.user_id = u.id
	loop
		perform 1;

		-- Skip the user if his last name is null
		if rec.last_name is null or trim(rec.last_name) = '' then
			raise notice 'User % skipped due to null last name', rec.email;
			continue;
		end if;

		-- Increment new demo organization name if needed
		select counter into last_name_suffix
		from tmp_last_name_count
		where last_name = rec.last_name;

		if not found then
			last_name_suffix := 1;
			insert into tmp_last_name_count(last_name, counter)
			values (rec.last_name, last_name_suffix);
		else
			last_name_suffix := last_name_suffix + 1;
			update tmp_last_name_count set counter = last_name_suffix
			where last_name = rec.last_name;
		end if;

		-- Creating the new organization name based on occurences
		new_organization_name := format('DEMO-%s-%s',rec.last_name,last_name_suffix);

		raise notice 'Creation of the organization for user % - %', rec.creator_id, rec.email;
		
		-- check if organization already exists, if it's the case then we take the current id to use it later. Otherwise we insert the new organization

		new_organization_id = create_organization_from_demo_organization(new_organization_name, rec.subscriber_id);
		
		-- Insertion of creators in g4it_user_organization

		new_user_organization_id = link_user_to_organization(rec.creator_id, rec.email, new_organization_id, true);
		
		-- Adding admin role to users

		perform assign_role_to_user_organization(new_user_organization_id, role_org_admin_id, rec.creator_id, rec.email);

		-- Inserting data for a future rollback
		for ds_backup_rec in
			select uid, name, organization_id
			from digital_service
			where user_id = rec.creator_id
			and organization_id = rec.organization_id
		loop
			insert into ds_migration_rollback(digital_service_id, old_organization_id)
			values(ds_backup_rec.uid, ds_backup_rec.organization_id);
			raise notice 'Digital service % % with old organization % saved for probable rollback', ds_backup_rec.uid, ds_backup_rec.name, ds_backup_rec.organization_id;
		end loop;

		-- Associating the digital service to the new organization
		for ds_rec in
			update digital_service
			set organization_id = new_organization_id
			where user_id = rec.creator_id
			and organization_id = rec.organization_id
		returning uid, name
		loop
			raise notice 'Digital service % % now associated with organization %', ds_rec.uid, ds_rec.name, new_organization_id;

			-- Shared users
			for dss_backup_rec in
				select dss.id, dss.organization_id
				from digital_service_shared dss
				join g4it_user u ON u.id = dss.user_id
				where dss.user_id != rec.creator_id and dss.digital_service_uid = ds_rec.uid
			loop
				-- Inserting current data for a future rollback
				insert into dss_migration_rollback(digital_service_shared_id, old_organization_id)
				values(dss_backup_rec.id, dss_backup_rec.organization_id);
				raise notice 'Digital service shared % with old organization % saved for probable rollback', dss_backup_rec.id, dss_backup_rec.organization_id;
			end loop;

			for shared_user_rec in
				select distinct dss.user_id as shared_user_id, u.email
				from digital_service_shared dss
				join g4it_user u ON u.id = dss.user_id
				where dss.user_id != rec.creator_id and dss.digital_service_uid = ds_rec.uid
			loop
				raise notice 'Shared user % - % detected', shared_user_rec.shared_user_id, shared_user_rec.email;
				
				-- Insertion of shared users in g4it_user_organization

				new_user_organization_id = link_user_to_organization(shared_user_rec.shared_user_id, shared_user_rec.email, new_organization_id, false);
				
				-- Adding write role to shared users

				perform assign_role_to_user_organization(new_user_organization_id, role_ds_write_id, shared_user_rec.shared_user_id, shared_user_rec.email);

				-- Associating the digital service shared to the new organization
				update digital_service_shared
				set organization_id = new_organization_id
				where digital_service_uid = ds_rec.uid
				and user_id = shared_user_rec.shared_user_id;
				raise notice 'Digital service shared % now associated with organization %', shared_user_rec.shared_user_id, new_organization_id;

			end loop;
		end loop;
		commit;
		raise notice 'Migration around user % - % commited', rec.creator_id, rec.email;
	end loop;

	raise notice 'Migration ended with success';
end;
$$
```

3. Execute create organization function (which is used in our procedure script)

```sql
create or replace function create_organization_from_demo_organization(
	new_organization_name text,
	subscriber_id_organization int8) returns int
language plpgsql
as $$
declare
	new_organization_id int;
begin
	if exists (
		select 1 from g4it_organization
		where name = new_organization_name
		and g4it_organization.subscriber_id = subscriber_id_organization
	) then
		raise notice 'Organization name % with subscriber % already exists', new_organization_name, subscriber_id_organization;
			
		select id into new_organization_id 
		from g4it_organization
		where name = new_organization_name
		and g4it_organization.subscriber_id = subscriber_id_organization;
	else
		insert into g4it_organization ("name", subscriber_id, status) 
		values (new_organization_name, subscriber_id_organization, 'ACTIVE')
		returning id into new_organization_id;
	
		insert into ds_migration_new_organization_created_rollback(new_organization_id)
		values (new_organization_id);

		raise notice 'Organization with id: % name: % created', new_organization_id, new_organization_name;
	end if;
	return new_organization_id;
end;
$$
```

4. Execute link user to organization function (which is used in procedure script)

```sql
create or replace function link_user_to_organization(
	creator_id int8,
	user_email varchar,
	new_organization_id int,
	default_flag boolean) returns int
language plpgsql
as $$
declare
	new_user_organization_id int;
begin
	if exists (
		select 1 from g4it_user_organization
		where user_id = creator_id
		and organization_id = new_organization_id
	) then 
		raise notice 'Link between user % - % and organization % already exists', creator_id, user_email, new_organization_id;
			
		select id into new_user_organization_id
		from g4it_user_organization
		where user_id = creator_id
		and organization_id = new_organization_id;
	else
		insert into g4it_user_organization(user_id, organization_id, default_flag)
		values (creator_id, new_organization_id, default_flag) -- true or false ?
		returning id into new_user_organization_id;

		raise notice 'User % - % linked to organization %', creator_id, user_email, new_organization_id;
	end if;
	return new_user_organization_id;
end;
$$
```

5. Execute assign role to user organization function (which is used in procedure script)

```sql
create or replace function assign_role_to_user_organization(
	new_user_organization_id int,
	role_to_assign_id int,
	creator_id int8,
	user_email varchar) returns void
language plpgsql
as $$
declare
	role_name varchar;
begin
	if exists (
		select 1 from g4it_user_role_organization
		where user_organization_id = new_user_organization_id
		and role_id = role_to_assign_id
	) then
		raise notice 'Role % is already assigned to user_organization %', role_to_assign_id, new_user_organization_id;
	else

		select name into role_name from g4it_role where id = role_to_assign_id;
		insert into g4it_user_role_organization(user_organization_id, role_id)
		values (new_user_organization_id, role_to_assign_id);
	
		raise notice 'Role % - % assigned to user % - %', role_to_assign_id, role_name, creator_id, user_email;
	end if;
end;
$$
```

6. Run the procedure

```sql
call migrate_ds_to_new_workspace();
```
