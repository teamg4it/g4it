// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiinout;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.PhysicalEquipmentAllocationService;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.PhysicalEquipmentEvaluationTrace;
import com.soprasteria.g4it.backend.apiinout.repository.PhysicalEquipmentEvaluationTraceRepository;
import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apireferential.business.PhysicalEquipmentReferenceResolver;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.util.PropertyPlaceholderHelper;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * PostgreSQL variants of the G4IT-48 migration checks. They are opt-in so that the default
 * build keeps using the H2 variants only.
 *
 * <p>Runs only with the Maven profile {@code integration-test} (JUnit tag {@code postgres-integration}).
 * Connection settings ({@code spring.datasource.url/username/password}) are read from
 * {@code application-integration-test.yml} on the test classpath; {@code ${KEY:default}} placeholders
 * are resolved from system properties, then environment variables, then their default.
 * Only a loopback host is accepted.</p>
 *
 * <p>Every test works in its own freshly created, uniquely named schema and drops only that schema
 * afterwards. Pre-existing schemas and data are never read or modified.</p>
 */
@Tag("postgres-integration")
class PhysicalEquipmentPostgresMigrationTest {

    private static final String CONFIG_FILE = "application-integration-test.yml";
    private static final Pattern LOOPBACK_URL =
            Pattern.compile("^jdbc:postgresql://(localhost|127\\.0\\.0\\.1|\\[::1])(:\\d+)?/[^/?]+(\\?.*)?$");
    private static final Pattern TEST_SCHEMA = Pattern.compile("^g4it48_t_[0-9a-f]{12}$");

    private String baseUrl;
    private String user;
    private String password;
    private String schema;

    private static String datasourceProperty(Properties properties, String key) {
        var raw = properties.getProperty("spring.datasource." + key);
        assertNotNull(raw, "spring.datasource." + key + " must be set in " + CONFIG_FILE);
        var resolver = new PropertyPlaceholderHelper("${", "}", ":", null, true);
        return resolver.replacePlaceholders(raw, name -> {
            var value = System.getProperty(name);
            return value != null ? value : System.getenv(name);
        });
    }

