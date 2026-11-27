// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiinout;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class PhysicalEquipmentUsageMigrationTest {

    @Test
    void addsNullableUsageWithoutChangingLegacyInputOrResults() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:g4it48-usage;MODE=PostgreSQL");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE in_physical_equipment (id BIGINT PRIMARY KEY, quantity DOUBLE PRECISION, electricity_consumption DOUBLE PRECISION)");
            statement.execute("CREATE TABLE out_physical_equipment (id BIGINT PRIMARY KEY, quantity DOUBLE PRECISION)");
            statement.execute("INSERT INTO in_physical_equipment VALUES (1, 2.5, 125)");
            statement.execute("INSERT INTO out_physical_equipment VALUES (1, 2.5)");
            var database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (var liquibase = new Liquibase("db/changelog/in-out/zz-physical-equipment-usage.yml",
                    new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());

                try (var row = statement.executeQuery("SELECT * FROM in_physical_equipment WHERE id = 1")) {
                    assertTrue(row.next());
                    assertEquals(2.5, row.getDouble("quantity"));
                    assertEquals(125, row.getDouble("electricity_consumption"));
                    assertNull(row.getString("usage_context"));
                    assertNull(row.getString("usage_fraction"));
                }
                statement.execute("UPDATE in_physical_equipment SET usage_context = 'COPE', usage_fraction = '0.5000' WHERE id = 1");
                try (var row = statement.executeQuery("SELECT usage_fraction FROM in_physical_equipment WHERE id = 1")) {
                    assertTrue(row.next());
                    assertEquals("0.5000", row.getString(1));
                }
                try (var row = statement.executeQuery("SELECT quantity FROM out_physical_equipment WHERE id = 1")) {
                    assertTrue(row.next());
                    assertEquals(2.5, row.getDouble(1));
                }
            }
        }
    }
}
