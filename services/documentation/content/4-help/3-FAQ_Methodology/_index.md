---
title: 'FAQ Methodology'
weight: 100
---

## Table of content

<!-- TOC -->
* [Collect](#collect)
* [Calculate](#calculate)
* [Analyse](#analyse)
* [Step-by-step](#step-by-step)
<!-- TOC -->


## Collect

### Retrieve physical equipments characteristics
{{% expand title="How to define physical equipment on which the virtual machine is hosted if i don't know it" expanded="false" %}}
**The Environmental footprint of a virtual machine** is directly linked to the **environmental footprint of the underlying physical equipment** on which the virtual machine is hosted.
**If you don’t know the characteristics of your physical equipment**, we recommend you select the server as :
- **Shared server**, that allocate a portion of the impact assessed for the underlying physical equipment
- **Compute or Storage**, depending on the main object of your server (treatment or storage)
- **Server S, M or L** where the size of server whose vCPU is closest to the sum of the vCPUs of all VMs. If one server is not sufficient, distribute its VMs across several servers.
{{% /expand %}}

{{% expand title="How to know the PUE of my server's datacenter" expanded="false" %}}
You must contact your server host. To learn more about the Power Usage Effectiveness and how it is calculated, check this page related to Power Usage Effectiveness (https://en.wikipedia.org/wiki/Power_usage_effectiveness).
{{% /expand %}}

### Retrieve physical equipments characteristics
{{% expand title="How to retrieve my VM characteristics from Vm Ware" expanded="false" %}}
To retrieve the VM characteristics on vmWare to evaluate the impact of the Digital Service,
Here the step by step to retrieve the information needed and to integrate them in G4IT : [Retrieve Vm Characteristics With vmWare](6-Servers_VmCharacteristicsWithvmWare.md)
{{% /expand %}}

{{% expand title="How to retrieve my azure cloud characteristics with Azure Console or Az CLI" expanded="false" %}}
Here the step by step to retrieve the information needed and to integrate them in G4IT : [Retrieve Azure Cloud Instances characteristics](5-Servers_AzureCloudInstances.md){{% /expand %}}

### Measure Data Exchange on the Network
{{% expand title="How to evaluate Data exchange thank to web analytic tool and Aws CloudWatch" expanded="false" %}}
- AWS CloudWatch monitor your AWS resources and the applications you run on AWS in real time https://docs.aws.amazon.com/cloudwatch/
- Matomo is a web analytics solution, more details on https://matomo.org/
Here the step by step to retrieve the information needed and to integrate them in G4IT : [Retrieve Data Exchanged with AWS CloudWatch & Matomo](4-Data_Exchanged_with_AWS_CloudWatch_Matomo.md)
{{% /expand %}}

### Retrieve User characteristics
{{% expand title="How to evaluate users application activity with matomo, source web analytics application" expanded="false" %}}
Matomo is a web analytics solution you can help you to retrieve the End-users characteristics to evaluate the impact of their devices using my Digital Service.
Here the step by step to retrieve the information needed and to integrate them in G4IT : [User Characteristics with Matomo](../3-FAQ_Methodology/1-User_Characteristics_with_Matomo.md).
{{% /expand %}}

{{% expand title="How to evaluate numbers of users and average connection time with Keycloak" expanded="false" %}}
Keycloak has built-in support to connect to existing LDAP or Active Directory servers you can help you to retrieve the End-users characteristics to evaluate the impact of their devices using my Digital Service.
Here the step by step to retrieve the information needed and to integrate them in G4IT : [User Characteristics with Keycloak](../3-FAQ_Methodology/2-User_Characteristics_with_KeyCloak.md).
{{% /expand %}}

{{% expand title="How to evaluate users application activity with your ELK and your API Manager" expanded="false" %}}
Your ELK and your API Manager can help you to retrieve the End-users characteristics to evaluate the impact of their devices using my Digital Service.
Here the step by step to retrieve the information needed and to integrate them in G4IT : [3-User_Characteristics_with_myELK_myAPIManager.md](3-User_Characteristics_with_myELK_myAPIManager.md)
{{% /expand %}}


## Calculate

### Convert data
{{% expand title="How can I remember my conversion rules" expanded="false" %}}
Use the notes !
{{% /expand %}}

{{% expand title="What are the recommended conversion between Azure Region and G4IT Country" expanded="false" %}}
Here the association proposed between Azure Region and Country:

| Region             | Country        |
|--------------------|----------------|
| westeurope         | Netherlands    |
| eastus             | United States  |
| norwayeast         | Norway         |
| uksouth            | United Kingdom |
| francecentral      | France         |
| northeurope        | Ireland        |
| centralindia       | India          |
| ukwest             | United Kingdom |
| swedencentral      | Sweden         |
| germanywestcentral | Germany        |
| switzerlandnorth   | Switzerland    |

{{% /expand %}}

{{% expand title="How to take into account the automatic shutdown policy of my instances in the estimate of the annual usage time" expanded="false" %}}
The automatic shutdown policy will help to estimate annual usage time:
- Without automatic shutdown, consider 365 (nb days per year) * 24 (up hours per day) =8760 hours
- With automatic shutdown from 7:00PM to 7:00AM, consider 365 (nb days per year) * 12 (up hours per day)= 4380 hours
If your instance is down over the weekend, reduce the number of days per year accordingly.
{{% /expand %}}

### Load data
{{% expand title="How data in G4IT are organized (data model)" expanded="false" %}}
4 kind of data are expected in G4IT
- Datacenter
- Physical Equipments
- Virtual Equipements (*Note* : Virtual Equipement is larger than Virtual Machine. It represents all it is needed for an application include a dedicated server, Cloud Services from AWS or Azure, …)
- Application : an application is a set of Virtual Equipement 

This organization in 4 files permit to load separately each kind of files, several time and permit to automate the load by API.
This following model is transparent for Digital Service Module but essential for Information System Module, because you need in that case to understand the link between all files you will load in G4IT.
{{% /expand %}}

{{% expand title="How to create a Datacenter file" expanded="false" %}}
You have 3 options :
- Start with template empty file loadable on the first panel when you create new inventory !
- Start with an example [Help with pre-filled templates](../../1-getting-started/1-getting-started-as-a-user/2-module-information-system/1-collect_data/01_Help_yourself_with_pre-filled_templates.md)
- Export an inventory for the Demo workspace and modify it with your datas
{{% /expand %}}

{{% expand title="How to create a Physical equipments file" expanded="false" %}}
You have 3 options :
- Start with template empty file loadable on the first panel when you create new inventory !
- Start with an example [Help with pre-filled templates](../../1-getting-started/1-getting-started-as-a-user/2-module-information-system/1-collect_data/01_Help_yourself_with_pre-filled_templates.md)
- Export an inventory for the Demo workspace and modify it with your datas
{{% /expand %}}

{{% expand title="How to create a Virtual equipments file" expanded="false" %}}
You have 3 options :
- Start with template empty file loadable on the first panel when you create new inventory !
- Start with an example [Help with pre-filled templates](../../1-getting-started/1-getting-started-as-a-user/2-module-information-system/1-collect_data/01_Help_yourself_with_pre-filled_templates.md)
- Export an inventory for the Demo workspace and modify it with your datas
{{% /expand %}}

{{% expand title="How to create an Application file" expanded="false" %}}
You have 3 options :
- Start with template empty file loadable on the first panel when you create new inventory !
- Start with an example [Help with pre-filled templates](../../1-getting-started/1-getting-started-as-a-user/2-module-information-system/1-collect_data/01_Help_yourself_with_pre-filled_templates.md)
- Export an inventory for the Demo workspace and modify it with your datas
{{% /expand %}}

{{% expand title="Can I automate the loading of the files " expanded="false" %}}
Yes, every API on G4IT are open ! try to use it !
Information needed are described in 03-Technical documentation
{{% /expand %}}

{{% expand title="How can I automate data loading from my CMDB" expanded="false" %}}
Yes, you can use tools like Power Query or Big Query to transform your data source into G4IT format.
We also have a Python program used in a consulting activity, don't hesitate to ask us to try it.
{{% /expand %}}

{{% expand title="How can I be sure of the content of my inventory" expanded="false" %}}
First, you can verify the number of Datacenters, Equipments, Virtual Equipements and Applications from the ‘My Information System’ main page.
Second, as soon you have launched the calculation, from the equipment view or the application view, the export button is available to download all the file of your inventory and the calculated indicators.
{{% /expand %}}

### Verify and Calculate on selected criteria

{{% expand title="Which criteria are available" expanded="false" %}}
11 criteria can be calculated if the data is available in the engine used in G4IT (NumEcoEval and BoaviztAPI) (See question on data Consistency for more detailed).

Depletion of natural resources (kg eq. Sb)
Non-living resources naturally present in the environment. Among them, the rare earths, the increasing extraction of which causes the release of toxic pollutants, are in limited quantities.​

Climate change (kg eq. CO2)
Global phenomenon of climate change characterized by a general increase in average temperatures, permanently modifying meteorological balances and ecosystems ​

Acidification (mol eq. H+)
Emission of sulfur compounds (mainly due to combustion processes in the production of electricity, heating and transport) generates a global acidification of the oceans and soils which disturbs the ecosystems. ​

Particulate Matter (Disease incidence)
Microscopic particles suspended in the ambient air. They are fine enough to enter the respiratory tract or even the alveoli with a harmful effect on health.

Ionizing radiation (eq. kBq U235) 
Radiation that produces ionizations in the material it passes through. Even in low doses, it can lead to risks of long-term effects like cancer.

Ozone depletion (kg CFC-11 eq.)
Depletion of the stratospheric ozone layer protecting from hazardous ultraviolet radiation.

Photochemical ozone formation (kg NMVOC eq.)
Potential of harmful tropospheric ozone formation ('summer smog') from air emissions.

Euthrophication, terrestrial (mol N eq.)
Eutrophication and potential impact on ecosystems caused by nitrogen and phosphorous emissions mainly due to fertilizers, combustion, sewage systems.

Euthrophication, freshwater (kg P eq.)
Eutrophication and potential impact on ecosystems caused by nitrogen and phosphorous emissions mainly due to fertilizers, combustion, sewage systems.

Euthrophication, marine (kg N eq.)
Eutrophication and potential impact on ecosystems caused by nitrogen and phosphorous emissions mainly due to fertilizers, combustion, sewage systems.

Resource use, fossils (MJ)
Depletion of non-renewable resources and deprivation for future generations.
{{% /expand %}}

{{% expand title="How can i select the calculated criteria" expanded="false" %}}
The criteria to calculate can be configured from 4 places:

- Administration panel / Manage organizations / Visualize organization / configure criteria for one subscriber

- Administration panel / Manage users / Visualize role / configure criteria for one organization

- Digital Services panel / My DS inventory / My Digital Service / configure criteria for my digital service (gears icon on the top right near Calculate button)

- Information System panel / My IS inventory / My Information System / configure criteria for my information system (gears icon on the bottom right on the panel for the IS you want to calculate)

See more details [3.2.5 Choose criteria](../../2-functional-documentation/use_cases/uc_administration/uc_administration_manage_organizations/uc5_choose_criteria.md)
{{% /expand %}}

{{% expand title="What happens if an indicator cannot be calculated" expanded="false" %}}
On the graph, a “data consistency” warning will be displayed.

As we have different calculation engine used in G4IT (NumEcoEval and BoaviztAPI), it’s important to know what we measure to compare things that can be compared.

G4IT needs to be clear about the perimeter evaluated and display the limit of each engine. For different reasons, it may happen that we are unable to produce indicators for one specific criterion or lifecycle step and that impacts the result. G4IT will give a solid evaluation but needs to be clear about the evaluation to compare effectively different items.

G4IT needs to display that it wasn’t able to produce indicators, but that doesn’t mean that there is no impact, only that there is a part of the impact that couldn’t be evaluated (mostly for impact data reason).

Type of error that can happen:

We did an evaluation regarding the criterion “Acidification” but BoaviztAPI doesn’t have data about this criterion.
Lifespan data wasn’t provided
Data is missing in the referential
Other technical reasons
See more details in [Data consistency](../../2-functional-documentation/global_concepts/uc1_dataconsistency.md)
{{% /expand %}}

{{% expand title="What principles are applied for the calculation" expanded="false" %}}
Product Category Rules (PCR) for Information Systems (IS) is a methodological reference framework for the environmental assessment of Information Systems (IS) and provides the method to be followed to calculate the indicators of the environmental labeling of this digital service.

The Product Category Rules (PCR) for Information Systems provides a comprehensive and reliable method for understanding their environmental footprint and the levers for action.

It is part of the European Union's Product Environmental Footprint method and provides the method to be followed for calculating the environmental labeling indicators.

The PCR is based on the LCA framework and defines, in particular:

- The functional unit: "Make available and use the Information System of organization X, by all its users, for one year." »
- Life cycle stages: Manufacturing, distribution, use, end of life
- Impact criteria: Global warming (CO2e), Depletion of abiotic resources, Acidification, Emission of fine particles, Ionizing radiation
- Allocation rules: E.g., BYOD, Cloud, Remote working
- Scope
{{% /expand %}}

### Calculation rules
{{% expand title="Which methodology is used to assess the footprint of a physical equipment" expanded="false" %}}
All information is available in [Physical equipment](../../2-functional-documentation/global_concepts/environmental_footprint_assessment_methodology/uc1_physical_equipment.md)
{{% /expand %}}

{{% expand title="Which methodology is used to assess the footprint of a virtual equipment" expanded="false" %}}
All information is available in [Virtual equipment](../../2-functional-documentation/global_concepts/environmental_footprint_assessment_methodology/uc2_virtual_equipment.md)
{{% /expand %}}

{{% expand title="Which methodology is used to assess the footprint of a cloud equipment" expanded="false" %}}
Boavizta footprint assessment methodology for cloud instances is described in the Boavizta Documentation (https://boavizta.org/en/blog/calculettes-carbone-clouds-providers).
{{% /expand %}}

{{% expand title="Which methodology is used to assess the footprint of an application" expanded="false" %}}
All information is available in [Application](../../2-functional-documentation/global_concepts/environmental_footprint_assessment_methodology/uc3_application.md)
{{% /expand %}}

{{% expand title="Which methodology is used to assess the footprint of exchange on the network" expanded="false" %}}
All information is available in [Network](../../2-functional-documentation/global_concepts/environmental_footprint_assessment_methodology/uc4_network.md)
{{% /expand %}}

## Analyse

{{% expand title="What is the meaning of people eq." expanded="false" %}}
If every human being respected planetary limits, each of us would have an individual package that would guarantee the sustainable balance of the system: The people equivalent (Eq. People) concept represent the number of people respecting this sustainable individual package.

It was performed by dividing planet boundary from each criteria (ref. JRC Technical report - Consumption and Consumer Footprint: methodology and results) by the number of people on Earth.
{{% /expand %}}

{{% expand title="What does the displayed data consistency warning mean" expanded="false" %}}
As we have different calculation engine used in G4IT (NumEcoEval and BoaviztAPI), it’s important to know what we measure to compare things that can be compared.

G4IT needs to be clear about the perimeter evaluated and display the limit of each engine. For different reasons, it may happen that we are unable to produce indicators for one specific criterion or lifecycle step and that impacts the result. G4IT will give a solid evaluation but needs to be clear about the evaluation to compare effectively different items.

G4IT needs to display that it wasn’t able to produce indicators, but that doesn’t mean that there is no impact, only that there is a part of the impact that couldn’t be evaluated (mostly for impact data reason).

Type of error that can happen:

We did an evaluation regarding the criterion “Acidification” but BoaviztAPI doesn’t have data about this criterion.
Lifespan data wasn’t provided
Data is missing in the referential
Other technical reasons
See more details in [Data consistency](../../2-functional-documentation/global_concepts/uc1_dataconsistency.md)
{{% /expand %}}

{{% expand title="Why my results are different than AZURE or AWS carbon cockpit" expanded="false" %}}
In order to better understand Cloud Provider Methododolgy, we invited you to read this note (external link : Understanding the results of cloud providers' carbon calculators | Boavizta) https://boavizta.org/en/blog/calculettes-carbone-clouds-providers
{{% /expand %}}


## Step by step

There are some step by step :
{{% children description="false" %}}
