---
title: "Rollback migration"
description: "SQL queries to rollback changes due to migration into the new format"
date: 2024-12-17T17:00:00+01:00
weight: 4
---

## SQL queries to rollback new data created by the migration

```
DELETE FROM in_datacenter WHERE digital_service_uid IN (SELECT uid FROM digital_service WHERE is_migrated=true);
DELETE FROM in_physical_equipment WHERE digital_service_uid IN (SELECT uid FROM digital_service WHERE is_migrated=true);
DELETE FROM in_virtual_equipment WHERE infrastructure_type='NON_CLOUD_SERVERS' AND digital_service_uid IN (SELECT uid FROM digital_service WHERE is_migrated=true);
DELETE FROM in_application WHERE digital_service_uid IN (SELECT uid FROM digital_service WHERE is_migrated=true);

DELETE FROM out_physical_equipment WHERE task_id IN (SELECT id FROM task t WHERE t.digital_service_uid IN (SELECT uid FROM digital_service WHERE is_migrated=true));
DELETE FROM out_virtual_equipment where infrastructure_type='NON_CLOUD_SERVERS' AND task_id IN (SELECT id FROM task t WHERE t.digital_service_uid IN (SELECT uid FROM digital_service WHERE is_migrated=true));
DELETE FROM out_application WHERE task_id IN (SELECT id FROM task t WHERE t.digital_service_uid IN (SELECT uid FROM digital_service WHERE is_migrated=true));

DELETE FROM task WHERE digital_service_uid IN (SELECT uid FROM digital_service ds WHERE ds.is_migrated=true);

UPDATE digital_service 
SET is_new_arch = false , is_migrated=false where is_migrated=true;


DELETE FROM in_datacenter WHERE inventory_id IN (select id FROM inventory WHERE is_migrated=true);
DELETE FROM in_physical_equipment WHERE inventory_id IN (select id FROM inventory WHERE is_migrated=true);
DELETE FROM in_virtual_equipment WHERE infrastructure_type='NON_CLOUD_SERVERS' AND inventory_id IN (select id FROM inventory WHERE is_migrated=true);
DELETE FROM in_application WHERE inventory_id IN (select id FROM inventory WHERE is_migrated=true);

DELETE FROM out_physical_equipment WHERE task_id IN (SELECT id FROM task t WHERE t.inventory_id IN (SELECT id FROM inventory WHERE is_migrated=true));
DELETE FROM out_virtual_equipment where infrastructure_type='NON_CLOUD_SERVERS' AND task_id IN (SELECT id FROM task t WHERE t.inventory_id IN (SELECT id FROM inventory WHERE is_migrated=true));
DELETE FROM out_application WHERE task_id IN (SELECT id FROM task t WHERE t.inventory_id IN (SELECT id FROM inventory WHERE is_migrated=true));

DELETE FROM task WHERE inventory_id IN (SELECT id FROM inventory i WHERE i.is_migrated=true);

UPDATE inventory 
SET is_new_arch = false , is_migrated=false WHERE is_migrated=true;

```
