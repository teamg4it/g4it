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

The referential API provides endpoints for importing CSV data. All import endpoints are `POST` requests and expect a `multipart/form-data` body containing a file.

| Data Type      | Endpoint Path                        |
| -------------- | ------------------------------------ |
| Item Impact    | `/api/referential/itemImpact/csv`    |
| Criterion      | `/api/referential/criterion/csv`     |
| Lifecycle Step | `/api/referential/lifecycleStep/csv` |
| Hypothesis     | `/api/referential/hypothesis/csv`    |
| Item Type      | `/api/referential/itemType/csv`      |
| Matching Item  | `/api/referential/matchingItem/csv`  |

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
2. **Organization Scope**: For organization-specific data, the service ensures that the `subscriber` column in the CSV matches the organization provided in the request.
3. **Persistence**:
    - For some types, the data is appended or merged.
    - For **Item Impact**, the existing data for the organization is **deleted** before importing the new records to ensure consistency.
4. **Caching**: After a successful import, the relevant caches (e.g., `ref_getAllCriteria`, `ref_getItemImpacts`) are cleared to ensure the new data is immediately available for calculations.

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
