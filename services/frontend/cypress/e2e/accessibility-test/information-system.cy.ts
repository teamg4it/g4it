import { init, reportA11yViolations, save, setPage } from "../utilsCypress";

describe("Information System", () => {
    before(() => {
        cy.visit("/");
        cy.get('[id="information-system"]').click();
        window.localStorage.setItem("lang", "en");
        cy.injectAxe();
        init("Information System", Cypress.env("mode"));
    });

    after(() => {
        save(Cypress.env("mode"));
    });

    it("Test information system", () => {
        cy.then(() => setPage("Default Page"));
        cy.checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            undefined,
            reportA11yViolations,
            true,
        );

        // new inventory
        cy.get('[id="new-inventory"]').click();
        cy.then(() => setPage("Add inventory component"));

        // switch to simulation
        cy.get('[id="simulation-radio-button"]').click();
        const simulationName = `test-cypress-information-system${Math.random().toFixed(6)}`;
        cy.get('[id="input-simulation-text"]').type(simulationName);

        // Infrastructure files
        cy.get('[id="space-menu-item-1"]').click();
        cy.get('[id="space-menu-item-1"]').should("have.class", "active");
        cy.get("#file0 input[type='file']").selectFile(
            "cypress/dataset/input/information-system/datacenter.csv",
            { force: true },
        );
        cy.get("#file1 input[type='file']").selectFile(
            "cypress/dataset/input/information-system/physical_equipment.csv",
            { force: true },
        );

        // Visit End-user Devices before Application Services
        cy.get('[id="space-menu-item-2"]').click();
        cy.get('[id="space-menu-item-3"]').click();
        cy.get('[id="space-menu-item-3"]').should("have.class", "active");
        cy.get("#file3 input[type='file']").selectFile(
            "cypress/dataset/input/information-system/virtual_equipment.csv",
            { force: true },
        );
        cy.get("#file4 input[type='file']").selectFile(
            "cypress/dataset/input/information-system/application.csv",
            { force: true },
        );
        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(
                {
                    include: [el.get(0)],
                    exclude: [[".p-scrollpanel-bar"]],
                },
                undefined,
                reportA11yViolations,
                true,
            );
        });

        // add new inventory
        cy.get("#add-new-inventory button").last().click({ force: true });

        // access to equipment view
        cy.then(() => setPage("Equipment multicriteria page"));
        cy.wait(3000);
        cy.contains("app-inventory-item", simulationName).within(() => {
            cy.get("#launch-estimate button").click({ force: true });
        });
        cy.contains("Yes").click();
        cy.wait(15000);
        cy.contains("app-inventory-item", simulationName).within(() => {
            cy.get("#inventory-equipment-button button").click();
        });
        cy.checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            {
                rules: {
                    "nested-interactive": { enabled: false }, // manually tested it's passed with AA rule
                },
            },
            reportA11yViolations,
            true,
        );

        // tabs navigation
        cy.then(() => setPage("Equipment criteria page"));
        cy.wait(10000).get('[id="Climate change"]').click();
        cy.wait(1000).checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            undefined,
            reportA11yViolations,
            true,
        );

        // filter on country
        cy.then(() => setPage("Equipment filter page"));
        cy.wait(5000).get("#filter-button button").click();
        cy.checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            {
                rules: {
                    "color-contrast": { enabled: false }, // manually tested it's passed with AA rule
                },
            },
            reportA11yViolations,
            true,
        );
        cy.get('[id="inv-accordion-panel-0"]').click();
        cy.get("#inv-filter-li-0 .p-checkbox-box").first().click({ force: true });
        cy.checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            {
                rules: {
                    "color-contrast": { enabled: false }, // manually tested it's passed with AA rule
                },
            },
            reportA11yViolations,
            true,
        );
        cy.get('[id="close-filter-sidebar-button"]').click();

        // return to my information system page
        cy.then(() => setPage("Default Page"));
        cy.get('[id="my-is-return-button"]').click();

        // access to application view
        cy.then(() => setPage("Application multicriteria page"));
        cy.contains("app-inventory-item", simulationName).within(() => {
            cy.get("#inventory-application-button button").click();
        });
        cy.checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            {
                rules: {
                    "nested-interactive": { enabled: false }, // manually tested it's passed with AA rule
                },
            },
            reportA11yViolations,
            true,
        );

        // tabs navigation
        cy.then(() => setPage("Application criteria page"));
        cy.wait(10000).get('[id="Climate change"]').click();
        cy.wait(1000).checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            undefined,
            reportA11yViolations,
            true,
        );
        // filter on domain
        cy.then(() => setPage("Application filter page"));
        cy.wait(5000).get("#application-filter-button button").click();
        cy.get('[id="app-inv-accordion-panel-0"]').click();
        cy.get("#app-inv-filter-li-0 .p-checkbox-box").first().click({ force: true });
        cy.checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            {
                rules: {
                    "color-contrast": { enabled: false }, //manually tested it's passed with AA rule
                },
            },
            reportA11yViolations,
            true,
        );
        cy.get('[id="close-filter-sidebar-button"]').click();

        // export inventory
        cy.get('[id="export-inventory-button"]').click();
        cy.wait(2000).checkA11y(
            { exclude: [[".p-scrollpanel-bar"]] },
            undefined,
            reportA11yViolations,
            true,
        );

        // return to my information system page
        cy.then(() => setPage("Default Page"));
        cy.get('[id="my-is-return-button"]').click();

        // delete test data after cypress testing
        cy.contains("app-inventory-item", simulationName).within(() => {
            cy.get("#delete-inventory button").click({ force: true });
        });
        cy.contains("Yes").click();
    });
});
