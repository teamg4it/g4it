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

--TODO to be uncommented when these tables are created with ecomind
--alter table in_ai_infrastructure 
--drop constraint "ai_infrastructure_digital_service_fk";
--alter table in_ai_infrastructure
--add constraint "ai_infrastructure_digital_service_fk"
--foreign key(digital_service_uid)
--references digital_service
--on delete cascade;

--alter table in_ai_parameters 
--drop constraint "ai_parameter_digital_service_fk";
--alter table in_ai_parameters
--add constraint "ai_parameter_digital_service_fk"
--foreign key(digital_service_uid)
--references digital_service
--on delete cascade;

--alter table out_ai_reco 
--drop constraint "ai_recommendations_digital_service_fk";
--alter table out_ai_reco 
--add constraint "ai_recommendations_digital_service_fk"
--foreign key(digital_service_uid)
--references digital_service
--on delete cascade;
--TODO to be uncommented when these tables are created with ecomind

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
		if not exists (
			select 1 from g4it_user where email = user_email
		) then
			insert into user_deletion_logs(date, user_email, message)
			values (now(), user_email, 'User ' || user_email || ' does not exist');
		else
			-- retrieving all orgs of a user
			for rec in 
				select giuo.organization_id, giuo.user_id, giuo.id as user_org_id, gio.subscriber_id from g4it_user_organization giuo 
				join g4it_user u on u.id = giuo.user_id
				join g4it_organization gio on gio.id = giuo.organization_id
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
	
					delete from digital_service_shared dss
					using digital_service ds
					where ds.organization_id = rec.organization_id
					and ds.uid = dss.digital_service_uid;
					insert into user_deletion_logs(date, user_email, message)
					values (now(), user_email, 'Digital services shared linked to organization ' || rec.organization_id || ' have been deleted');

					delete from digital_service ds
					where ds.organization_id = rec.organization_id;
					insert into user_deletion_logs(date, user_email, message)
					values (now(), user_email, 'Digital services linked to organization ' || rec.organization_id || ' have been deleted');
	
					-- deletion g4it_user_role_organization row
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
					insert into user_deletion_logs(date, user_email, message)
					values (now(), user_email, 'User ' || user_email || ' is not alone in organization ' || rec.organization_id);
	
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
						insert into user_deletion_logs(date, user_email, message)
						values (now(), user_email, 'User ' || user_email || ' is sole administrator of organization ' || rec.organization_id || ' which contains other users' );
					-- if there is more than an administrator in the organization or if the user is a member				
					else
						insert into user_deletion_logs(date, user_email, message)
						values (now(), user_email, 'User ' || user_email || ' is not the only administrator of organization ' || rec.organization_id || ' or is just a member' );
					
						-- deletion of inventories linked to the user
						delete from inventory inv
						using g4it_user u
						where inv.organization_id = rec.organization_id
						and inv.created_by = u.id
						and u.email = user_email;
						insert into user_deletion_logs(date, user_email, message)
						values (now(), user_email, 'Inventories of user ' || user_email || ' linked to organization ' || rec.organization_id || ' have been deleted');

						-- deletion of digital services shared to this user
						delete from digital_service_shared dss
						using g4it_user u
						where dss.user_id = u.id
						and u.email = user_email
						and dss.organization_id = rec.organization_id;
						insert into user_deletion_logs(date, user_email, message)
						values (now(), user_email, 'Digital services shared shared to user ' || user_email || ' have been deleted');						

						-- deletion of digital services not shared with other users
						delete from digital_service as ds 
						using g4it_user u
						where ds.uid not in (select digital_service_uid from digital_service_shared)
						and ds.user_id = u.id
						and u.email = user_email
						and ds.organization_id = rec.organization_id;
						insert into user_deletion_logs(date, user_email, message)
						values (now(), user_email, 'Digital services not shared with other users by user ' || user_email || ' have been deleted');
	
						-- deletion g4it_user_role_organization row
						delete from g4it_user_role_organization
						where user_organization_id = rec.user_org_id;
						insert into user_deletion_logs(date, user_email, message)
						values (now(), user_email, 'User role in organization ' || rec.organization_id || ' has been deleted');
		
						-- deletion of g4it_user_organization row
						delete from g4it_user_organization 
						where id = rec.user_org_id;
						insert into user_deletion_logs(date, user_email, message)
						values (now(), user_email, 'Link between user ' || user_email || ' and organization ' || rec.organization_id || ' has been deleted');

					end if;
				end if;
			end loop;
			-- user and subscriber link deletion if not linked anymore to an organization
			if not exists (select 1 from g4it_user_organization where user_id = rec.user_id) then
				
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, user_email || ' is not linked to an organization anymore - link with subscriber can be deleted');

				-- deletion of g4it_user_role_subscriber row
				delete from g4it_user_role_subscriber giurs
				using g4it_user_subscriber gius
				where gius.user_id = rec.user_id
				and giurs.user_subscriber_id = gius.id;
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'User role in subscriber ' || rec.subscriber_id || ' has been deleted');

				-- deletion of g4it_user_subscriber row
				delete from g4it_user_subscriber 
				where user_id = rec.user_id;
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'Link between user ' || user_email || ' and subscriber ' || rec.subscriber_id || ' has been deleted');

				delete from g4it_user
				where email = user_email;
	
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'User ' || user_email || ' has been deleted');
			else
				insert into user_deletion_logs(date, user_email, message)
				values (now(), user_email, 'User ' || user_email || ' has not been deleted - he is still linked to one or more organizations');
			end if;
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
