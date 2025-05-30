/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

export abstract class Constants {
    static readonly LANGUAGES = ["en", "fr"];
    static readonly COLOR = [
        "#355c7a",
        "#00baff",
        "#08e4ea",
        "#7aaacf",
        "#4c7c96",
        "#9fb4d4",
        "#0cb9bf",
        "#00a0c4",
        "#375560",
        "#6a8bb5",
        "#06d4da",
        "#00a5cc",
        "#406981",
        "#7f9ecf",
        "#0ae5eb",
        "#00c1e7",
        "#305a74",
        "#688fad",
        "#07dbe3",
        "#00b3cf",
        "#3c637f",
        "#769fc9",
        "#09e5eb",
        "#00bbd1",
        "#435c71",
        "#7eacd4",
        "#08e8ee",
        "#00c5d9",
        "#3e5f79",
        "#7791c4",
    ];
    static readonly CLOUD_SERVICE = "CloudService";
    static readonly TERMINAL = "Terminal";
    static readonly DIGITAL_SERVICES_CHART_ORDER = [
        this.TERMINAL,
        "Network",
        "Server",
        this.CLOUD_SERVICE,
    ];
    static readonly GREEN_COLOR_SET = ["#FFF6B0", "#ADDC6F", "#2FA255", "#006734"];
    static readonly PURPLE_COLOR_SET = [
        "#E8CBFF",
        "#CC8CFF",
        "#9747FF",
        "#6C00FF",
        "#E0A0FF",
        "#B76FFF",
    ];
    static readonly YELLOW_COLOR = "#FFBD00";
    static readonly BLUE_COLOR = "#00B2FF";
    static readonly UNSPECIFIED = "(Unspecified)";
    static readonly GRAPH_RED = "#86251e";
    static readonly GRAPH_GREY = "#6e7079";
    static readonly GRAPH_BLUE = "#00baff";

    static readonly ENDPOINTS = {
        inventories: "inventories",
        digitalServices: "digital-services",
        referential: "referential",
        users: "users",
        subscribers: "administrator/subscribers",
        organizations: "administrator/organizations",
        actions: "administrator-actions",
        version: "version",
        businessHours: "business-hours",
        templateFiles: "template-files",
        workspace: "workspace",
    };

    static readonly ERRORS: any = {
        404: "not-found",
        410: "expired",
    };

    static readonly FILE_TYPES = ["datacenter", "physical", "virtual", "application"];

    static readonly INTEGRATION_BATCH_COMPLETED_FAILED_STATUSES = [
        "COMPLETED",
        "COMPLETED_WITH_ERRORS",
        "FAILED",
        "FAILED_HEADERS",
    ];

    static readonly EVALUATION_BATCH_COMPLETED_FAILED_STATUSES = [
        "COMPLETED",
        "COMPLETED_WITH_ERRORS",
        "FAILED",
    ];

    static readonly EVALUATION_BATCH_RUNNING_STATUSES = [
        "UNKNOWN",
        "STARTED",
        "STARTING",
        "IN_PROGRESS",
    ];

    static readonly EVALUATION_BATCH_STATUS_MAPPING = {
        DATA_EXTRACTION: "data_extraction",
        DATA_EXPOSITION_TO_NUMECOVAL: "data_extraction",
        CALCUL_SUBMISSION_TO_NUMECOVAL: "calculation_submission",
        CALCUL_IN_PROGRESS: "calculation_in_progress",
        AGGREGATION_IN_PROGRESS: "aggregation_in_progress",
        IN_PROGRESS: "calculation_in_progress",
    };

    static readonly INVENTORY_TYPE = {
        INFORMATION_SYSTEM: "INFORMATION_SYSTEM",
        SIMULATION: "SIMULATION",
    };

    static readonly EXPORT_BATCH_GENERATED = "EXPORT_GENERATED";
    static readonly EXPORT_REMOVED = "REMOVED";

    static readonly ORGANIZATION_STATUSES = {
        ACTIVE: "ACTIVE",
        TO_BE_DELETED: "TO_BE_DELETED",
    };

    static readonly CONSTANT_VALUE = {
        NONE: "none",
    };

    static readonly RECIPIENT_MAIL = "support.g4it@soprasteria.com";
    static readonly SUBJECT_MAIL = "Support Request";
    static readonly SPACE = " ";
    static readonly ADMIN = "admin";
    static readonly PEOPLEEQ = "Peopleeq";

    static readonly ACV_STEP = "acvStep";
    static readonly EQUIPMENT_DIMENSIONS = [
        "acvStep",
        "country",
        "entity",
        "equipment",
        "status",
    ];
    static readonly EQUIPMENT_FILTERS = ["country", "entity", "equipment", "status"];
    static readonly EQUIPMENT_FILTERS_MAP: any = {
        country: "countries",
        entity: "entities",
        equipment: "equipments",
        status: "status",
    };
    static readonly APPLICATION_FILTERS = [
        { field: "environment", translated: false },
        { field: "equipmentType", translated: false },
        {
            field: "lifeCycle",
            translated: true,
        },
        {
            field: "domain",
            translated: false,
            children: [
                {
                    field: "subDomain",
                    translated: false,
                },
            ],
        },
    ];
    static readonly APPLICATION_FILTERS_MAP: { [key: string]: string } = {
        environment: "environments",
        equipmentType: "types",
        lifeCycle: "lifeCycles",
        domain: "domains",
    };
    static readonly ALL: string = "All";
    static readonly EMPTY: string = "!Empty"; // ! character is used for sorting Empty on top

    static readonly MUTLI_CRITERIA: string = "multi-criteria";

    static readonly CLOUD_SERVICES: string = "Cloud Services";

    static readonly CHART_RICH = {
        redBold: {
            color: Constants.GRAPH_RED,
            fontWeight: 600,
            fontSize: 15,
        },
        red: {
            color: Constants.GRAPH_RED,
        },
        grey: {
            color: Constants.GRAPH_GREY,
        },
    };

    static readonly DATA_QUALITY_STATUS = {
        ok: "OK",
        err: "ERROR",
        error: "ERREUR",
    };

    static readonly DATA_QUALITY_ERROR = [
        this.DATA_QUALITY_STATUS.err,
        this.DATA_QUALITY_STATUS.error,
    ];

    static readonly TOTAL_VISIBLE_GRAPH_ITEMS = 10;
    static readonly USEFUL_INFORMATION = "useful-information";
    static readonly DECLARATIONS = "declarations";
    static readonly WELCOME_PAGE = "welcome-page";

    static readonly VALID_PAGES = [
        this.USEFUL_INFORMATION,
        this.DECLARATIONS,
        this.WELCOME_PAGE,
    ];
}
