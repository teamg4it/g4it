---
title: "3.3.1 Import and Export referential data"
description: "This use case describes how to import and export referential data via CSV files in the administration module."
weight: 10
mermaid: true
---

## Table of contents

- [Description](#description)
- [State Diagram](#state-diagram)
- [Behavior Rules](#behavior-rules)
- [Sequence Diagram](#sequence-diagram)

## Description

As a **G4IT SuperAdmin**, the user can upload CSV files to update the core referential data used for environmental impact calculations, or download the existing data as a CSV file.

**Navigation Path**  
Administration panel / Update Referential

**Access Conditions**  
The connected user must have the `ROLE_SUPER_ADMINISTRATOR`. Organization administrators do not have access to this feature.

## State Diagram

{{< mermaid >}}
stateDiagram-v2
[*] --> Idle
Idle --> SelectingType: Select Data Type
SelectingType --> SelectingFile: Choose CSV File
SelectingType --> Downloading: Click Download
SelectingFile --> Uploading: Click Upload
Uploading --> Processing: File Sent to Server
Processing --> Success: 0 errors
Processing --> Warning: Partial success (>0 lines imported, >0 errors)
Processing --> Error: Technical error or 100% lines failed
Downloading --> Success: File downloaded
Success --> Idle: Clear / New Action
Warning --> Idle: Clear / New Action
Error --> Idle: Retry / New Action
{{< /mermaid >}}

## Behavior Rules

{{% expand title="Show the detail" expanded="false" %}}

| Reference       | Element            | Type   | Description                                                                                                                                     |
| :-------------- | :----------------- | :----- | :---------------------------------------------------------------------------------------------------------------------------------------------- |
| **Dropdown**    | Data Type          | Select | Allows choosing between: Criterion, Lifecycle Step, Hypothesis, Item Type, Matching Item, Item Impact.                                          |
| **FileUpload**  | Choose             | Button | Opens file picker. Filters for `.csv` files. Max size: 100MB.                                                                                   |
| **FileUpload**  | Upload             | Button | Disabled if no file is selected or no data type is chosen. Triggers the POST request.                                                           |
| **Button**      | Download           | Button | Disabled if no data type is chosen. Triggers the GET request to download the existing data.                                                     |
| **ProgressBar** | Upload Progress    | UI     | Displays an indeterminate animation while the server processes the file. It does not reflect real-time transfer progress.                       |
| **Validation**  | Organization Check | Logic  | For specific types (Hypothesis, Item Type, Matching Item, Item Impact), the `subscriber` column in the CSV must match the current organization. |
| **Result**      | Success Message    | UI     | Displays the number of lines successfully imported into the database.                                                                           |
| **Result**      | Error List         | UI     | Displays a scrollable list of errors with line numbers and specific validation messages.                                                        |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor Admin as Administrator
participant Front as G4IT Front-End
participant Back as G4IT Back-End
participant DB as Referential DB
participant Cache as Cache Manager

    rect rgb(240, 240, 240)
    Note over Admin, Cache: Import Flow (POST)
    Admin->>Front: Select Type (e.g., itemImpact)
    Admin->>Front: Select CSV File
    Admin->>Front: Click Upload
    Front->>Back: POST /{backend_url}/referential/itemImpact/csv
    Note over Back: Parse CSV (Apache Commons CSV)
    loop For each record
        Back->>Back: Validate Format & Organization
    end
    alt If valid records found
        Back->>DB: Delete existing organization data (for itemImpact)
        Back->>DB: Save new records
        Back->>Cache: Clear relevant caches (e.g., ref_getItemImpacts)
    end
    Back-->>Front: Return ImportReportRest (importedLines, errors)
    Front-->>Admin: Display status and error details if any
    end

    rect rgb(230, 240, 255)
    Note over Admin, Cache: Export Flow (GET)
    Admin->>Front: Select Type (e.g., hypothesis)
    Admin->>Front: Click Download
    Front->>Back: GET /{backend_url}/referential/hypothesis/csv
    alt Global type (lifecycleStep, criterion)
        Back->>DB: Fetch all records (no organization filter)
    else Organization-scoped type (hypothesis, itemType, matchingItem, itemImpact)
        Back->>DB: Fetch records filtered by organization
    end
    Back->>Back: Generate CSV content (written to temp file, then streamed)
    Back-->>Front: Return CSV stream
    Front-->>Admin: Trigger browser download
    end

{{< /mermaid >}}
