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
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.PhysicalEquipmentAllocationService;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.PhysicalEquipmentEvaluationTrace;
import com.soprasteria.g4it.backend.apiinout.repository.PhysicalEquipmentEvaluationTraceRepository;
import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apireferential.business.PhysicalEquipmentReferenceResolver;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhysicalEquipmentEvaluationTraceMigrationTest {

    @Test
    void isolatesTaskSnapshotsAndRetainsThemWhenSourceIsRemoved() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:g4it48-trace;MODE=PostgreSQL");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE task (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE in_physical_equipment (id BIGINT PRIMARY KEY, quantity DOUBLE PRECISION)");
            statement.execute("INSERT INTO task VALUES (1), (2)");
            statement.execute("INSERT INTO in_physical_equipment VALUES (10, 2.5)");
            var database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (var liquibase = new Liquibase("db/changelog/in-out/zz-physical-equipment-evaluation-trace.yml",
                    new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());

                statement.execute("""
                        INSERT INTO physical_equipment_evaluation_trace
                        (task_id, source_equipment_id, item_category, usage_context, usage_fraction,
                         effective_fraction, original_quantity)
                        VALUES (1, 10, 'DEVICES', 'BYOD', NULL, 0.57, 2.5),
                               (2, 10, 'DEVICES', 'COPE', '0.5000', 0.5, 2.5)
                        """);
                assertThrows(SQLException.class, () -> statement.execute("""
                        INSERT INTO physical_equipment_evaluation_trace
                        (task_id, source_equipment_id, item_category, effective_fraction)
                        VALUES (1, 10, 'DEVICES', 1)
                        """));
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
    }

    @Test
    void repositoryReloadAndRetryReplaceOnlyTheCurrentTaskSnapshot() throws Exception {
        String url = "jdbc:h2:mem:g4it48-trace-jpa;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url);
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE task (id BIGINT PRIMARY KEY)");
            statement.execute("INSERT INTO task VALUES (1), (2)");
            var database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (var liquibase = new Liquibase("db/changelog/in-out/zz-physical-equipment-evaluation-trace.yml",
                    new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }

        var dataSource = new DriverManagerDataSource(url);
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
                assertEquals(0.85d, retried.getEffectiveFraction());
                assertEquals(2.5d, retried.getOriginalQuantity());
                assertEquals("COPE", retried.getUsageContext());
                assertEquals(0.57d, otherTask.getEffectiveFraction());
                assertEquals("BYOD", otherTask.getUsageContext());
                assertEquals(2, traces.count());
                var mapper = Mappers.getMapper(ImpactToCsvRecord.class);
                var context = Context.builder().workspaceId(7L).datetime(LocalDateTime.of(2026, 11, 24, 12, 0)).build();
                equipment.setName("synthetic-device");
                equipment.setUsageContext("changed after evaluation");
                var impact = ImpactEquipementPhysique.builder().critere("CLIMATE_CHANGE").etapeACV("USING")
                        .statutIndicateur("OK").trace("").typeEquipement("laptop").unite("kg CO2 eq").build();
                var currentTask = traces.findByTaskIdAndSourceEquipmentIdIn(1L, List.of(10L));
                assertEquals(1, currentTask.size());
                var exported = mapper.toCsv(context, 1L, "synthetic-inventory", equipment, impact, 1d, true,
                        currentTask.getFirst());
                assertEquals("COPE", exported.get(26));
                assertEquals("0.85", exported.get(27));
                var earlierExport = mapper.toCsv(context, 2L, "synthetic-inventory", equipment, impact, 1d, true, otherTask);
                assertEquals("BYOD", earlierExport.get(26));
                assertEquals("0.57", earlierExport.get(27));
                entityManager.getTransaction().commit();
            } finally {
                entityManager.close();
            }
        } finally {
            factory.destroy();
        }
    }
}
