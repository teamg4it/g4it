---
title: "What Data Needs to Be Collected?"
description: "This section explains the kind of data you need to collect before starting an assessment"
weight: 10
---
<!-- TOC -->
  * [Data Collected](#data-collected)
  * [Datacenter](#datacenter)
    * [Data That Affects the Impact](#data-that-affects-the-impact)
    * [Data for Informational Purposes](#data-for-informational-purposes)
  * [Physical Equipment](#physical-equipment)
    * [Data That Affects the Impact](#data-that-affects-the-impact-1)
    * [Data for Informational Purposes](#data-for-informational-purposes-1)
    * [For Equipment in a Datacenter](#for-equipment-in-a-datacenter)
  * [Virtual Equipment](#virtual-equipment)
    * [Hosting Under Control](#hosting-under-control)
      * [Allocation Calculation](#allocation-calculation)
      * [Notes](#notes)
    * [Data for Informational Purposes](#data-for-informational-purposes-2)
    * [Hosting Unknown – Cloud Services (IaaS)](#hosting-unknown--cloud-services-iaas)
  * [Application Infrastructure](#application-infrastructure)
    * [Data for Informational Purposes](#data-for-informational-purposes-3)
<!-- TOC -->

## Data Collected

![Diagram showing types of data needed for inventory impact evaluation, such as user equipment, network equipment, and servers](../images/data_collected.png)

G4IT requires four main types of data:

- **Datacenter**: Provides information about the energy efficiency of the data center.
- **Physical Equipment**: The foundation of every IT system.
- **Virtual Equipment**: Represents logical units or segments of physical infrastructure, essential for application
  impact assessment.
- **Application**: Comprised of one or more virtual resources.

Each data type can be imported independently, multiple times, and supports automation via API.

Some of this data directly affects impact calculations, while other data is used for categorization and user interface
clarity.

---

## Datacenter

### Data That Affects the Impact

- **Location**  
  The country or region where the datacenter is located.

- **Power Usage Effectiveness (PUE)**  
  A measure of energy efficiency. Lower values indicate better performance by comparing total facility power to IT
  equipment power.

### Data for Informational Purposes

- **Entity**  
  The name of the organizational unit responsible for the equipment.

---

## Physical Equipment

### Data That Affects the Impact

- **Equipment Characteristics**  
  Includes server model, CPU type, RAM, storage, etc.

- **Quantity**  
  Number of identical equipment units.

- **Lifespan** (if known)  
  Expected operational life of the equipment.

- **Energy Consumption** (if known)  
  Average yearly energy use in kilowatt-hours (kWh).

- **Location**  
  The country where the equipment is located.

### Data for Informational Purposes

- **Status**  
  Indicates whether the equipment is in use, in storage, in transit, etc.

- **Entity**  
  The responsible organizational unit.

### For Equipment in a Datacenter

- **Associated Datacenter**  
  Specifies the datacenter hosting the equipment.

---

## Virtual Equipment

Virtual Equipment in G4IT includes any virtual or logical infrastructure component necessary for application operation (
e.g., VMs, Pods, containers, routers).

### Hosting Under Control

When the underlying physical equipment is known and managed internally:

#### Allocation Calculation

- **Compute Servers**:  
  Allocation = vCPUs used by this Virtual Equipment ÷ total vCPUs on the physical host.

- **Storage Servers**:  
  Allocation = storage capacity used by this Virtual Equipment ÷ total available on the host.

- **Default Allocation**:  
  If data is missing, assume equal distribution among all Virtual Equipment.

- **Custom Allocation Factor**:  
  User-defined values override automatic calculations.

#### Notes

1. Clusters = Sum of physical resources.
2. In containerized/dynamic environments, allocate based on actual usage when possible.
3. Private cloud resources are considered "owned" once allocated—even if unused.
4. Developer laptops can be modeled as Virtual Equipment.
5. Dedicated application servers should be treated as 100% allocated Virtual Equipment.

### Data for Informational Purposes

- **Entity**  
  The responsible organizational unit.

---

### Hosting Unknown – Cloud Services (IaaS)

When infrastructure details are abstracted (e.g., public cloud services):

- **Cloud Provider**  
  e.g., AWS, Microsoft Azure, Google Cloud.

- **Subscription Details**:
    - **Instance Type**  
      e.g., AWS t3.large, Azure D-series.
    - **Location**  
      Geographical region of the cloud instance.

- **Usage Metrics**:
    - **Operating Time**  
      Duration the instance is running.
    - **Average Workload**  
      Percentage of compute capacity used.

---

## Application Infrastructure

In G4IT, an application is defined as a group of Virtual Equipment.

### Data for Informational Purposes

- **Environment Type**  
  Categorizes environments (e.g., production, development, staging).

- **Domain** and **Subdomain**  
  Helps organize and group applications in the GUI.

---

[Detailed documentation about the module](../../../../2-functional-documentation/use_cases/uc_inventory/_index.md)
