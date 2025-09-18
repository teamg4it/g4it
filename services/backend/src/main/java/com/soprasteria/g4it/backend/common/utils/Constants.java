/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public final class Constants {

    /**
     * Human-readable local date time
     */
    public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * Human-readable local date time with ms
     */
    public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    /**
     * File-readable local date time
     */
    public static final DateTimeFormatter FILE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    /**
     * Local date allowed formatters
     */
    public static final List<DateTimeFormatter> LOCAL_DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );
    /**
     * Specific local date when format error
     */
    public static final LocalDate ERROR_DATE_FORMAT = LocalDate.of(1, 1, 1);
    /**
     * The progress percentage after completion.
     */
    public static final String COMPLETE_PROGRESS_PERCENTAGE = "100%";
    /**
     * The progress percentage when started.
     */
    public static final String STARTED_PROGRESS_PERCENTAGE = "0%";
    /**
     * DateTimeFormatter yyyyMMdd_HHmm
     */
    public static final DateTimeFormatter FORMATTER_DATETIME_MINUTE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    /**
     * File url context key.
     */
    public static final String FILE_URL_CONTEXT_KEY = "file.url";
    /**
     * File length context key.
     */
    public static final String FILE_LENGTH_CONTEXT_KEY = "file.length";
    /**
     * Rejected files zip filename
     */
    public static final String REJECTED_FILES_ZIP = "rejected-files.zip";
    /**
     * Jwt email field
     */
    public static final String JWT_EMAIL_FIELD = "email";
    public static final String JWT_FIRST_NAME = "given_name";
    public static final String JWT_LAST_NAME = "family_name";
    public static final String JWT_SUB = "sub";
    public static final String ROLE_INVENTORY_READ = "ROLE_INVENTORY_READ";
    public static final String ROLE_INVENTORY_WRITE = "ROLE_INVENTORY_WRITE";
    public static final String ROLE_DIGITAL_SERVICE_READ = "ROLE_DIGITAL_SERVICE_READ";
    public static final String ROLE_DIGITAL_SERVICE_WRITE = "ROLE_DIGITAL_SERVICE_WRITE";
    public static final String ROLE_ORGANIZATION_ADMINISTRATOR = "ROLE_ORGANIZATION_ADMINISTRATOR";
    public static final String ROLE_WORKSPACE_ADMINISTRATOR = "ROLE_WORKSPACE_ADMINISTRATOR";
    public static final String ROLE_SUPER_ADMINISTRATOR = "ROLE_SUPER_ADMINISTRATOR";
    public static final String ROLE_ECO_MIND_AI_READ = "ROLE_ECO_MIND_AI_READ";
    public static final String ROLE_ECO_MIND_AI_WRITE = "ROLE_ECO_MIND_AI_WRITE";
    public static final String SUPER_ADMIN_EMAIL = "admin@g4it.com";
    public static final List<String> ALL_BASIC_ROLES = List.of(
            ROLE_DIGITAL_SERVICE_WRITE,
            ROLE_DIGITAL_SERVICE_READ,
            ROLE_INVENTORY_WRITE,
            ROLE_INVENTORY_READ,
            ROLE_ECO_MIND_AI_READ,
            ROLE_ECO_MIND_AI_WRITE
    );
    public static final List<String> ORGANIZATION_ROLES = Stream.concat(
                    Stream.of(ROLE_ORGANIZATION_ADMINISTRATOR, ROLE_WORKSPACE_ADMINISTRATOR),
                    ALL_BASIC_ROLES.stream())
            .toList();
    public static final String DEMO = "DEMO";
    public static final Long INTERNAL_WORKSPACE = 0L;
    public static final String INTERNAL_ORGANIZATION = "--INTERNAL-G4IT--";
    public static final List<String> WORKSPACE_ACTIVE_OR_DELETED_STATUS = List.of(
            WorkspaceStatus.ACTIVE.name(),
            WorkspaceStatus.TO_BE_DELETED.name()
    );
    public static final String NOSECURITY = "nosecurity";
    /**
     * BATCH SIZE
     */
    public static final int BATCH_SIZE = 10000;
    /**
     * CSV EXTENSION
     */
    public static final String CSV = ".csv";
    /**
     * ZIP EXTENSION
     */
    public static final String ZIP = ".zip";
    public static final String MANUFACTURING = "MANUFACTURING";
    public static final String USING = "USING";
    public static final String REFERENTIAL_VERSION_CLOUD = "BaseImpact 2011";
    public static final String CLOUD_TYPE = "compute";

    public static final String NETWORK_DATE_PURCHASE = "2020-01-01";

    public static final String NETWORK_DATE_WITHDRAWAL = "2021-01-01";

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}