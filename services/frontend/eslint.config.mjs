// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

import eslint from "@eslint/js";
import angular from "angular-eslint";
import tseslint from "typescript-eslint";

export default tseslint.config(
    {
        files: ["**/*.ts"],
        extends: [
            eslint.configs.recommended,
            ...tseslint.configs.recommended,
            ...angular.configs.tsRecommended,
        ],
        processor: angular.processInlineTemplates,
    },
    {
        files: ["**/*.html"],
        extends: [...angular.configs.templateRecommended],
    },
);
