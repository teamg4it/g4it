// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiindicator.controller;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiindicator.business.InventoryIndicatorService;
import com.soprasteria.g4it.backend.apiindicator.business.InventorySpreadsheetExportService;
import com.soprasteria.g4it.backend.apiindicator.mapper.ApplicationIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.mapper.IndicatorRestMapper;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentIndicatorBO;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserWorkspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserWorkspaceRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.config.AuthenticationFilter;
import com.soprasteria.g4it.backend.config.JsonNullableConfig;
import com.soprasteria.g4it.backend.config.RestExceptionHandler;
import com.soprasteria.g4it.backend.config.SecurityConfig;
import com.soprasteria.g4it.backend.server.gen.api.InventoryIndicatorApiController;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.nullValue;

/**
 * Exercises the export download through the real SecurityConfig filter chain (JWT resource server),
 * the real AuthenticationFilter/AuthService role resolution, method security and RestExceptionHandler.
 * Only persistence, user lookup and file storage are mocked.
 */
class InventoryIndicatorExportSecurityTest {

    private static final String URL = "/organizations/synthetic-org/workspaces/7/inventories/9/indicators/export/download";
    private static final String VALID_TOKEN = "synthetic-valid-token";
    private static final String SPREADSHEET_URL = URL.replace("/export/download", "/export/spreadsheet/download");

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private UserService userService;
    private InventoryService inventoryService;
    private FileSystemService files;
    private UserWorkspaceRepository userWorkspaces;

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @ImportAutoConfiguration(JacksonAutoConfiguration.class)
    @Import({SecurityConfig.class, AuthenticationFilter.class, AuthService.class, RestExceptionHandler.class,
            InventorySpreadsheetExportService.class,
            InventoryIndicatorApiController.class, InventoryIndicatorController.class, JsonNullableConfig.class})
    static class TestConfig {
        @Bean
        WebMvcConfigurer jsonConverters(JsonMapper mapper) {
            return new WebMvcConfigurer() {
                @Override
                public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                    converters.removeIf(converter -> converter instanceof JacksonJsonHttpMessageConverter);
                    converters.add(new JacksonJsonHttpMessageConverter(mapper));
                }
            };
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                if (!VALID_TOKEN.equals(token)) throw new BadJwtException("invalid synthetic token");
                return Jwt.withTokenValue(token).header("alg", "none").subject("synthetic-sub")
                        .claim("email", "user@example.com").build();
            };
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        // SecurityConfig is @Profile("!test"); the build may activate "test" globally.
        context.getEnvironment().setActiveProfiles("export-security-test");
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("test", Map.of("cors.allowed.origins", "http://localhost")));
        var mocks = List.of(UserService.class, InventoryService.class, DigitalServiceRepository.class,
                DigitalServiceService.class, InventoryIndicatorService.class, TaskRepository.class,
                ApplicationIndicatorMapper.class, FileSystemService.class,
                UserWorkspaceRepository.class, OrganizationRepository.class, RoleService.class);
        // Plain singletons: Spring must not autowire the mocks' own fields.
        context.addBeanFactoryPostProcessor(factory -> {
            mocks.forEach(type -> factory.registerSingleton(type.getSimpleName(), Mockito.mock(type)));
            factory.registerSingleton(IndicatorRestMapper.class.getSimpleName(),
                    Mappers.getMapper(IndicatorRestMapper.class));
        });
        context.register(TestConfig.class);
        context.refresh();

        userService = context.getBean(UserService.class);
        inventoryService = context.getBean(InventoryService.class);
        files = context.getBean(FileSystemService.class);
        userWorkspaces = context.getBean(UserWorkspaceRepository.class);
        when(userService.getUserFromToken(any(Jwt.class))).thenReturn(UserBO.builder()
                .email("user@example.com").sub("synthetic-sub").build());
        when(context.getBean(TaskRepository.class).findByInventoryAndLastCreationDate(any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(42L).build()));
        when(context.getBean(OrganizationRepository.class).findByName("synthetic-org"))
                .thenReturn(Optional.of(Organization.builder().id(3L).build()));
        when(inventoryService.inventoryExists("synthetic-org", 7L, 9L)).thenReturn(true);

        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class),
                        context.getBean(AuthenticationFilter.class))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    private void givenWorkspaceRoles(List<String> roles) {
        var user = UserBO.builder().id(5L).email("user@example.com").sub("synthetic-sub")
                .organizations(List.of(OrganizationBO.builder().id(3L).name("synthetic-org").roles(List.of())
                        .workspaces(List.of(WorkspaceBO.builder().id(7L).name("workspace").roles(roles).build()))
                        .build()))
                .build();
        when(userService.getUserByName(any(UserBO.class))).thenReturn(user);
        when(userWorkspaces.findByWorkspaceIdAndUserId(7L, 5L)).thenReturn(Optional.of(UserWorkspace.builder()
                .workspace(Workspace.builder().name("workspace").build())
                .roles(roles.stream().map(r -> Role.builder().name(r).build()).collect(java.util.stream.Collectors.<Role>toList())).build()));
    }

    private static byte[] syntheticZip() throws Exception {
        var out = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("ind_physical_equipment.csv"));
            zip.write("BYOD,0.57\n".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return out.toByteArray();
    }

    @Test
    void missingTokenIsRejectedWith401() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        verifyNoInteractions(files);
    }

    @Test
    void invalidTokenIsRejectedWith401() throws Exception {
        mvc.perform(get(URL).header("Authorization", "Bearer forged")).andExpect(status().isUnauthorized());
        verifyNoInteractions(files);
    }

    @Test
    void userWithoutInventoryRoleIsRejectedWith403ByMethodSecurity() throws Exception {
        givenWorkspaceRoles(List.of("ROLE_DIGITAL_SERVICE_READ"));
        mvc.perform(get(URL).header("Authorization", "Bearer " + VALID_TOKEN)
                .accept("application/json", "application/zip")).andExpect(status().isForbidden());
        verifyNoInteractions(files);
    }

    @Test
    void inventoryReaderWithoutWriteIsRejectedWith403ByController() throws Exception {
        givenWorkspaceRoles(List.of("ROLE_INVENTORY_READ"));
        mvc.perform(get(URL).header("Authorization", "Bearer " + VALID_TOKEN)
                .accept("application/json", "application/zip")).andExpect(status().isForbidden());
        verifyNoInteractions(files);
    }

    @Test
    void workspaceNotGrantedToUserIsRejectedWith401() throws Exception {
        givenWorkspaceRoles(List.of("ROLE_INVENTORY_WRITE"));
        mvc.perform(get(URL.replace("/workspaces/7/", "/workspaces/8/"))
                .header("Authorization", "Bearer " + VALID_TOKEN)).andExpect(status().isUnauthorized());
        verifyNoInteractions(files);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/export/download", "/export/spreadsheet/download", "/equipments"})
    void inventoryOutsideAuthorizedWorkspaceIsRejectedBeforeDataAccess(String endpoint) throws Exception {
        givenWorkspaceRoles(List.of("ROLE_INVENTORY_WRITE"));
        when(inventoryService.inventoryExists("synthetic-org", 7L, 9L)).thenReturn(false);

        mvc.perform(get(URL.replace("/export/download", endpoint))
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isForbidden());

        verify(inventoryService).inventoryExists("synthetic-org", 7L, 9L);
        verifyNoInteractions(files, context.getBean(TaskRepository.class),
                context.getBean(InventoryIndicatorService.class));
    }

    @Test
    void authorizedWriterReceivesZipWithEvaluatedColumns() throws Exception {
        givenWorkspaceRoles(List.of("ROLE_INVENTORY_WRITE"));
        when(files.downloadFile(eq("synthetic-org"), anyLong(), eq(FileFolder.EXPORT), eq("42.zip")))
                .thenReturn(new ByteArrayInputStream(syntheticZip()));

        var body = mvc.perform(get(URL).header("Authorization", "Bearer " + VALID_TOKEN)
                        .accept("application/zip", "application/json"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (var zip = new ZipInputStream(new ByteArrayInputStream(body))) {
            var entry = zip.getNextEntry();
            assertNotNull(entry);
            assertEquals("ind_physical_equipment.csv", entry.getName());
            assertEquals("BYOD,0.57\n", new String(zip.readAllBytes(), StandardCharsets.UTF_8));
        }
        verify(inventoryService).inventoryExists("synthetic-org", 7L, 9L);
        verify(files).downloadFile("synthetic-org", 7L, FileFolder.EXPORT, "42.zip");
    }

    @Test
    void spreadsheetUsesTheSameExportAuthorizationAndLiteralTextCells() throws Exception {
        givenWorkspaceRoles(List.of("ROLE_INVENTORY_WRITE"));
        var archive = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(archive)) {
            zip.putNextEntry(new ZipEntry("physical_equipment.csv"));
            zip.write("contexteUsage;fractionUsage\n=1+1;0.5000\n".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        when(files.downloadFile("synthetic-org", 7L, FileFolder.EXPORT, "42.zip"))
                .thenReturn(new ByteArrayInputStream(archive.toByteArray()));

        var response = mvc.perform(get(SPREADSHEET_URL)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .accept("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/json"))
                .andExpect(status().isOk()).andReturn().getResponse();

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertEquals("attachment; filename=\"inventory-9.xlsx\"", response.getHeader("Content-Disposition"));
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            var row = workbook.getSheetAt(0).getRow(1);
            assertEquals(CellType.STRING, row.getCell(0).getCellType());
            assertEquals("=1+1", row.getCell(0).getStringCellValue());
            assertEquals("0.5000", row.getCell(1).getStringCellValue());
        }
        verify(inventoryService).inventoryExists("synthetic-org", 7L, 9L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_DIGITAL_SERVICE_READ", "ROLE_INVENTORY_READ"})
    void spreadsheetRejectsMissingExportRolesBeforeStorageAccess(String role) throws Exception {
        givenWorkspaceRoles(List.of(role));

        mvc.perform(get(SPREADSHEET_URL).header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(files);
    }

    @Test
    void spreadsheetRejectsMissingToken() throws Exception {
        mvc.perform(get(SPREADSHEET_URL)).andExpect(status().isUnauthorized());
        verifyNoInteractions(files);
    }

    @Test
    void spreadsheetReturnsAnErrorInsteadOfAnUnsafeFallbackForAnInvalidArchive() throws Exception {
        givenWorkspaceRoles(List.of("ROLE_INVENTORY_WRITE"));
        when(files.downloadFile("synthetic-org", 7L, FileFolder.EXPORT, "42.zip"))
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        mvc.perform(get(SPREADSHEET_URL).header("Authorization", "Bearer " + VALID_TOKEN)
                        .accept("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/json"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        verify(files, times(1)).downloadFile("synthetic-org", 7L, FileFolder.EXPORT, "42.zip");
    }

    @Test
    void equipmentIndicatorsSerializeUsageSharesAsNumbers() throws Exception {
        givenWorkspaceRoles(List.of("ROLE_INVENTORY_READ"));
        when(context.getBean(InventoryIndicatorService.class).getEquipmentIndicators("synthetic-org", 7L, 9L))
                .thenReturn(Map.of("climate", EquipmentIndicatorBO.builder()
                        .label("climate").byodDevicesPercentage(100d / 3).copeDevicesPercentage(0d)
                        .impacts(List.of()).build()));

        mvc.perform(get(URL.replace("/export/download", "/equipments"))
                        .header("Authorization", "Bearer " + VALID_TOKEN).accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.climate.byodDevicesPercentage").value(100d / 3))
                .andExpect(jsonPath("$.climate.copeDevicesPercentage").value(0d));
    }

    @Test
    void equipmentIndicatorsSerializeNoDataAsExplicitNull() throws Exception {
        givenWorkspaceRoles(List.of("ROLE_INVENTORY_READ"));
        when(context.getBean(InventoryIndicatorService.class).getEquipmentIndicators("synthetic-org", 7L, 9L))
                .thenReturn(Map.of("climate", EquipmentIndicatorBO.builder()
                        .label("climate").impacts(List.of()).build()));

        mvc.perform(get(URL.replace("/export/download", "/equipments"))
                        .header("Authorization", "Bearer " + VALID_TOKEN).accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.climate.byodDevicesPercentage").value(nullValue()))
                .andExpect(jsonPath("$.climate.copeDevicesPercentage").value(nullValue()));
    }
}
