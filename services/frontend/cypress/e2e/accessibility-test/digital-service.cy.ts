import { init, reportA11yViolations, save, setPage } from "../utilsCypress";

describe("Digital Service", () => {
    before(() => {
        cy.visit("/");
        cy.get('[id="digital-services"]').click();
        window.localStorage.setItem("lang", "en");
        cy.injectAxe();
        init("Digital Service", Cypress.env("mode"));
    });

    after(() => {
        save(Cypress.env("mode"));
    });

    it("Test digital service", () => {
        cy.then(() => setPage("Default page"));
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);
        // create digital service
        cy.log("### Test without change ###");
        cy.get('[id="add-digital"]').click();
        const dsName = `cy-digital-service ${Math.random().toFixed(2)}`;
        cy.wait(200).get('[id="dsName-input"]').clear().type(dsName);
        cy.wait(2000).get('[id="validate-creation-button"]').click();

        // add a note
        cy.then(() => setPage("Note component"));
        cy.log("### Note ###");
        cy.get('[id="add-note"]').click();
        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });
        cy.get("p-editor").type("test");
        cy.get('[id="save-note"]').click();

        // add a terminal
        cy.log("### Terminals ###");
        cy.get('[id="add-terminals"]').click();
        // test the sidebar
        cy.then(() => setPage("Add terminal component"));
        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });
        cy.get('[id="submit-terminals"]').click();
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // add a networks
        cy.log("### Networks ###");
        cy.get('[id="add-networks"]').click();
        // test the sidebar
        cy.then(() => setPage("Add network component"));
        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });
        cy.get('[id="submit-networks"]').click();
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // add a servers
        cy.log("### Servers ###");
        cy.get('[id="add-servers"]').click();
        // test the sidebar
        cy.then(() => setPage("Add server previous component"));
        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });
        cy.get('[id="next-servers"]').click();
        // test the next sidebar
        cy.then(() => setPage("Add server next component"));
        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });
        cy.get('p-dropdown[formControlName="host"]')
            .find(".p-dropdown")
            .click({ force: true })
            .get("p-dropdownitem")
            .children()
            .first()
            .click({ force: true });
        cy.get('[id="vcpu"]').type("1");
        cy.get('[inputId="minmaxfraction"]').type("1");
        cy.get('[id="submit-servers"]').click();

        // cloud
        cy.log("### Cloud Servers ###");
        cy.get('[id="add-cloud"]').click();
        // test the sidebar
        cy.then(() => setPage("Add cloud component"));
        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });
        cy.get('[id="cloud-cancel"]').click();

        cy.get('[id="calculate"]').click();

        // calculate
        cy.log("### Visualize ###");
        cy.wait(2000).get('[ng-reflect-impact="climate-change"]').click();
        cy.then(() => setPage("visualize page"));
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // test criteria button
        cy.then(() => setPage("Criteria component"));
        cy.log("### Criteria ###");
        cy.get('[id="criteria-button"]').click();
        cy.get('[id="criteria"]').then((el) => {
            cy.checkA11y(
                el.get(0),
                {
                    rules: {
                        "heading-order": { enabled: false },
                    },
                },
                reportA11yViolations,
                true,
            );
        });
        cy.get('[id="criteria-cancel"]').click();
        // manage version page
        cy.wait(2000).get('[id="manage-versions"]').click();
        cy.wait(2000).get('[id="return-button"]').click();
        // delete the digital service
        cy.get('[id="return-button"]').click();

        cy.contains("p-card", dsName).within(() => {
            cy.get('[id="delete-digital-service"]').click();
        });
        cy.get('[aria-label="Yes"]').click();
    });
});