    @BeforeEach
    void createIsolatedSchema() throws Exception {
        var resource = new ClassPathResource(CONFIG_FILE);
        assertTrue(resource.exists(), CONFIG_FILE + " must be present on the test classpath");
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resource);
        var properties = yaml.getObject();
        assertNotNull(properties);
        baseUrl = datasourceProperty(properties, "url");
        user = datasourceProperty(properties, "username");
        password = datasourceProperty(properties, "password");
        assertTrue(LOOPBACK_URL.matcher(baseUrl).matches(),
                "spring.datasource.url must target a loopback PostgreSQL host");
        schema = "g4it48_t_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        assertTrue(TEST_SCHEMA.matcher(schema).matches());
        try (var connection = DriverManager.getConnection(baseUrl, user, password);
             var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
        }
    }

    @AfterEach
    void dropIsolatedSchema() throws Exception {
        if (schema == null || !TEST_SCHEMA.matcher(schema).matches()) {
            return;
        }
        try (var connection = DriverManager.getConnection(baseUrl, user, password);
             var statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }

    private String schemaScopedUrl() {
        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + "currentSchema=" + schema;
    }

    private Connection schemaConnection() throws SQLException {
        return DriverManager.getConnection(schemaScopedUrl(), user, password);
    }

    // Liquibase closes the connection it wraps, so migrations use a dedicated connection.
    private void migrate(Connection ignored, String changelog) throws Exception {
        var database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(schemaConnection()));
        database.setDefaultSchemaName(schema);
        database.setLiquibaseSchemaName(schema);
        try (var liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(), database)) {
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

    private String columnInfo(Connection connection, String table, String column) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT data_type || '/' || is_nullable FROM information_schema.columns
                WHERE table_schema = ? AND table_name = ? AND column_name = ?""")) {
            statement.setString(1, schema);
            statement.setString(2, table);
            statement.setString(3, column);
            try (var rows = statement.executeQuery()) {
                return rows.next() ? rows.getString(1) : null;
            }
        }
    }

    @Test
    void postgresAddsNullableUsageWithoutChangingLegacyInputOrResults() throws Exception {
        try (var connection = schemaConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE in_physical_equipment (id BIGINT PRIMARY KEY, quantity DOUBLE PRECISION, electricity_consumption DOUBLE PRECISION)");
            statement.execute("CREATE TABLE out_physical_equipment (id BIGINT PRIMARY KEY, quantity DOUBLE PRECISION)");
            statement.execute("INSERT INTO in_physical_equipment VALUES (1, 2.5, 125)");
            statement.execute("INSERT INTO out_physical_equipment VALUES (1, 2.5)");

            migrate(connection, "db/changelog/in-out/zz-physical-equipment-usage.yml");

            assertEquals("text/YES", columnInfo(connection, "in_physical_equipment", "usage_context"));
            assertEquals("text/YES", columnInfo(connection, "in_physical_equipment", "usage_fraction"));
            assertNull(columnInfo(connection, "out_physical_equipment", "usage_context"));
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
            // Re-running the changelog must be a no-op (append-only, already applied).
            migrate(connection, "db/changelog/in-out/zz-physical-equipment-usage.yml");
            try (var row = statement.executeQuery("SELECT count(*) FROM databasechangelog WHERE id = 'g4it-48-physical-equipment-source-usage'")) {
                assertTrue(row.next());
                assertEquals(1, row.getInt(1));
            }
        }
    }

    @Test
    void postgresIsolatesTaskSnapshotsAndRetainsThemWhenSourceIsRemoved() throws Exception {
        try (var connection = schemaConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE task (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE in_physical_equipment (id BIGINT PRIMARY KEY, quantity DOUBLE PRECISION)");
            statement.execute("INSERT INTO task VALUES (1), (2)");
            statement.execute("INSERT INTO in_physical_equipment VALUES (10, 2.5)");

            migrate(connection, "db/changelog/in-out/zz-physical-equipment-evaluation-trace.yml");

            assertEquals("text/YES", columnInfo(connection, "physical_equipment_evaluation_trace", "usage_context"));
            assertEquals("text/YES", columnInfo(connection, "physical_equipment_evaluation_trace", "usage_fraction"));
            assertEquals("text/NO", columnInfo(connection, "physical_equipment_evaluation_trace", "item_category"));
            assertEquals("double precision/NO", columnInfo(connection, "physical_equipment_evaluation_trace", "effective_fraction"));
            assertEquals("double precision/YES", columnInfo(connection, "physical_equipment_evaluation_trace", "original_quantity"));

            statement.execute("""
                    INSERT INTO physical_equipment_evaluation_trace
                    (task_id, source_equipment_id, item_category, usage_context, usage_fraction,
                     effective_fraction, original_quantity)
                    VALUES (1, 10, 'DEVICES', 'BYOD', NULL, 0.57, 2.5),
                           (2, 10, 'DEVICES', 'COPE', '0.5000', 0.5, 2.5)
                    """);
            var duplicate = assertThrows(SQLException.class, () -> statement.execute("""
                    INSERT INTO physical_equipment_evaluation_trace
                    (task_id, source_equipment_id, item_category, effective_fraction)
                    VALUES (1, 10, 'DEVICES', 1)
                    """));
            assertEquals("23505", duplicate.getSQLState());
            var unknownTask = assertThrows(SQLException.class, () -> statement.execute("""
                    INSERT INTO physical_equipment_evaluation_trace
                    (task_id, source_equipment_id, item_category, effective_fraction)
                    VALUES (999, 11, 'DEVICES', 1)
                    """));
            assertEquals("23503", unknownTask.getSQLState());

            statement.execute("DELETE FROM in_physical_equipment WHERE id = 10");
            try (var rows = statement.executeQuery("""
                    SELECT usage_context, usage_fraction, effective_fraction, original_quantity
                    FROM physical_equipment_evaluation_trace WHERE task_id = 2 AND source_equipment_id = 10
                    """)) {
                assertTrue(rows.next());
                assertEquals("COPE", rows.getString("usage_context"));
                assertEquals("0.5000", rows.getString("usage_fraction"));
                assertEquals(0.5, rows.getDouble("effective_fraction"));
                assertEquals(2.5, rows.getDouble("original_quantity"));
                assertFalse(rows.next());
            }
            statement.execute("DELETE FROM task WHERE id = 1");
            try (var rows = statement.executeQuery("SELECT task_id FROM physical_equipment_evaluation_trace")) {
                assertTrue(rows.next());
                assertEquals(2L, rows.getLong("task_id"));
                assertFalse(rows.next());
            }
        }
    }

    @Test
    void postgresRepositoryReloadAndRetryReplaceOnlyTheCurrentTaskSnapshot() throws Exception {
        try (var connection = schemaConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE task (id BIGINT PRIMARY KEY)");
            statement.execute("INSERT INTO task VALUES (1), (2)");
            migrate(connection, "db/changelog/in-out/zz-physical-equipment-evaluation-trace.yml");
        }

        var dataSource = new DriverManagerDataSource(schemaScopedUrl(), user, password);
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setManagedTypes(PersistenceManagedTypes.of(PhysicalEquipmentEvaluationTrace.class.getName()));
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "none"));
        factory.afterPropertiesSet();
        try {
            var entityManager = factory.getObject().createEntityManager();
            try {
                var traces = new JpaRepositoryFactory(entityManager)
                        .getRepository(PhysicalEquipmentEvaluationTraceRepository.class);
                var itemTypes = mock(ItemTypeRepository.class);
                when(itemTypes.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                        ItemType.builder().type("laptop").itemCategory("DEVICES").build()));
                var references = mock(PhysicalEquipmentReferenceResolver.class);
                when(references.resolve(any(), eq("laptop"), eq("synthetic-org"), eq(7L), eq(1L)))
                        .thenReturn(new PhysicalEquipmentReferenceResolver.Reference(null, null, null, false));
                var service = new PhysicalEquipmentAllocationService(new ItemCategoryResolver(itemTypes), references, traces);
                var equipment = InPhysicalEquipment.builder().id(10L).type("laptop")
                        .quantity(2.5d).usageContext("BYOD").build();

                entityManager.getTransaction().begin();
                var categories = service.begin(1L, "synthetic-org", 7L);
                service.save(List.of(service.resolve(equipment, 1L, "synthetic-org", 7L, 1, categories)));
                categories = service.begin(2L, "synthetic-org", 7L);
                service.save(List.of(service.resolve(equipment, 2L, "synthetic-org", 7L, 1, categories)));
                entityManager.getTransaction().commit();
                entityManager.clear();

                entityManager.getTransaction().begin();
                equipment.setUsageContext("COPE");
                categories = service.begin(1L, "synthetic-org", 7L);
                service.save(List.of(service.resolve(equipment, 1L, "synthetic-org", 7L, 1, categories)));
                entityManager.flush();
                entityManager.clear();
                var retried = traces.findByTaskIdAndSourceEquipmentId(1L, 10L).orElseThrow();
                var otherTask = traces.findByTaskIdAndSourceEquipmentId(2L, 10L).orElseThrow();
                assertNotNull(retried.getId());
                assertEquals(0.85d, retried.getEffectiveFraction());
                assertEquals(2.5d, retried.getOriginalQuantity());
                assertEquals("COPE", retried.getUsageContext());
                assertEquals(0.57d, otherTask.getEffectiveFraction());
                assertEquals("BYOD", otherTask.getUsageContext());
                assertEquals(2, traces.count());
                entityManager.getTransaction().commit();
            } finally {
                entityManager.close();
            }
        } finally {
            factory.destroy();
        }
    }

    private void prepareReferentialForLocalSeed() throws Exception {
        try (var connection = schemaConnection(); var statement = connection.createStatement()) {
            // The referential changelog updates this table, owned by a different changelog.
            statement.execute("""
                    CREATE TABLE ref_sustainable_individual_package
                    (criteria VARCHAR(255), individual_sustainable_package DOUBLE PRECISION)
                    """);
            migrate(connection, "db/changelog/greenitdb.changelog-referential-initialization.yml");
            migrate(connection, "db/changelog/greenitdb.changelog-add-item-type-item-category.yml");
        }
    }

    private String localReferenceSeed() throws Exception {
        return new ClassPathResource("sql/g4it48-local-reference.sql").getContentAsString(StandardCharsets.UTF_8);
    }

    private long impactCount(Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM ref_item_impact")) {
            assertTrue(rows.next());
            return rows.getLong(1);
        }
    }

    @Test
    void postgresLocalSeedRequiresExplicitOptInWithoutChangingReferenceData() throws Exception {
        prepareReferentialForLocalSeed();
        try (var connection = schemaConnection(); var statement = connection.createStatement()) {
            long before = impactCount(connection);
            String seed = localReferenceSeed();
            var error = assertThrows(SQLException.class, () -> statement.execute(seed));
            assertTrue(error.getMessage().contains("g4it48.local_seed=on"));
            statement.execute("ROLLBACK");
            assertEquals(before, impactCount(connection));
        }
    }

    @Test
    void postgresLocalSeedCompletesMissingFactorsIdempotentlyAndPreservesExistingOnes() throws Exception {
        prepareReferentialForLocalSeed();
        try (var connection = schemaConnection(); var statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO ref_item_impact (name, criterion, lifecycle_step, value, avg_electricity_consumption)
                    VALUES ('smartphone-2', 'CLIMATE_CHANGE', 'MANUFACTURING', 123, 10)
                    """);
            statement.execute("""
                    INSERT INTO ref_item_impact (name, criterion, lifecycle_step, value, workspace_id)
                    VALUES ('smartphone-2', 'CLIMATE_CHANGE', 'MANUFACTURING', 777, 7)
                    """);
            // Remove one mix factor only in the isolated test schema to exercise gap filling.
            statement.execute("""
                    DELETE FROM ref_item_impact
                    WHERE category = 'electricity-mix' AND location = 'France'
                      AND criterion = 'ACIDIFICATION' AND subscriber IS NULL AND workspace_id IS NULL
                    """);
            long before = impactCount(connection);
            statement.execute("SET g4it48.local_seed = 'on'");
            statement.execute(localReferenceSeed());
            assertEquals(before + 20, impactCount(connection));
            statement.execute(localReferenceSeed());
            assertEquals(before + 20, impactCount(connection));
            try (var rows = statement.executeQuery("""
                    SELECT count(*), count(DISTINCT (criterion, lifecycle_step))
                    FROM ref_item_impact
                    WHERE name = 'smartphone-2' AND subscriber IS NULL AND workspace_id IS NULL
                      AND criterion IN ('CLIMATE_CHANGE', 'ACIDIFICATION', 'IONISING_RADIATION',
                                        'PARTICULATE_MATTER', 'RESOURCE_USE')
                      AND lifecycle_step IN ('MANUFACTURING', 'TRANSPORTATION', 'USING', 'END_OF_LIFE')
                      AND value > 0 AND avg_electricity_consumption > 0
                    """)) {
                assertTrue(rows.next());
                assertEquals(20, rows.getLong(1));
                assertEquals(20, rows.getLong(2));
            }
            try (var rows = statement.executeQuery("""
                    SELECT value, workspace_id FROM ref_item_impact
                    WHERE name = 'smartphone-2' AND criterion = 'CLIMATE_CHANGE'
                      AND lifecycle_step = 'MANUFACTURING' ORDER BY workspace_id NULLS FIRST
                    """)) {
                assertTrue(rows.next());
                assertEquals(123, rows.getDouble(1));
                assertNull(rows.getObject(2));
                assertTrue(rows.next());
                assertEquals(777, rows.getDouble(1));
                assertEquals(7, rows.getLong(2));
                assertFalse(rows.next());
            }
            try (var rows = statement.executeQuery("""
                    SELECT count(DISTINCT criterion) FROM ref_item_impact
                    WHERE category = 'electricity-mix' AND location = 'France'
                      AND subscriber IS NULL AND workspace_id IS NULL
                      AND criterion IN ('CLIMATE_CHANGE', 'ACIDIFICATION', 'IONISING_RADIATION',
                                        'PARTICULATE_MATTER', 'RESOURCE_USE') AND value > 0
                    """)) {
                assertTrue(rows.next());
                assertEquals(5, rows.getInt(1));
            }
        }
    }

    @Test
    void postgresLocalSeedRollsBackGapFillingWhenAnExistingFactorIsUnusable() throws Exception {
        prepareReferentialForLocalSeed();
        try (var connection = schemaConnection(); var statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO ref_item_impact (name, criterion, lifecycle_step, value)
                    VALUES ('smartphone-2', 'CLIMATE_CHANGE', 'MANUFACTURING', -1)
                    """);
            long before = impactCount(connection);
            statement.execute("SET g4it48.local_seed = 'on'");
            String seed = localReferenceSeed();
            var error = assertThrows(SQLException.class, () -> statement.execute(seed));
            assertTrue(error.getMessage().contains("exactly 20 usable Smartphone factors"));
            statement.execute("ROLLBACK");
            assertEquals(before, impactCount(connection));
            try (var rows = statement.executeQuery("""
                    SELECT value FROM ref_item_impact WHERE name = 'smartphone-2'
                      AND criterion = 'CLIMATE_CHANGE' AND lifecycle_step = 'MANUFACTURING'
                      AND subscriber IS NULL AND workspace_id IS NULL
                    """)) {
                assertTrue(rows.next());
                assertEquals(-1, rows.getDouble(1));
            }
        }
    }
}
