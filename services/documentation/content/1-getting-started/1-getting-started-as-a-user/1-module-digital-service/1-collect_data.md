---
title: "What Data Needs to Be Collected?"
description: "This section explains the kind of data you need to collect before starting an assessment"
weight: 10
---

## Data collected

![Schema that described the data that need to be collected in case digital service impact evaluation : End-user devices, Data Exchange and Infrastructure](../media/data_collected.png)

### End-user Devices

The devices that users use to access my digital service play a key role in performance, usage patterns, and
environmental impact.

-   **Device Configuration**:

    -   Information about the types of devices users employ to access the service (e.g., smartphones, tablets, desktops).
    -   Location of the devices when users access to the digital service.

-   **Annual Usage Time**:

    -   The total time users engage with the service annually, on average.
    -   This metric helps understand the demand for resources, service load, and user habits.

-   **Device Lifetime**:
    -   The average duration users keep their devices before replacing them.
    -   This can help estimate the replacement cycle for hardware in the user base.

---

### Data Exchange

The data exchanged between users and the digital service is an important factor in determining network load and overall
service performance.

-   **Network Type**: The kind of network users rely on to access the service (e.g., fixed-line internet or mobile
    networks).

-   **User Location**: The geographical distribution of users, including the countries or regions where they are located.

-   **Data Volume**:
    -   The total amount of data transmitted over the network each year to support the digital service.
    -   This includes all data exchanges, such as loading pages, data retrieval, uploads, and downloads.

---

### Servers

#### **Physical Servers**

-   **Machine Type**: Description of the physical servers used, including model and performance characteristics (e.g., CPU
    type, RAM, storage).

-   **Server Lifetime**: The expected lifetime of the physical servers before replacement or upgrade.

-   **Energy Consumption**: The average power usage of the servers, measured in kilowatts per hour (kWh).

-   **Server Size**: evaluated in vCPU if it's a compute server or in GB if it's a storage server.

-   **Operating Time**: The amount of time the servers are actively in operation each day or month, impacting wear and
    energy consumption.

#### **Data Center Configuration**

-   **Location**:

    -   The country or region where the data center is located.

-   **PUE (Power Usage Effectiveness)**:
    -   A measure of energy efficiency in the data center. Lower PUE indicates better efficiency, as it measures how much
        energy is used by the infrastructure compared to the cooling and power distribution.

#### **Virtual Machines (VMs)**

-   **Quantity**:The total number of virtual machines utilized in the service infrastructure.

-   **VM Size**: The resource allocation for each virtual machine (e.g., CPU cores, storage).

-   **Operating Time**: The total uptime of virtual machines throughout the year, measured in hours.

---

#### **Cloud Hosting (IaaS - Infrastructure as a Service)**

I use cloud service providers (e.g., AWS, Azure, Scaleway, Google Cloud Platform) for certain infrastructure needs, and here are the key configurations:

-   **Cloud Provider**:

    -   The service provider hosting the infrastructure (e.g., Microsoft Azure, Amazon Web Services, Scaleway, Google Cloud Platform(GCP)).

-   **Cloud Subscription Information**:

    -   **Instance Type**: Specific configuration of the virtual machines (e.g., instance type in AWS, series of virtual
        machines used in Azure, instance type in Scaleway, type/series of machine in GCP).
    -   **Location**: The geographical region where the cloud infrastructure is hosted.

-   **Usage of Cloud Instances**:
    -   **Operating Time**: The duration for which the cloud instances are actively running.
    -   **Average workload**: Percentage of server load.

---

## Scenario used as an example

## Functional documentation

[Detailed documentation about the module](../../../../2-functional-documentation/use_cases/uc_digital_services/_index.md)
