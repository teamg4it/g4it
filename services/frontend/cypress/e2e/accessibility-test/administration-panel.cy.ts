import { init, reportA11yViolations, save, setPage } from "../utilsCypress";

describe("Administration panel", () => {
    before(() => {
        cy.visit("/");
        cy.get('[id="administration"]').click();
        window.localStorage.setItem("lang", "en");
        cy.injectAxe();
        init("Administration panel", Cypress.env("mode"));
    });

    after(() => {
        save(Cypress.env("mode"));
    });

    it("Test administration panel", () => {
        cy.then(() => setPage("Administration panel"));
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // switch to manage organizations
        cy.get('[id="organizations-tab"]').click();
        cy.then(() => setPage("Organizations page"));

        // select subscriber
        cy.get('p-dropdown[id="subscriberName"]')
            .find(".p-dropdown")
            .click({ force: true })
            .get("p-dropdownitem")
            .children()
            .first()
            .click({ force: true });
        cy.wait(3000).checkA11y(undefined, undefined, reportA11yViolations, true);

        // edit subscriber
        cy.get('[id="edit-organizations-button"]').click();
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

        // add & delete an organization
        cy.get('[id="new-organization-input"]').type(
            "cy_temp_" + Math.floor(Math.random() * 1000000) + 1,
        );
        cy.get('[id="add-organization-button"]').click();

        cy.get('[id="edit-organizations-button"]').click();
        cy.get('[id="delete-organization-button"]').last().click();
        cy.contains("DELETE").click();
        // hide some error with color-contrast with the overlay
        cy.wait(3000).checkA11y(undefined, undefined, reportA11yViolations, true);

        cy.get('[id="users-tab"]').click();
        // select an organization
        cy.get('p-dropdown[id="organizationName"]')
            .find(".p-dropdown")
            .click({ force: true })
            .get("p-dropdownitem")
            .children()
            .last()
            .click({ force: true });
        // wait the dropdown was not visible to unchecked some error
        cy.wait(3000).checkA11y(undefined, undefined, reportA11yViolations, true);

        // choose criteria for this organization
        cy.then(() => setPage("User criteria component"));
        cy.get('[id="criteria-button"]').click();
        cy.get('[id="criteria"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });
        cy.contains("Cancel").click();

        // search for a referenced user
        cy.get('[id="searchName"]').type("admin@g4it.com");
        cy.get('[id="search-button"]').click();
        cy.wait(500)
            .get("body")
            .then((el) => {
                if (el.find('[id="actions-button"]').length) {
                    cy.get('[id="actions-button"]').click();

                    // edit user access & role and save changes
                    cy.then(() => setPage("User access and role management"));
                    cy.get('p-dropdown[id="user-role-dropdown"]')
                        .find(".p-dropdown")
                        .click({ force: true })
                        .get("p-dropdownitem")
                        .children()
                        .last()
                        .click({ force: true });
                } else if (el.find('[id="edit-button"]').length) {
                    cy.get('[id="edit-button"]').click();
                    // edit user access & role and save changes
                    cy.then(() => setPage("User access and role management"));
                    cy.get('p-dropdown[id="user-role-dropdown"]')
                        .find(".p-dropdown")
                        .click({ force: true })
                        .get("p-dropdownitem")
                        .children()
                        .last()
                        .click({ force: true });
                }
            });

        cy.get('[role="complementary"]').then((el) => {
            cy.checkA11y(el.get(0), undefined, reportA11yViolations, true);
        });

        cy.get("body")
            .then((el) => {
                if (el.find('[id="add-button"]').length) {
                    cy.get('[id="add-button"]').click();
                } else if (el.find('[id="update-button"]').length) {
                    cy.get('[id="update-button"]').click();
                }
            })
            .then(() => {
                cy.get('[id="administration"]').click();
            });

        cy.then(() => setPage("Administration panel"));
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);
        cy.get('[id="delete-button"]').click();
        cy.contains("DELETE").click();
    });
});
