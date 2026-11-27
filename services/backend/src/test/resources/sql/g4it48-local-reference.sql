-- This file has been created (totally or partially) with the assistance of artificial intelligence tools.
-- All content has been generated under the direct supervision of a named individual,
-- and under the AI.Backbone Orchestrator Compliance framework

BEGIN;

DO $$
BEGIN
    IF current_setting('g4it48.local_seed', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Local test data only. Explicitly set g4it48.local_seed=on before loading this fixture.';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM ref_item_type
        WHERE type = 'Smartphone' AND ref_default_item = 'smartphone-2'
          AND item_category = 'DEVICES' AND subscriber IS NULL AND workspace_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Expected the default Smartphone DEVICES type pointing to smartphone-2. No reference data changed.';
    END IF;
    IF (SELECT count(*) FROM ref_criterion WHERE code IN (
        'CLIMATE_CHANGE', 'ACIDIFICATION', 'IONISING_RADIATION', 'PARTICULATE_MATTER', 'RESOURCE_USE'
    )) <> 5 OR (SELECT count(*) FROM ref_lifecycle_step WHERE code IN (
        'MANUFACTURING', 'TRANSPORTATION', 'USING', 'END_OF_LIFE'
    )) <> 4 THEN
        RAISE EXCEPTION 'Missing required criteria or lifecycle steps. Initialize the local reference database first.';
    END IF;
END $$;

-- NULL scopes do not prevent duplicates through the existing unique constraint.
LOCK TABLE ref_item_impact IN SHARE ROW EXCLUSIVE MODE;

WITH criteria(code, factor) AS (
    VALUES ('CLIMATE_CHANGE', 1.0), ('ACIDIFICATION', 0.1),
           ('IONISING_RADIATION', 0.01), ('PARTICULATE_MATTER', 0.001), ('RESOURCE_USE', 0.0001)
), stages(code, factor) AS (
    VALUES ('MANUFACTURING', 100.0), ('TRANSPORTATION', 10.0),
           ('USING', 1.0), ('END_OF_LIFE', 5.0)
)
INSERT INTO ref_item_impact (
    criterion, lifecycle_step, name, category, avg_electricity_consumption,
    description, location, level, source, tier, unit, value, subscriber, version,
    workspace_id, is_hidden
)
SELECT c.code, s.code, 'smartphone-2', 'Specific', 10.0,
       'Synthetic Smartphone factors for allocation tests; not environmental reference values.',
       'France', '2-Equipement', 'G4IT-48 synthetic local test data - NOT FOR PRODUCTION',
       'End-user/Terminals', r.unit, c.factor * s.factor, NULL, 'g4it48-local-v1', NULL, false
FROM criteria c
CROSS JOIN stages s
JOIN ref_criterion r ON r.code = c.code
WHERE NOT EXISTS (
    SELECT 1 FROM ref_item_impact i
    WHERE i.name = 'smartphone-2' AND i.criterion = c.code AND i.lifecycle_step = s.code
      AND i.subscriber IS NULL AND i.workspace_id IS NULL
);

WITH criteria(code, factor) AS (
    VALUES ('CLIMATE_CHANGE', 0.1), ('ACIDIFICATION', 0.01),
           ('IONISING_RADIATION', 0.001), ('PARTICULATE_MATTER', 0.0001), ('RESOURCE_USE', 0.00001)
)
INSERT INTO ref_item_impact (
    criterion, lifecycle_step, name, category, description, location, level,
    source, unit, value, subscriber, version, workspace_id, is_hidden
)
SELECT c.code, 'MANUFACTURING', 'G4IT48 synthetic electricity France', 'electricity-mix',
       'Synthetic electricity factors for local allocation tests only.', 'France', '0-Base data',
       'G4IT-48 synthetic local test data - NOT FOR PRODUCTION',
       r.unit, c.factor, NULL, 'g4it48-local-v1', NULL, false
FROM criteria c
JOIN ref_criterion r ON r.code = c.code
WHERE NOT EXISTS (
    SELECT 1 FROM ref_item_impact i
    WHERE i.category = 'electricity-mix' AND i.location = 'France' AND i.criterion = c.code
      AND i.subscriber IS NULL AND i.workspace_id IS NULL
);

DO $$
BEGIN
    IF (SELECT count(*) FROM ref_item_impact
        WHERE name = 'smartphone-2' AND subscriber IS NULL AND workspace_id IS NULL
          AND criterion IN ('CLIMATE_CHANGE', 'ACIDIFICATION', 'IONISING_RADIATION',
                            'PARTICULATE_MATTER', 'RESOURCE_USE')
          AND lifecycle_step IN ('MANUFACTURING', 'TRANSPORTATION', 'USING', 'END_OF_LIFE')
          AND value > 0 AND avg_electricity_consumption > 0) <> 20 THEN
        RAISE EXCEPTION 'Expected exactly 20 usable Smartphone factors. Transaction rolled back.';
    END IF;
END $$;

COMMIT;
