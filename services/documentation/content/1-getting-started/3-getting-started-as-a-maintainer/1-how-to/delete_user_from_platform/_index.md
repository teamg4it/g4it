---
title: "Delete user from platform"
description: "Use a SQL procedure to delete users of your choice from platform"
date: 2025-05-22T17:00:00+01:00
weight: 4
---

## Delete users of your choice from platform

1. Create the user deletion logs table :

```sql
create table user_deletion_logs (id serial primary key not null, date timestamp, user_email text, message varchar)
```

2. Alter table to allow the drop cascade for a digital service and a user
```sql
alter table in_application
drop constraint "in_application_digital_service_fk";
alter table in_application
add constraint "in_application_digital_service_fk"
foreign key(digital_service_uid)
references digital_service
on delete cascade;

alter table in_datacenter
drop constraint "in_datacenter_digital_service_fk";
alter table in_datacenter
add constraint "in_datacenter_digital_service_fk"
foreign key(digital_service_uid)
references digital_service
on delete cascade;

alter table in_physical_equipment
drop constraint "physical_eqp_digital_service_fk";
alter table in_physical_equipment
add constraint "physical_eqp_digital_service_fk"
foreign key(digital_service_uid)
references digital_service
on delete cascade;

alter table in_virtual_equipment
drop constraint "virtual_eqp_digital_service_fk";
alter table in_virtual_equipment
add constraint "virtual_eqp_digital_service_fk"
foreign key(digital_service_uid)
references digital_service
on delete cascade;

alter table note
drop constraint "created_by_user_fk";
alter table note
add constraint "created_by_user_fk"
foreign key(created_by)
references g4it_user
on delete cascade;

alter table note
drop constraint "last_updated_by_user_fk";
alter table note
add constraint "last_updated_by_user_fk"
foreign key(last_updated_by)
references g4it_user
on delete cascade;
```

3. Execute the delete user procedure : 
```sql
create or replace procedure user_deletion(users varchar[])
language plpgsql
as $$
declare
	user_email varchar;
	rec record;
	org_name varchar;
	subscriber_name varchar;
begin
	foreach user_email in array users
	loop

		-- retrieving all orgs of a user
		for rec in 
			select giuo.organization_id, giuo.user_id, giuo.id as user_org_id from g4it_user_organization giuo 
			join g4it_user u on u.id = giuo.user_id
			where email = user_email
		loop
			-- if there is only one user in the organization (it means this user is user_email)
			if (select count(*) from g4it_user_organization
				where organization_id = rec.organization_id) = 1
			then
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'User ' || user_email || ' is alone in organization ' || rec.organization_id || ', all inventories and ds of the organization and the organization itself will be deleted');
				
				-- deletion of all inventories and digital services linked to the organization
				delete from inventory inv
				where inv.organization_id = rec.organization_id;
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'Inventories linked to organization ' || rec.organization_id || ' have been deleted');
			
				delete from digital_service ds
				where ds.organization_id = rec.organization_id;
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'Digital services linked to organization ' || rec.organization_id || ' have been deleted');
			
				-- deletion user_role_organization row
				delete from g4it_user_role_organization
				where user_organization_id = rec.user_org_id;
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'User role in organization ' || rec.organization_id || ' has been deleted');
			
				-- deletion of g4it_user_organization row
				delete from g4it_user_organization 
				where id = rec.user_org_id;
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'Link between user ' || user_email || ' and organization ' || rec.organization_id || ' has been deleted');
				
				-- organization deletion
				delete from g4it_organization
				where id = rec.organization_id;
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'Organization ' || rec.organization_id || ' has been deleted');

			-- if there are more than one user in the organization
			else 
				-- if there is only one administrator in this organization which is user_email
				if (select count(giuo.user_id) from g4it_organization gio
					join g4it_user_organization giuo on giuo.organization_id = gio.id
					join g4it_user_role_organization giuro on giuro.user_organization_id = giuo.id
					join g4it_role gir on gir.id = giuro.role_id 
					where gir."name" = 'ROLE_ORGANIZATION_ADMINISTRATOR'
					and giuo.organization_id = rec.organization_id) = 1 and 
					(select giuo.user_id from g4it_organization gio
					join g4it_user_organization giuo on giuo.organization_id = gio.id
					join g4it_user_role_organization giuro on giuro.user_organization_id = giuo.id
					join g4it_role gir on gir.id = giuro.role_id 
					where gir."name" = 'ROLE_ORGANIZATION_ADMINISTRATOR'
					and giuo.organization_id = rec.organization_id) = rec.user_id
				then
					select org.name, sub.name into org_name, subscriber_name from g4it_organization org
					join g4it_subscriber sub on sub.id = org.subscriber_id
					where org.id = rec.organization_id;

					insert into user_deletion_logs(date, user_email, message)
					values (now(), user_email, 'User ' || user_email || ' has not been deleted : sole administrator of ' || org_name || '(' || subscriber_name || ')' );
				-- if there is more than an administrator in the organization or if the user is a member				
				else 
					delete from digital_service as ds 
					using g4it_user u
					where ds.uid not in (select digital_service_uid from digital_service_shared)
					and u.id = ds.user_id
					and u.email = user_email;

					insert into user_deletion_logs(date, user_email, message)
					values (now(), user_email, 'Digital services not shared with other users by user ' || user_email || ' have been deleted');
						
					-- deletion of g4it_user_role_organization row
					delete from g4it_user_role_organization giuro
					using g4it_user_organization giuo
					where giuo.id = giuro.user_organization_id 
					and giuo.user_id = rec.user_id
					and giuo.organization_id = rec.organization_id;
						
					insert into user_deletion_logs(date, user_email, message)
					values (now(), user_email, 'User role in organization ' || rec.organization_id || ' has been deleted');
					
					-- deletion of g4it_user_organization row
					delete from g4it_user_organization giuo 
					using g4it_organization gio						
					where giuo.user_id = rec.user_id
					and giuo.organization_id = rec.organization_id;
		
					insert into user_deletion_logs(date, user_email, message)
					values (now(), user_email, 'Link between user ' || user_email || ' and organization ' || rec.organization_id || ' has been deleted');
				end if;
			end if;
		end loop;
		-- user deletion if not linked anymore to an organization
		if not exists (select 1 from g4it_user_organization where user_id = rec.user_id) then
			delete from g4it_user
			where email = user_email;
						
			insert into user_deletion_logs(date, user_email, message)
			values (now(), user_email, 'User ' || user_email || ' has been deleted');
		else
			insert into user_deletion_logs(date, user_email, message)
			values (now(), user_email, 'User ' || user_email || ' has not been deleted - he is still linked to one or more organizations');
		end if;
	end loop;
end;
$$
```

4. Run the procedure, ex :
```sql
call user_deletion(array['email1@g4it.com','email2@g4it.com']);
```

there is only one parameter which is an array of user emails
