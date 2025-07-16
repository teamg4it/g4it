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

2. Execute the delete user procedure : 
```sql
create or replace procedure user_deletion(users varchar[])
language plpgsql
as $$
declare
	user_email varchar;
	rec record;
	subscriber_name varchar;
	sub_rec record;
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
