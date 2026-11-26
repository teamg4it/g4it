<!-- AI_DISCLAIMER v1.0 -->
# Things to know to happily work on this project 🌈

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Getting started

To start the project, follow these steps :

```bash
npm i
npm start
```

Open your browser on `http://localhost:4200/`. The app will automatically reload if you
change any of the source files.

### Backend Configuration

You have two options for working with the backend:

- Keep the entire backend running.
- Or open another terminal and run the command `npm run start:mockserver`. This will use the
mock server defined in the file `mock-server/server`.js to simulate data.

#### Unit tests

```bash
npm test
```

To run unit tests for the entire application and view the code coverage percentage, use
the command: `ng test --code-coverage`.

Feel free to refer to this README file whenever you need guidance while working on the
Angular inventory frontend.

#### Lint

Run `npm run lint` from `services/frontend` to check TypeScript and Angular
templates, including inline templates. GitHub Actions runs the same check as
a blocking job on frontend pull requests and pushes to `main`.

The versioned `eslint-suppressions.json` records existing violations by file,
rule, and count. New violations exceeding a recorded count fail the check.
When fixing existing violations, run `npm run lint -- --prune-suppressions`
and commit the reduced baseline. Until it is pruned, a new violation can
replace a fixed violation of the same rule in the same file without exceeding
the recorded count. Avoid regenerating the entire baseline to hide new
violations.

#### Full Mocked or with backend

To activate mocked mode, launch this command :

```shell
npm run start:mockserver
```

_Note:_

- _This will use the mock server defined in the file `mock-server/server.js` to simulate
    data._

The best way to develop is to have end to end project locally.

### Debugging with Elf

For debugging purposes, you can use the Elf devTool(). Add the following code snippet
somewhere in your code and install
the [Redux DevTools extension](https://chrome.google.com/webstore/detail/redux-devtools/lmhkpmbekcpmknklioeibfkpmmfibljd).

```typescript
import { devTools } from "@ngneat/elf-devtools";
devTools();
```

We use .repository.ts files to set up and manipulate our Elf store that are located
in `app/core/store`

To know more how to work with Elf, see
the [Elf official documentation](https://ngneat.github.io/elf/)

### Text localization

If you need to locate the text, you can find it in the file `assets/i18n/en.json`. It is
already populated with some texts for the inventories page. To use it in HTML, you can use
the following syntax: `{{ 'inventories-footprint.round-button.status' | translate }}`.
A `fr.json` file can be added to the `assets/i18n` folder to automatically translate the
text.

### Styling

To modify the styles, you can make changes in the `styles.scss` file. There is a
variables.scss file where colors and other variables are declared, which can be used in
styles.scss using the syntax `var(--primary-color)`.

To ease your life please use the [PrimeNg](https://primeng.org/) components.
The PrimeNg theme can be changed but be cautious if you modify PrimeNG components directly
in the `assets/themes/primeng-theme.scss` file.

For the charts we use the components
from [Echarts](https://echarts.apache.org/en/index.html)
The echarts theme location is `assets/themes/primeng-theme.scss`.

And finally please check the `assets/images` folder before importing already existing
icons and designs.

---

### Software requirements

List of tools to be installed:

- Node.js 20.19+, 22.13+ or 24+ (CI lint uses Node.js 22)

#### Cypress tests

Run `npm run cypress:open` to execute cypress tests locally one by one.
Run `npm run cypress:run` to execute all tests, then run `start cypress/report/$(date +"%m-%d-%Y")/result.html` to open report (from folder `services/frontend`).

#### End-to-end tests

Run `npm run e2e` to execute the end-to-end tests via a platform of your choice. To use
this command, you need to first add a package that implements end-to-end testing
capabilities.

## Further help

To get more help on the Angular CLI use `ng help` or go check out
the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.

## Env variable injection for urls

For production, we inject url values as env variable in the nginx container, and replace
them thanks to the [nginx/25-inject-env-var.sh](nginx/25-inject-env-var.sh) script.
This script is placed in docker-entrypoint.d folder, and is executed before container
startup.

/!\ You need to inject at least the `$BASE_HREF` env var:

```html
<base href="$BASE_HREF/" />
<!-- Inject /overgreen -->
<base href="/overgreen/" />
```

For instance, if you want to start the image locally:

```sh
podman run -d -p 4200:4200 --name overgreen-frontend -e BASE_HREF=http://localhost:4200
```

And for deployment on k8s:

```sh
BASE_HREF=/overgreen
```
