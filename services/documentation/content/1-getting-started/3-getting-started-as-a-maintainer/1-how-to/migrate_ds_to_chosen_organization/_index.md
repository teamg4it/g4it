---
title: "Migrate a digital service to a chosen organization"
description: "How to migrate a chosen digital service from his current organization to a chosen organization"
date: 2025-05-20T14:28:06+01:00
weight: 5
---

## Migrate a chosen digital service from his current organization to a chosen organization

1. Execute the migrate procedure script :
```sql
create or replace procedure migrate_ds_to_chosen_workspace(
	ds_uid varchar,
	new_ds_organization_id int)
language plpgsql
as $$
declare
	ds_name varchar;
	current_org_id int;
	creator_id int;
	user_email varchar;
	new_user_organization_id int;
	role_org_admin_id int;
	role_ds_write_id int;
	shared_user_rec record;
begin

	-- Check if ds exists	
	if not exists ( 
		select 1 
		from (
			select *
			from digital_service ds
			where uid = ds_uid
		) check_ds
	) then
		raise notice 'No existing digital service with id %', ds_uid;
		return;
	end if;

	-- Check if org exists
	if not exists ( 
		select 1 
		from (
			select *
			from g4it_organization o
			where id = new_ds_organization_id
		) check_org
	) then
		raise notice 'No existing organization with id %', new_ds_organization_id;
		return;
	end if;

	select name, organization_id into ds_name, current_org_id from digital_service where uid = ds_uid;
	select user_id, u.email into creator_id, user_email from digital_service join g4it_user u on u.id = user_id where uid = ds_uid;
	select id into role_org_admin_id from g4it_role where "name" = 'ROLE_ORGANIZATION_ADMINISTRATOR';
	select id into role_ds_write_id from g4it_role where "name" = 'ROLE_DIGITAL_SERVICE_WRITE';

	raise notice 'Launch of digital service % - % migration script from % organization to % organization', ds_uid, ds_name, current_org_id, new_ds_organization_id;

	-- Associating the digital service to the new organization
	update digital_service
	set organization_id = new_ds_organization_id
	where uid = ds_uid;
	raise notice 'Digital service % - % now associated with organization %', ds_uid, ds_name, new_ds_organization_id;

	-- Insertion of creator in g4it_user_organization
	new_user_organization_id = link_user_to_organization(creator_id, user_email, new_ds_organization_id, true);

	-- Adding admin role to user
	perform assign_role_to_user_organization(new_user_organization_id, role_ds_write_id, creator_id, user_email);

	for shared_user_rec in
		select user_id as shared_user_id, u.email as user_email
		from digital_service_shared dss
		join g4it_user u on u.id = dss.user_id
		where dss.digital_service_uid = ds_uid
	loop
		raise notice 'Shared user % - % detected', shared_user_rec.shared_user_id, shared_user_rec.user_email;
				
		-- Insertion of shared users in g4it_user_organization

		new_user_organization_id = link_user_to_organization(shared_user_rec.shared_user_id, shared_user_rec.user_email, new_ds_organization_id, false);
				
		-- Adding write role to shared users

		perform assign_role_to_user_organization(new_user_organization_id, role_ds_write_id, shared_user_rec.shared_user_id, shared_user_rec.user_email);
			
		-- Associating the digital service shared to the new organization
		update digital_service_shared
		set organization_id = new_ds_organization_id
		where digital_service_uid = ds_uid
		and user_id = shared_user_rec.shared_user_id;
		raise notice 'Digital service shared % now associated with organization %', shared_user_rec.shared_user_id, new_ds_organization_id;

	end loop;

end;
$$
```

2. Execute link user to organization function (which is used in procedure script)

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

3. Execute assign role to user organization function (which is used in procedure script)

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

4. Run the procedure, ex :

```sql
call migrate_ds_to_chosen_workspace('6eb6e72f-2348-4057-8a99-78622daf53ec', 2);
```
parameter 1 is the digital_service uid you want to move

parameter 2 is the new organization chosen for the digital_service
