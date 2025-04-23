## Context

This folder is used to generate the documentation for a set of database tables.
For each table group, a markdown document is generated.

## Requirements

-   The database must be up and running
-   You must have installed docker image `schemacrawler/schemacrawler`

## Properties setup

The database connection properties are stored in `db.properties.json`.

## Usage

Open a Powershell terminal and run `.\schemacrawler.ps1 -file [file]` where `[file]` is a JSON file containing a list of table groups.

_Example input file_:

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

_Example usage_:

`.\schemacrawler.ps1 -file input.json`

## Output

A timestamped output folder is created with the following structure (see example above):

```
out/
└── yyyyMMdd_HHmmss/
    └── _index.md
```

## Other scripts

-   `markdown.py` runs inside the schemacrawler virtual machine and generates the output markdown description for each group
