---

title: 'Release v3.13.1'
description: 'Backend modernization with Spring Boot 4, improved CSV import reliability, platform reference data upload fix, and enhanced installation guidance.'
weight: 140

-----------

## Overview

Release v3.13.1 focuses on backend modernization, platform reliability, and developer experience improvements. This version delivers a major technology upgrade by migrating the backend framework to Spring Boot 4, ensuring alignment with the latest Spring ecosystem standards while improving maintainability, compatibility, and long-term supportability. Additionally, this release resolves several user-reported issues affecting CSV imports and platform reference data management. Improvements have also been made to the local installation process and documentation to provide a smoother onboarding experience for developers. Overall, this release strengthens the platform foundation, improves operational reliability, and enhances both user and developer experience.

## Spring Boot 4 Modernization

The platform backend has been upgraded from Spring Boot 3 to Spring Boot 4.

**Improvements**

* Upgrade of the application backend framework to Spring Boot 4
* Modernization of backend dependencies and framework ecosystem
* Improved compatibility with current Java and cloud-native technologies
* Enhanced maintainability and long-term platform support
* Alignment with the latest Spring development standards and best practices

**Benefits**

* Improved platform stability and reliability
* Access to the latest framework enhancements and security updates
* Reduced technical debt through dependency modernization
* Stronger foundation for future feature development
* Better support for future scalability and performance improvements

This modernization ensures the platform remains aligned with current technology standards and supported software versions.

---

## Improved CSV Import Reliability

The CSV import process has been enhanced to improve compatibility and reliability.

**Previous Behavior**

Some valid CSV files were incorrectly rejected during import with a message indicating that the file type was not supported, while the same data could be successfully imported using XLSX format.

**Improvements**

Users can now successfully import supported CSV files without encountering erroneous file type validation errors.

---

## Platform Reference Data Upload Fix

The platform reference data administration workflow has been corrected.

**Previous Behavior**

Administrators could select a platform reference data file in the Administration interface but were unable to complete the upload operation.

**Improvements**

* Restored upload button functionality
* Corrected the file upload workflow
* Improved reliability of platform reference data management operations

Administrators can now successfully upload and update platform reference data files.

## Enhanced Local Installation Experience

The local installation process and documentation have been improved to provide a smoother onboarding experience for developers.

---

## 3.13.1

### Major Changes

- 2172 | Spring Boot 4 migration

### Minor Changes

- 2273 | Fix platform reference data upload issue
- 2261 | Improve CSV file import reliability
- 2210 | Enhance local installation documentation and best practices

---

## Installation Notes





