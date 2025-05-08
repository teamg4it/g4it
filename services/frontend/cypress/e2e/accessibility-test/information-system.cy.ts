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
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // new inventory
        cy.get('[id="new-inventory"]').click();
        cy.then(() => setPage("Add inventory component"));

        // switch to simulation
        cy.get('[id="simulation-radio-button"]').click();
        cy.get('[id="input-simulation-text"]').type("test-cypress-information-system");

        // load files
        cy.get("[id=file0] input").selectFile(
            "cypress/dataset/input/information-system/datacenter.csv",
            { force: true },
        );
        cy.get("[id=file1] input").selectFile(
            "cypress/dataset/input/information-system/physical_equipment.csv",
            { force: true },
        );
        cy.get("[id=file2] input").selectFile(
            "cypress/dataset/input/information-system/virtual_equipment.csv",
            { force: true },
        );
        cy.get("[id=file3] input").selectFile(
            "cypress/dataset/input/information-system/application.csv",
            { force: true },
        );
        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });

        // add new inventory
        cy.get('[id="add-new-inventory"]').click({ force: true });

        // access to equipment view
        cy.then(() => setPage("Equipment multicriteria page"));
        cy.wait(3000);
        cy.get('[id="launch-estimate"]').click();
        cy.contains("Yes").click();
        cy.wait(15000);
        cy.get('[id="inventory-equipment-button"]').last().click();
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // tabs navigation
        cy.then(() => setPage("Equipment criteria page"));
        cy.get('[id="climate-change"]').click();
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // filter on country
        cy.then(() => setPage("Equipment filter page"));
        cy.get('[id="filter-component"]').click();
        cy.checkA11y(
            undefined,
            {
                rules: {
                    "color-contrast": { enabled: false }, // manually tested it's passed with AA rule
                },
            },
            reportA11yViolations,
            true,
        );
        cy.get('[id="All"]').first().click("left");
        cy.checkA11y(
            undefined,
            {
                rules: {
                    "color-contrast": { enabled: false }, // manually tested it's passed with AA rule
                },
            },
            reportA11yViolations,
            true,
        );

        // return to my information system page
        cy.then(() => setPage("Default Page"));
        cy.get('[id="my-is-return-button"]').click();

        // access to application view
        cy.then(() => setPage("Application multicriteria page"));
        cy.get('[id="inventory-application-button"]').last().click();
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // tabs navigation
        cy.then(() => setPage("Application criteria page"));
        cy.get('[id="climate-change"]').click();
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);
        // filter on domain
        cy.then(() => setPage("Application filter page"));
        cy.get('[id="filter-component"]').click();
        cy.get('[id="All"]').first().click("left");
        cy.checkA11y(
            undefined,
            {
                rules: {
                    "color-contrast": { enabled: false }, //manually tested it's passed with AA rule
                },
            },
            reportA11yViolations,
            true,
        );

        // export inventory
        cy.get('[id="filter-component"]').click();
        cy.get('[id="export-inventory-button"]').click();
        cy.wait(2000).checkA11y(undefined, undefined, reportA11yViolations, true);

        // return to my information system page
        cy.then(() => setPage("Default Page"));
        cy.get('[id="my-is-return-button"]').click();

        // delete test data after cypress testing
        cy.get('[id="delete-inventory"]').click({ force: true });
        cy.contains("Yes").click();
    });
});
