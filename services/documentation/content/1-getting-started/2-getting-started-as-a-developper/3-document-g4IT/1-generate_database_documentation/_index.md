---
title: "Generate database documentation"
description: "Step by step guide on how to generate documentation for G4IT's data model"
weight: 3
---

## Generate documentation for a table group

1. Make sure the database schema is up to date (pull and run the latest version of the backend app to apply the most recent liquibase migrations)
2. Create a JSON file containing the list of table group to document. Each group should have a name, a description and a
   non-empty list of table names (note that in the documentation, the pages will be displayed in the order the groups were provided in the JSON file).

    **Sample JSON file**:

    ```json
    {
        "tableGroups": [
            {
                "name": "input-tables",
                "description": "These are the input tables",
                "tables": [
                    "in_datacenter",
                    "in_application",
                    "in_physical_equipment",
                    "in_virtual_equipment"
                ]
            },
            {
                "name": "output-tables",
                "description": "These are the output tables",
                "tables": [
                    "out_datacenter",
                    "out_application",
                    "out_physical_equipment",
                    "out_virtual_equipment"
                ]
            }
        ]
    }
    ```

3. Go into `services/documentation/schemacrawler` and run `.\schemacrawler.ps1 -file [filepath]` using _Powershell_ (replace
   `[filepath]` with the path to your JSON file).
4. A timestamped output directory should have been generated. It should contain one folder per table group. Each folder
   contains a Markdown document.
5. The folders can be directly imported into the _Hugo_ documentation.
