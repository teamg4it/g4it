---
title: 'Add user rights to end user'
weight: 2
---

## Add a subscriber and an organization

Create a subscriber and an organization

1. Execute the create procedure script

```sql
create or replace procedure add_subscriber(
   subscriber varchar, 
   subscriber_domains varchar
)
language plpgsql    
as $$
begin
	-- Create subscriber if not exist
	insert into g4it_subscriber (name, creation_date, last_update_date, authorized_domains)
		select subscriber, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, subscriber_domains where not exists (
			select s.name from g4it_subscriber s where s.name = subscriber
		);

    commit;
end;$$
```

2. Run the procedure, ex :

```sql
# call add_subscriber(subscriber, subscriber_authorized_domains);
call add_subscriber('SUBSCRIBER-DEMO', NULL);
call add_subscriber('SUBSCRIBER-DEMO', 'g4it.com,gmail.com');
```

3. Drop the procedure

```sql
drop procedure add_subscriber;
```


## Add Subscriber's Administrator rights to user.

1. Execute the administrator procedure script

```sql
create or replace procedure add_administator_role_on_subscriber(
   usermail varchar,
   subscriber varchar
)
language plpgsql    
as $$
declare
userid int;
subscriberid int;
begin
    SELECT id INTO STRICT userid FROM g4it_user where email = usermail;
    SELECT id INTO STRICT subscriberid FROM g4it_subscriber where name = subscriber;
    
    -- Link user with subscriber
	insert into g4it_user_subscriber (user_id, subscriber_id, default_flag)
		select userid, subscriberid, true where not exists (
			select us.user_id, us.subscriber_id from g4it_user_subscriber us where us.user_id = userid and us.subscriber_id = subscriberid
		);
	-- Add administrator role on subscriber to manage organizations
	insert into g4it_user_role_subscriber (user_subscriber_id, role_id)
		select us.id, r.id from g4it_user_subscriber us, g4it_role r 
			where us.user_id = userid
			and us.subscriber_id = subscriberid
			and r.name = 'ROLE_SUBSCRIBER_ADMINISTRATOR'
			and not exists (
			    select urs.user_subscriber_id, urs.role_id from g4it_user_role_subscriber urs where urs.user_subscriber_id = us.id and urs.role_id = r.id
			);
    commit;
end;$$
```
2. Run the procedure, ex

```sql
call add_administator_role_on_subscriber('admin@g4it.com', 'SUBSCRIBER-DEMO');
```

3. Drop the procedure

```sql
drop procedure add_administator_role_on_subscriber;
```

## Remove user's rights

1. Execute the create procedure script

```sql
create or replace procedure remove_user_role_on_subscriber(
   usermail varchar,
   subscriber varchar
)
language plpgsql    
as $$
begin
    
    -- Remove user role on subscriber
    delete from g4it_user_role_subscriber
    where user_subscriber_id = (select gius.id from g4it_user_subscriber gius 
        inner join g4it_user gu on gu.id = gius.user_id
        inner join g4it_subscriber gis on gis.id = gius.subscriber_id 
        where gu.email = usermail
        and gis.name = subscriber);
         
	-- Remove link user with subscriber
	delete from g4it_user_subscriber
	where user_id = (select id from g4it_user where email = usermail)
	and subscriber_id = (select id from g4it_subscriber where name = subscriber);

    commit;
end;$$
```

2. Run the procedure, ex

```sql
call remove_user_role_on_subscriber('admin@g4it.com', 'SUBSCRIBER-DEMO');
```

3. Drop the procedure

```sql
drop procedure remove_user_role_on_subscriber;
```

