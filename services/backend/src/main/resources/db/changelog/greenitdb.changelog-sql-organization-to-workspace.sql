-- greenitdb.changelog-rename-organization-to-workspace.sql

-- Workspace renames
ALTER TABLE g4it_user_organization RENAME TO g4it_user_workspace;
ALTER TABLE g4it_user_workspace RENAME COLUMN organization_id TO workspace_id;
ALTER TABLE g4it_user_role_organization RENAME TO g4it_user_role_workspace;
ALTER TABLE g4it_user_role_workspace RENAME COLUMN user_organization_id TO user_workspace_id;

-- Organization changes
ALTER TABLE g4it_user_subscriber RENAME TO g4it_user_organization;
ALTER TABLE g4it_user_organization RENAME COLUMN subscriber_id TO organization_id;
ALTER TABLE g4it_user_role_subscriber RENAME TO g4it_user_role_organization;
ALTER TABLE g4it_user_role_organization RENAME COLUMN user_subscriber_id TO user_organization_id;

-- Inventory changes
ALTER TABLE inventory RENAME COLUMN organization_id TO workspace_id;

-- Digital service changes
ALTER TABLE digital_service RENAME COLUMN organization_id TO workspace_id;

-- workspace changes --

ALTER TABLE g4it_organization RENAME TO g4it_workspace;

ALTER TABLE g4it_workspace RENAME COLUMN subscriber_id TO organization_id;

-- organization changes -----

ALTER TABLE g4it_subscriber RENAME TO g4it_organization;