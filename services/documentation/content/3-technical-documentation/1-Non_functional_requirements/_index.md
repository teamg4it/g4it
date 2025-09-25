---
title: 'Non functional requirements'
date:  2025-02-06T14:28:38+01:00
weight: 20
---
### Overview

You will find here all the information about the non-functional requirements of the project as performance, data retention strategy, deployment strategy, etc.

### Open source contribution

G4IT is an open-source project. The aim is to be easy to contribute to the project.
The code base must be well documented and the code must be clean and easy to read in order to ease the contribution of the community.
G4IT need also to have a large non regression test in order to be sure that the code is not broken by the new contribution.

### Ecoconception

The application must be eco-designed and have a low environmental impact.
* Low memory footprint
* Low disk usage on database and file storage
* Autimatic stop during inactivity (night and week-end)
* Heavy task are executed only on demand

### Performance

#### Hypothesis

Here we will base our performance requirements on the following hypothesis:
* We load an inventory of 1000 virtual machines, 1000 physical servers, 1000 network devices, 1000 storage devices, 10000 applications, 1000 cloud services, 1000 digital services.

#### UI response time

A page must load in less than 2 seconds.

So the synchronious backend calls must be less than 1 second.

#### Asynchronous calls response time

##### Load inventory

The inventory must be loaded in less than 1 minute.

##### Estimate inventory

The inventory must be estimated in less than 1 minute.

### Data segregation

The data must be segregated by organisation / workspace
No organisation can access the data of another organisation.
No user workspace can access the data of another workspace.

### High availability and resiliency

* Backend calculations must not impact user experience
* Capacity to scale for a high number of calculations

### Security
* Application are located in private network
* HTTPS SSL v1.2+
* Usage of OIDC for authentication between components
* Features are backend secured, and only accessible to authenticated and authorized users

### Hosting

The G4IT SAAS plateform must be cloud provider-agnostic.
It must be able to be deployed on any cloud provider or on-premise.

#### Resources consumption

The application 

### Accessibility

The application must be accessible to all users.
It must be compatible with screen readers and other assistive technologies.
Some users may have disabilities that require the use of assistive technologies to access the application.
Accessibility best practices must be followed to ensure that the application is usable by all users.
They are described [here](../5-frontend_documentation/accessibility/)
