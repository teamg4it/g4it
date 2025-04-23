---
title: 'Add storage retention configuration'
weight: 3
---

## Add storage retention configuration

This configuration is used to automatically delete old files, located in Azure Storage and specifically in **output** and **export** directories.

### Default Configuration
The **default** storage retention configuration can be configured as environment variables
  
More precisely with:
- G4IT_STORAGE_RETENTION_DAY_EXPORT
- G4IT_STORAGE_RETENTION_DAY_OUTPUT

Also, the cron is set in G4IT_STORAGE_RETENTION_CRON variable.
  
And G4IT_STORAGE_RETENTION_ONINIT = "true" forces the deletion to be executed when the pod is starting (for test purposes).

### Fine-grained Configuration
Storage retention configurations are located in the database, in **tables** :
- **g4it_subscriber**
- **g4it_organization**

In **columns** named :
- **storage_retention_day_export**
- **storage_retention_day_output**

Priority of configurations: organization > subscriber > default.

1. Examples of configuration
```sql
SELECT * FROM g4it_subscriber; -- check the config
UPDATE g4it_subscriber SET storage_retention_day_export = 10 WHERE id = 1; -- 10 and 1 must be changed in your case
UPDATE g4it_subscriber SET storage_retention_day_output = 10 WHERE id = 1; -- 10 and 1 must be changed in your case

-- Same example for organization
SELECT * FROM g4it_organization; -- check the config
UPDATE g4it_organization SET storage_retention_day_export = 10 WHERE id = 1; -- 10 and 1 must be changed in your case
UPDATE g4it_organization SET storage_retention_day_output = 10 WHERE id = 1; -- 10 and 1 must be changed in your case

-- the configuration will be taken into account at the execution of automatic deletion
-- which is configured every day at 7am
```

