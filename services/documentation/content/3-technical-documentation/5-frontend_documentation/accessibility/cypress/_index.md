---
title: "Cypress"
description: "Cypress installation"
date: 2023-12-28T08:20:38+01:00
weight: 30
---

In this section, you will learn how to install and use cypress in local.

### Installation

If you've already installed cypress on your project, you can skip this part.

To install Cypress, you need to install two dependencies in your project: \
``npm install cypress-axe `` and ``npm install axe-core ``
This can take several minutes.

You should also add in tsconfig.json: \
``"types": ["cypress", "cypress-axe"]``

You can then add the launch command to your package.json file to make it easier to launch: \
``"cypress:open": "cypress open",
"cypress:run": "cypress run"``

- The first time you run Cypress, you'll be given a choice of two test types: e2e and component. Choose e2e.
  Cypress will then install the files it needs in your project. They will be located at the root of your project in the
  /cypress folder.
  You'll also find a file at the root of your project ``cypress.config.ts`` to configure cypress. Add to this file to
  make it look like this:

```
export default defineConfig({
    e2e: {
        setupNodeEvents(on, config) {
            // implement node event listeners here
        },
        baseUrl: "http://localhost:4200",
    },
});
```

You can change the port to match your project.

Then add the cypress dependency to /cypress/support/e2e.ts : ``import "cypress-axe";``

### Launch

You can run cypress after launching the front you want to test with the command:
``npm run cypress:run``.\
This will make it possible to run all test scenarios and generate a report accordingly in cypress/report.
Otherwise, the command ``npm run cypress:open`` will launch cypress with a window for more precise identification of
accessibility errors.\
If it's the first time cypress is launch, a window will open allowing you to initialize cypress. To do this, click on
e2e, and cypress will initialize the project accordingly if it hasn't already been done.
You can then select the browser with which you want to test.\
The browser will open and launch the chosen test, displaying any errors identified in the log.

### Test

To add a test, place it in the e2e/accessibility-test folder, and it will be automatically taken into account.

#### Structure

To create a test, start the file with `describe("", () => {})` which includes an entire test suite, and each test or
scenario begins with `it("", () => {})`. We can add `before` or `after` to perform actions before each test,
use `beforeAll` or `afterAll`.\
To interact with page elements, you can use `cy.get()` with an element to identify the html element. The best practice
is to get it using the `id` or an option such as `data-cy` of a html element, so you don't have to depend on the type of
html element and its content.

#### Accessibility

To test the accessibility of the command with cypress, you can use the command `cy.checkA11y();` with 4 parameters
allowing you to limit the element to be tested, exclude certain errors, log and specify whether the test stops if an
error is identified.\
Example of command with options :

```
cy.checkA11y(
    element,
        {
            rules: {
                "heading-order": { enabled: false },
            },
        },
        reportA11yViolations,
    true,
);
```

#### Report

The report is managed by the `utilsCypress.ts`. This file is used to save the test content, avoid duplicates and
generate an html file with a global view of the tests performed.

To save the contents of the tests, they must be retrieved in a `before` with the method `init("")` and save it in
an `after` with a `save()`.\
A `result.json` will be created, containing the information needed to save the test results.
If you wish to run a scenario without creating a report, remember to delete the contents of the json, which will not be
deleted at the end of each test but after the creation of the report.\
The file `create-report.cy.ts` is used to generate the report from the test runs and should therefore be run last.
The report created is in html format and can be viewed in a browser. If another report is created on the same day, the
old report will be overwritten.
