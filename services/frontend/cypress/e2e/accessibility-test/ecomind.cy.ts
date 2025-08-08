import { init, reportA11yViolations, save, setPage } from "../utilsCypress";

describe("Ecomind", () => {
    before(() => {
        cy.visit("/");
        cy.get('[id="eco-mind-ai"]').click();
        window.localStorage.setItem("lang", "en");
        cy.injectAxe();
        init("Ecomind", Cypress.env("mode"));
    });

    after(() => {
        save(Cypress.env("mode"));
    });

    it("Test ecomind", () => {
        cy.then(() => setPage("Default page"));
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);
        // create ai digital service
        cy.log("### Test without change ###");
        cy.get('[id="add-digital"]').click();

        // test calculation failed
        cy.then(() => setPage("Infrastructure page"));
        cy.log("### Calculate failed ###");
        cy.get('[id="calculate"]').click();
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // fill infrastructure fields
        cy.log("### Fill Infrastructure Fields ###");
        cy.get("#cpu-cores").type("4");
        cy.get("#gpu-count").type("4");
        cy.get("#gpu-memory").type("4");
        cy.get("#ram-size").type("4");
        cy.get('[id="pue"]').type("1.5");
        cy.get('[id="complementary-pue"]').type("1.3");
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        // fill ai parameters fields
        cy.then(() => setPage("AI Parameters page"));
        cy.log("### Fill AI Parameters Fields ###");
        cy.get('[inputId="finetuning"]').click();
        cy.get("#nb-user-year").type("4");
        cy.get("#average-request").type("4");
        cy.get("#average-number-token").type("4");
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);

        cy.get('[id="calculate"]').click();
        // calculate
        cy.log("### Visualize ###");
        cy.then(() => setPage("visualize page"));
        cy.checkA11y(undefined, undefined, reportA11yViolations, true);
        // delete the ai digital service
        cy.get('[id="delete-service"]').click();
        cy.get('[aria-label="Yes"]').click();
    });
});
