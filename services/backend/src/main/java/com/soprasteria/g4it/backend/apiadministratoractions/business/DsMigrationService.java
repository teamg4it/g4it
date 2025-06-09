/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministratoractions.business;

import com.soprasteria.g4it.backend.server.gen.api.dto.AllEvaluationStatusRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.SQLWarning;

@Service
@Slf4j
public class DsMigrationService {
    private final JdbcTemplate jdbcTemplate;

    public DsMigrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public AllEvaluationStatusRest migrateDemoDs() {
        try {
            log.info("Migrate digital services from DEMO workspace to new workspaces");

            jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                // Call the SQL procedure
                try (CallableStatement cs = connection.prepareCall("CALL migrate_ds_to_new_workspace()")) {
                    cs.execute();

                    // Log raise notice messages from SQL procedure
                    SQLWarning sqlWarning = cs.getWarnings();

                    while (sqlWarning != null) {
                        log.info("PostgreSQL NOTICE : {}", sqlWarning.getMessage());
                        sqlWarning = sqlWarning.getNextWarning();
                    }
                }
                return null;
            });

            log.info("Migration done");
            return AllEvaluationStatusRest.builder().response("success").build();
        } catch (Exception e) {
            log.error("Migration failed : " + e.getCause().getMessage());
            return AllEvaluationStatusRest.builder().response("error").build();
        }
    }
}
