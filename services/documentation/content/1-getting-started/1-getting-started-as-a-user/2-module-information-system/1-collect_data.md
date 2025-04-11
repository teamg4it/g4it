title: "What data needs to be collected?"
description: "This section explains the kind of data you need to collect before starting an assessment"
weight: 10
---

## Data collected

![Schema that describes the data needs to be collected for inventory impact evaluation: users equipments, network equipment, servers...](../images/data_collected.png)

Four types of data are expected in G4IT:

- **Datacenter**: allows us to take into account the efficiency of a datacenter.
- **Physical Equipment**: this is a key component; every IT system is based on physical equipment.
- **Virtual Equipment**: represents a part of physical equipment in reality and allows us to compute the impact of an application. It can be a Virtual Machine, Pod, part of a hypervisor, or a router...
- **Application**: an application is a sum of Virtual Equipment.

This organization allows each kind of file to be loaded separately, multiple times, and enables the load to be automated by API.

On this page, you can find all the data you will need to evaluate the impact of an inventory. Some data influences the calculation of the impact, while others do not and simply help users to understand or categorize within the GUI.

### **Data Center**

Data that affects the Impact:
- **Location of the datacenter**:
    - The country or region where the data center is located.

- **Power Usage Effectiveness of the datacenter**:
    - PUE measures the energy efficiency of a data center. A lower PUE indicates better efficiency, as it measures how much energy is used by the infrastructure compared to cooling and power distribution.

Data that informs users:
- **Entity**: the name of the entity within the organization responsible for the equipment.

### **Physical Equipment**

Data that affects the Impact:
- **Equipment characteristic**: Description of the physical servers used, including model and performance characteristics (e.g., CPU type, RAM, storage).

- **Quantity**: number of Equipments with the same characteristics.

- **Equipment Lifespan** (if accurate data exists): The expected lifespan of physical Equipments.

- **Energy Consumption** (if accurate data exists): The annual average power usage of the Equipments, measured in kilowatt-hours (kWh).

- **Location**: The country where the Equipment is located.

Data that informs users:
- **Status**: the objective is to know, for example, if the equipment is in use, in stock, in transit...
- **Entity**: the name of the entity within the organization responsible for the equipment.

#### **Specificities for equipment in a datacenter**

Data that affects the Impact:
- **Associated Data center**: The datacenter where the equipment is running.

### **Virtual Equipment**

Virtual Equipment in the G4IT context is used to estimate the impact of an application portfolio.
> NOTE 1:
> Virtual Equipment is more encompassing than a Virtual Machine. It represents everything needed for an application (including a dedicated server. In that latter case, see here).
> When you know the hosted technical architecture.
>
#### **Hosting under control**
In that case, you need to know the underlying physical equipment because G4IT will consider a virtual equipment (a Virtual Machine, a Pod, a hypervisor, a Switch...) as a part of it.
To determine the portion of this virtual equipment, G4IT uses four types of information:
- For compute servers: the portion will be determined as the quantity of vCPUs of this Virtual Machine divided by the sum of vCPUs of all Virtual Equipment linked to the corresponding Physical Equipment.
- For storage servers: the portion will be determined as the storage capacity of this Virtual Machine divided by the sum of storage capacity of all Virtual Equipment linked to the corresponding Physical Equipment.
- If one piece of Virtual Equipment does not have this previous information, the portion will be determined by dividing 1 EqP by the Number of Virtual Equipment.
- In all cases, you can apply your own allocation factor; this will be prioritized over the rest.

Data that informs users:
- **Entity**: the name of the entity within the organization responsible for the equipment.

>NOTE
> 1) A cluster can be considered as a sum of Physical Equipment.
> 2) In a dynamic allocation of resources, such as in a containerization context, it is very hard to define this portion beforehand. It is easier to do it a posteriori using real consumption of resources if tooling is in place (like FinOps or billing).
> 3) In the case of a private cloud, you have to consider as your own resources those allocated to you from the moment they are not available to others, even if they are not used.
> 4) You can consider as Virtual Equipment the part of a laptop used to develop an application.
> 5) A dedicated server for an application must be defined as Virtual Equipment with an allocation factor of 100%.
---
#### **Hosting unknown - Cloud Services (IaaS - Infrastructure as a Service)**

In that case, information on hosting will be approximated based on the following information:

- **Cloud Provider**:
    - The service provider hosting the infrastructure (e.g., Microsoft Azure, Amazon Web Services).

- **Cloud Subscription Information**:
    - **Instance Type**: Specific configuration of the virtual machines (e.g., instance type in AWS, series of virtual machines used in Azure).
    - **Location**: The geographical region where the cloud infrastructure is hosted.

- **Usage of Cloud Instances**:
    - **Operating Time**: The duration for which the cloud instances are actively running.
    - **Average workload**: Percentage of server load.


### **Applicative infrastructure**
An Application in G4IT corresponds to a sum of Virtual Machines (according to the definition given in the previous paragraph).

Data that informs users:
- **Environment type**: makes it possible to distinguish different categories of environments like "production," "integration," "development"...
- **Domain** and **subdomain**: allow grouping applications in the G4IT GUI.

[Detailed documentation about the module](../../../../2-functional-documentation/use_cases/uc_inventory/_index.md)
