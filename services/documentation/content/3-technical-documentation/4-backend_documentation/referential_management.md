---
title: "Referential Management"
description: "Technical details about the referential data import process."
weight: 30
---

The referential management system handles the import and export of core data used by the G4IT calculation engine.

## CSV Import Architecture

The import process follows a standard flow:

1. **Frontend**: `UpdateReferenceComponent` uses PrimeNG `FileUpload` to send a CSV file to the backend.
2. **Controller**: `ReferentialImportExportController` receives the multipart file and the data type.
3. **Service**: `ReferentialImportService` parses the CSV and maps it to the database.

## API Endpoints

The referential API provides endpoints for importing and exporting CSV data. All endpoints use the same path structure but different HTTP methods.

| Data Type      | Endpoint Path                                 | Methods       |
| -------------- | --------------------------------------------- | ------------- |
| Item Impact    | `{backend_url}/referential/itemImpact/csv`    | `GET`, `POST` |
| Criterion      | `{backend_url}/referential/criterion/csv`     | `GET`, `POST` |
| Lifecycle Step | `{backend_url}/referential/lifecycleStep/csv` | `GET`, `POST` |
| Hypothesis     | `{backend_url}/referential/hypothesis/csv`    | `GET`, `POST` |
| Item Type      | `{backend_url}/referential/itemType/csv`      | `GET`, `POST` |
| Matching Item  | `{backend_url}/referential/matchingItem/csv`  | `GET`, `POST` |

- **POST**: Used to import data. Expects a `multipart/form-data` body with a field named `file` containing the CSV. An optional `organization` query parameter is required for organization-scoped types (`hypothesis`, `itemType`, `matchingItem`, `itemImpact`).
- **GET**: Used to export (download) the current referential data. Returns a CSV file stream. An optional `organization` query parameter is required for organization-scoped types. Global types (`lifecycleStep`, `criterion`) do not require it.

## Business Logic: `ReferentialImportService`

The `ReferentialImportService` is the core component for processing CSV files. It uses **Apache Commons CSV** for parsing.

### CSV Format

The parser is configured with the following settings:

- **Format**: `RFC4180`
- **Delimiter**: Semicolon (`;`) (Defined in `CsvUtils.DELIMITER`)
- **Headers**: Expected on the first line.
- **Trimming**: whitespace is trimmed from values.

### Processing Flow

1. **Validation**: Each line is parsed and mapped to a REST DTO using `ReferentialMapper`. Validation constraints (JSR 303/380) are then checked using a `Validator`.
2. **Organization Scope**: Some types are **global** (`lifecycleStep`, `criterion`) and do not use an organization. Others are **organization-scoped** (`hypothesis`, `itemType`, `matchingItem`, `itemImpact`): the service validates that every row's `subscriber` column matches the `organization` parameter provided in the request.
3. **Persistence**:
    - For some types, the data is appended or merged.
    - For **Item Impact**, the existing data for the organization is **deleted** before importing the new records to ensure consistency.
4. **Caching**: After a successful import, the relevant cache is cleared per data type (e.g., `ref_getAllCriteria`, `ref_getItemImpacts`). For `itemImpact`, two caches are cleared: `ref_getItemImpacts` and `ref_getCountries`.

## CSV Schema Definitions

The source of truth for CSV headers and required fields is the `csv-headers.yml` file located in `services/backend/src/main/resources/`.

```yaml
# Example from csv-headers.yml
referential:
    itemImpact:
        - name: "subscriber"
        - name: "item_type"
        - name: "criterion"
        - name: "lifecycle_step"
        - name: "unit"
        - name: "value"
```

The `ReferentialMapper` uses these definitions to map CSV columns to the internal data model.
