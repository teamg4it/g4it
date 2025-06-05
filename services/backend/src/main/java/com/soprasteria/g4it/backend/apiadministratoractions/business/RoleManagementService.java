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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.SQLWarning;

@Service
@Slf4j
public class RoleManagementService {

    private final JdbcTemplate jdbcTemplate;

    public RoleManagementService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public AllEvaluationStatusRest executeRoleCleanup() {
        try {
            log.info("START-- removal of Digital-service write access for non-admin users");

            jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                // Call the procedure
                try (CallableStatement cs = connection.prepareCall("CALL remove_write_role_for_demo_users()")) {
                    cs.execute();

                    //  Log NOTICE messages
                    SQLWarning warning = cs.getWarnings();
                    while (warning != null) {
                        log.info("PostgreSQL NOTICE: {}", warning.getMessage());
                        warning = warning.getNextWarning();
                    }
                }
                return null;
            });

            log.info("COMPLETED-- write access for non-admin users");
            return AllEvaluationStatusRest.builder().response("success").build();
        } catch (DataAccessException ex) {
            log.error("Failed to remove access: {}", ex.getMessage());
            return AllEvaluationStatusRest.builder().response("error").build();
        }
    }

}
