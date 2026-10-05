---

title: 'Release v3.13.0'
description: 'Platform modernization with Angular 21, secure Keycloak deployment improvements, enhanced duplication feedback, and improved error messaging.'
weight: 137
-----------

## Overview

Release v3.13.0 focuses on **platform modernization, security, compliance, and user experience improvements**.

This version delivers a major technology upgrade by migrating the frontend framework to **Angular 21** and updating the user interface components to the latest PrimeNG version.

A key enhancement of this release is the modernization of the platform's authentication infrastructure through the migration of **Keycloak** to a deployment model based on officially maintained and security-patched images. This change strengthens the security of the Identity and Access Management (IAM) component, reduces exposure to unpatched vulnerabilities, and improves compliance with internal security and audit requirements while preserving all existing authentication and Single Sign-On (SSO) capabilities.

Additionally, several usability improvements enhance user interactions by providing better feedback during Digital Service duplication operations and clearer error messages when unexpected server-side issues occur.

Overall, this release improves maintainability, security, operational resilience, compliance readiness, and the overall user experience.

---

### Angular 21 & PrimeNG Modernization

The platform frontend has been upgraded from Angular 18 to Angular 21, including the associated PrimeNG migration.

#### Improvements

* Upgrade of the application framework to **Angular 21**
* Migration to the latest supported **PrimeNG** components
* Improved application maintainability and long-term support
* Enhanced compatibility with modern browser capabilities
* Updated frontend dependencies and framework ecosystem

#### Benefits

* Improved platform stability and maintainability
* Better alignment with current Angular best practices
* Access to framework performance improvements and security updates
* Stronger foundation for future feature development

This modernization ensures the platform remains aligned with current technology standards and supported software versions.

---

### Secure Keycloak Deployment

The Keycloak deployment architecture has been modernized to improve security, maintainability, and compliance.

#### Improvements

* Migration away from the previous Bitnami Helm chart deployment
* Adoption of a standard deployment approach using officially maintained Keycloak images
* Explicit version management for deployed images
* Improved security update strategy for Keycloak, Java, and operating system dependencies
* Enhanced operational documentation and deployment practices

#### Security Benefits

* Reduced exposure to unpatched vulnerabilities
* Improved compliance with internal security and audit requirements
* Better long-term maintainability of the Identity and Access Management (IAM) platform
* Stronger security posture for authentication and access control services

#### Functional Continuity

The migration has been designed to ensure:

* No changes to authentication workflows
* Preservation of existing realms, users, and configurations
* Continued support for Single Sign-On (SSO) integrations
* Uninterrupted token management and application integrations

Users can continue to authenticate and access the platform without any functional changes.

---

### Improved Digital Service Duplication Experience

The duplication workflow for Digital Services has been enhanced to provide clearer feedback during processing.

#### Previous Behavior

When duplicating a Digital Service version, no loading indication was displayed while the operation was being processed.

This could lead users to click the duplication action multiple times, unintentionally creating several duplicate versions.

#### Improvements

* Loading indicator displayed immediately after initiating duplication
* User actions temporarily disabled while duplication is in progress
* Automatic opening of the newly created draft version once available
* Improved feedback during asynchronous processing

These changes provide a clearer and more predictable user experience while preventing accidental duplicate creations.

---

### User-Friendly Error Messages

Error handling has been improved across the platform to provide clearer feedback when unexpected server errors occur.

#### Improvements

* Replacement of generic **"Unknown Error"** messages
* Introduction of user-friendly alert messages
* Improved communication when internal server errors occur
* Better guidance for users encountering unexpected issues

This enhancement makes troubleshooting easier and improves the overall user experience.

---

## 3.13.0

### Major Changes

- 2180 | Angular 21 upgrade and PrimeNG migration
- 2168 | Secure Keycloak deployment using officially maintained images and Helm chart

### Minor Changes

- 2188 | Digital Service version duplication now displays loading state during processing
- 2203 | Improved user-friendly error messages for internal server errors

---

## Installation Notes


