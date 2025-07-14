---
title: "Add a subscriber and associated right to users"
weight: 2
---

## Add a subscriber

1. Execute the create subscriber procedure script

```sql
create or replace procedure add_subscriber(
   subscriber varchar,
   subscriber_domains varchar,
   data_retention_day int4,
   storage_retention_day_export int4,
   criteria _varchar
)
language plpgsql
as $$
begin
	-- Create subscriber if not exist
	insert into g4it_subscriber (name, creation_date, last_update_date, authorized_domains, data_retention_day,storage_retention_day_export,criteria)
		select subscriber, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, subscriber_domains, data_retention_day,storage_retention_day_export,criteria where not exists (
			select s.name from g4it_subscriber s where s.name = subscriber
		);

    commit;
end;$$
```

2. Run the procedure, ex :

```sql
# call add_subscriber(subscriber, subscriber_authorized_domains, data_retention_day, storage_retention_day_export, criteria);
call add_subscriber('SUBSCRIBER-DEMO', 'g4it.com,gmail.com',730,20,'{climate-change,ionising-radiation,acidification,particulate-matter,resource-use}');
```

3. Drop the procedure

```sql
drop procedure add_subscriber;
```

## Add an organization

1. Execute the create organization procedure script

```sql
create or replace procedure add_organization(
   subscriber varchar,
   organization varchar
)
language plpgsql
as $$
declare 
 sub_id int8;
begin	
    select id into sub_id from g4it_subscriber sub where sub.name=subscriber;	
	insert into g4it_organization (name, creation_date, last_update_date, subscriber_id, status)
		values(organization,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,sub_id,'ACTIVE');

    commit;
end;$$
```

2. Run the procedure, ex :

```sql
# call add_organization(subscriber, organization);
call add_organization('SUBSCRIBER-DEMO','DEMO');
```

3. Drop the procedure

```sql
drop procedure add_organization;
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

## Add storage account for subscriber
Each subscriber’s organization has an isolated and dedicated substructure in the platform’s storage container that is specific to the subscriber, in which the G4IT platform deposits.

At infra level make sure to create a storage account for the subscriber. If azure infra is used, follow the steps below:

1. update the variable: accounts_config in vars/$env.tfvars file by adding
```sql
accounts_config = [
  {
    secret_name  = "<THE-SUBSCRIBER-NAME>",  # Real subscriber name, in UPPER-KEBAB-CASE
    account_name = "<theinternalname>" ,     # Internal account name !!! max 14 characters !!!
    type         = "<STORAGE-TYPE>" ,        # ZRS or LRS
  }
]
```

2. Execute the pipeline with the target Environment

