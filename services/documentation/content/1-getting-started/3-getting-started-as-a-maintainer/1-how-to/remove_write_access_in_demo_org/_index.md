---
title: "Script to remove the 'write' privilege on DEMO organization"
description: "How to remove the privilege 'ROLE_DIGITAL_SERVICE_WRITE' on DEMO organization"
date: 2025-05-29T14:28:06+01:00
weight: 6
---

## Process for revoking the digital service write privilege from all non-admin users in the DEMO organization for each subscriber.

1. Execute the script to identify the non-admin users with write access on digital service module.

```sql
SELECT
    u.email,
    o.name AS organization_name,
    s.name AS subscriber_name,
    uo.id AS user_organization_id
FROM public.g4it_user_role_organization uro
JOIN public.g4it_user_organization uo ON uro.user_organization_id = uo.id
JOIN public.g4it_user u ON uo.user_id = u.id
JOIN public.g4it_organization o ON uo.organization_id = o.id
JOIN public.g4it_subscriber s ON o.subscriber_id = s.id
WHERE uro.role_id = (SELECT id FROM public.g4it_role WHERE name = 'ROLE_DIGITAL_SERVICE_WRITE')
    AND o.name COLLATE "C" = 'DEMO'
    AND NOT EXISTS (
        SELECT 1
        FROM public.g4it_user_role_organization admin_uro
        WHERE admin_uro.user_organization_id = uro.user_organization_id
        AND admin_uro.role_id = (SELECT id FROM public.g4it_role WHERE name = 'ROLE_ORGANIZATION_ADMINISTRATOR')
    );
```

2. Execute the procedure script :

```sql
 CREATE OR REPLACE PROCEDURE remove_write_role_for_demo_users()
              LANGUAGE plpgsql
              AS $$
              DECLARE
                  write_role_id BIGINT;
                  read_role_id BIGINT;
                  org_admin_role_id BIGINT;
                  removed_count INT := 0;
                  added_count INT := 0;
                  user_row RECORD;
              BEGIN
                  -- Get role IDs
                  SELECT id INTO write_role_id FROM public.g4it_role WHERE name = 'ROLE_DIGITAL_SERVICE_WRITE';
                  SELECT id INTO read_role_id FROM public.g4it_role WHERE name = 'ROLE_DIGITAL_SERVICE_READ';
                  SELECT id INTO org_admin_role_id FROM public.g4it_role WHERE name = 'ROLE_ORGANIZATION_ADMINISTRATOR';

                  -- Loop over all non-admin demo users who have the write role
                  FOR user_row IN
                      SELECT
                          uro.user_organization_id,
                          u.email,
                          o.name AS organization_name,
                          s.name AS subscriber_name  -- Added subscriber name
                      FROM public.g4it_user_role_organization uro
                      JOIN public.g4it_user_organization uo ON uro.user_organization_id = uo.id
                      JOIN public.g4it_user u ON uo.user_id = u.id
                      JOIN public.g4it_organization o ON uo.organization_id = o.id
                      JOIN public.g4it_subscriber s ON o.subscriber_id = s.id
                      WHERE uro.role_id = write_role_id
                        AND o.name COLLATE "C" = 'DEMO'
                        AND NOT EXISTS (
                              SELECT 1
                              FROM public.g4it_user_role_organization admin_uro
                              WHERE admin_uro.user_organization_id = uro.user_organization_id
                                AND admin_uro.role_id = org_admin_role_id
                        )
                  LOOP
                      -- Add read access if not already present
                      INSERT INTO public.g4it_user_role_organization (user_organization_id, role_id)
                      VALUES (user_row.user_organization_id, read_role_id)
                      ON CONFLICT (user_organization_id, role_id) DO NOTHING;

                      IF FOUND THEN
                          added_count := added_count + 1;
                      END IF;

                      -- Remove write access
                      DELETE FROM public.g4it_user_role_organization
                      WHERE user_organization_id = user_row.user_organization_id
                        AND role_id = write_role_id;

                      IF FOUND THEN
                          removed_count := removed_count + 1;
                      END IF;

                      RAISE NOTICE 'Processed user % (Organization: %, Subscriber: %) - Removed WRITE, Added READ',
                          user_row.email,
                          user_row.organization_name,
                          user_row.subscriber_name;
                  END LOOP;

                  RAISE NOTICE 'Total write roles removed: %', removed_count;
                  RAISE NOTICE 'Total read roles added: %', added_count;
              END;
              $$;
```

3. Run the procedure :

```sql
call remove_write_role_for_demo_users;
```

4. Validate if all the users identified in _STEP 1_ with the 'Processed user' from _STEP 2_.

5. Drop the procedure:

```sql
drop procedure remove_write_role_for_demo_users;
```

### Rollback script to restore the write access:

```sql
DO $$
DECLARE
    write_role_id BIGINT;
    read_role_id BIGINT;
    user_record RECORD;
    target_emails VARCHAR[] := ARRAY['abc@xyz.com', 'xyz@abc.com']; -- Input array listed from the *STEP 1*
BEGIN
    -- Get role IDs with validation
    SELECT id INTO write_role_id FROM public.g4it_role WHERE name = 'ROLE_DIGITAL_SERVICE_WRITE';
    SELECT id INTO read_role_id FROM public.g4it_role WHERE name = 'ROLE_DIGITAL_SERVICE_READ';

    IF write_role_id IS NULL OR read_role_id IS NULL THEN
        RAISE EXCEPTION 'Required roles not found';
    END IF;

    -- Process users with READ access in DEMO organizations
    FOR user_record IN
        SELECT
            u.email,
            o.name AS organization_name,
            s.name AS subscriber_name,
            uro.user_organization_id
        FROM public.g4it_user_role_organization uro
        JOIN public.g4it_user_organization uo ON uro.user_organization_id = uo.id
        JOIN public.g4it_user u ON uo.user_id = u.id
        JOIN public.g4it_organization o ON uo.organization_id = o.id
        JOIN public.g4it_subscriber s ON o.subscriber_id = s.id
        WHERE uro.role_id = read_role_id
          AND o.name COLLATE "C" = 'DEMO'
          AND u.email = ANY(target_emails)
    LOOP
        -- Grant WRITE access
        INSERT INTO public.g4it_user_role_organization
            (user_organization_id, role_id)
        VALUES
            (user_record.user_organization_id, write_role_id)
        ON CONFLICT (user_organization_id, role_id) DO NOTHING;

        -- Raise notification with user details
        RAISE NOTICE 'Write Access restored for % (Organization: %, Subscriber: %)',
            user_record.email,
            user_record.organization_name,
            user_record.subscriber_name;
    END LOOP;
END;
$$;

```
