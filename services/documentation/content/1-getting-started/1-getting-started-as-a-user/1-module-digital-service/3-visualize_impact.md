---
title: "How to visualize the impact of my digital service?"
description: "This section is a user guide to understand how to visualize the data in G4IT"
weight: 30
---

<!-- TOC -->
  * [Evaluating the Impact of Your Digital Service](#evaluating-the-impact-of-your-digital-service)
    * [**Step 1:** Begin the Evaluation](#step-1-begin-the-evaluation)
    * [**Step 2:** Select Evaluation Criteria in the footprint view](#step-2-select-evaluation-criteria-in-the-footprint-view)
    * [**Step 3:** Visualize the Overall Impact](#step-3-visualize-the-overall-impact)
  * [Navigating the Graph](#navigating-the-graph)
    * [**Step 4:** View a Specific Evaluation Criterion](#step-4-view-a-specific-evaluation-criterion)
    * [End-user Devices](#end-user-devices)
      * [**Step 5:** View the Impact of End-user Devices](#step-5-view-the-impact-of-end-user-devices)
      * [**Step 6:** View Lifecycle Steps for End-user Devices](#step-6-view-lifecycle-steps-for-end-user-devices)
    * [Data Exchange](#data-exchange)
      * [**Step 7:** View the Impact of Data Exchange](#step-7-view-the-impact-of-data-exchange)
    * [Private Infrastructures](#private-infrastructures)
      * [If You Know Your Infrastructure Configuration](#if-you-know-your-infrastructure-configuration)
        * [**Step 8:** View the Impact of Non-cloud Servers (Infrastructure Known)](#step-8-view-the-impact-of-non-cloud-servers-infrastructure-known)
        * [**Step 9:** View Impact for a Specific Server](#step-9-view-impact-for-a-specific-server)
      * [Cloud Hosting (IaaS - Infrastructure as a Service)](#cloud-hosting-iaas---infrastructure-as-a-service)
      * [**Step 10:** View the Impact of Cloud Services](#step-10-view-the-impact-of-cloud-services)
      * [**Step 11:** View Lifecycle Stages for Cloud Services](#step-11-view-lifecycle-stages-for-cloud-services)
  * [Example Scenario Used](#example-scenario-used)
  * [Functional Documentation](#functional-documentation)
<!-- TOC -->

---

## Evaluating the Impact of Your Digital Service

### **Step 1:** Begin the Evaluation

-   **Action:**
    -   Click the "Calculate My Impact" button to start the evaluation.
    -   The first estimation is done for the default criteria chosen by your workspace administrator.
    -   The "Calculate My Impact" button disappears. Once any equipment is added/modified, the button is re-appears.

![Screenshot showing the "Calculate" and "Settings" buttons for starting the evaluation process.](../media/15_evaluate_the_impact.png)

### **Step 2:** Select Evaluation Criteria in the footprint view

-   **Action:**
    -   Click the 'Edit Criteria' button
    -   From the list of criteria, select the one you want to evaluate for the impact of your digital service.
    -   If you reset to default, it will select the criteria chosen by your workspace administrator.
    -   Click 'Save' button. Re-evaluation launches for the selected criteria.

![Screenshot showing the criteria selection interface with a list of evaluation criteria.](../media/16_choose_criteria.png)

### **Step 3:** Visualize the Overall Impact

After starting the evaluation by clicking the appropriate button, you will be automatically redirected to the page to
visualize the impact of your digital service.

-   This page is divided into three parts with two important buttons:

    1. **The List of Evaluated Criteria:** Navigate this menu to view the impact of your digital service on each
       criterion.
    2. **Main Graph:** Depending on the evaluation level, it displays the impact distribution. You can interact with the
       graph by clicking on different components.
    3. **Guidance:** Based on your current view, this section provides an explanation of the impact and key advice for
       reducing it.
    4. **Action:** In some cases, G4IT may not have been able to evaluate the impact on all criteria. This button enables
       the data consistency view which is disabled by default. You can learn more about this in
       the [Data Consistency](../../../../2-functional-documentation/global_concepts/uc1_dataconsistency.md)
       documentation.
    5. **Action:** This button allows you to see inconsistencies in the graph. If you cannot see this button in the view,
       you can enable it from the 'ENABLE DATA CONSISTENCY' button You can learn more about this in
       the [Data Consistency](../../../../2-functional-documentation/global_concepts/uc1_dataconsistency.md)

    **Action:** Access this view by clicking the "Visualize my results in the" tab in the digital service view.

![Screenshot showing the overall impact of the digital service, divided into different components with the main graph and evaluation criteria list.](../media/17_visualize_impact.png)

---

## Navigating the Graph

### **Step 4:** View a Specific Evaluation Criterion

1. **Selected Criteria**
2. **Action:** Click on the generic graph or select a specific criterion to access a new graph showing the impact
   distribution according to key elements of the digital service:
    - End-user devices
    - Data exchange
    - Infrastructure: - Servers - Cloud services
3. **Guidance:** This view provides explanations about the impact and advice on how to reduce it.

![Screenshot of a graph showing the impact distribution based on selected evaluation criteria. Each section is clickable for further exploration.](../media/18_visualize_impact_criteria.png)

---

### End-user Devices

#### **Step 5:** View the Impact of End-user Devices

1. **Action:** Click on the "Terminals" section of the graph to access a new graph showing the impact distribution based
   on:
    - **Type of end-user devices**
    - **Location**
2. **Guidance:** This view provides explanations about the impact and advice on how to reduce it.

![Screenshot showing the impact distribution based on end-user devices, including type and location.](../media/19_visualize_impact_terminal.png)

#### **Step 6:** View Lifecycle Steps for End-user Devices

1. **Action:** Click on the "Terminals" graph bar to access a new graph showing the impact distribution based on
   lifecycle stages for:
    - Device type
    - Location
2. **Guidance:** This view provides explanations about the impact and advice on how to reduce it.

![Screenshot showing the lifecycle stages of end-user devices and their associated impact distribution.](../media/20_visualize_impact_terminal_lifecycle.png)

---

### Data Exchange

#### **Step 7:** View the Impact of Data Exchange

1. **Action:** Click on the "Networks" section of the graph to access a new graph showing the impact distribution based
   on the type of network and its location. This graph is not interactive.
2. **Guidance:** This view provides explanations about the impact and advice on how to reduce it.

![Screenshot showing the impact of data exchange based on different network types and locations. This section of the graph is non-interactive.](../media/21_visualize_impact_network.png)

---

### Private Infrastructures

#### If You Know Your Infrastructure Configuration

##### **Step 8:** View the Impact of Non-cloud Servers (Infrastructure Known)

1. **Action:** Click on the "Non-cloud servers" section of the graph to access a new graph showing the impact
   distribution according to:
    - **Type of infrastructure:** Dedicated storage, dedicated compute, shared storage, shared compute.
    - For each type, the graph breaks down the impact by **server name**.
2. **Guidance:** This view provides explanations about the impact and advice on how to reduce it.

![Screenshot showing the breakdown of impact based on infrastructure type (dedicated storage, compute, etc.) and server names.](../media/22_visualize_impact_server.png)

##### **Step 9:** View Impact for a Specific Server

1. **Action:** Click on a specific "Server name" in the "Non-cloud servers" graph to access a new graph showing the
   impact distribution based on:
    - **Lifecycle stage**
    - **VM (Virtual Machine)**
2. **Guidance:** This view provides explanations about the impact and advice on how to reduce it.

![Screenshot showing the impact of a specific server based on its lifecycle stage and associated virtual machine (VM).](../media/23_visualize_impact_server_vm.png)

#### Cloud Hosting (IaaS - Infrastructure as a Service)

#### **Step 10:** View the Impact of Cloud Services

1. **Action:** Click on the "Cloud services" section of the graph to access a new graph showing the impact distribution
   according to:
    - **Instance type**
    - **Location**
2. **Guidance:** This view provides explanations about the impact and advice on how to reduce it.

![Screenshot showing the impact distribution of cloud services based on instance type and location.](../media/24_visualize_impact_cloud.png)

#### **Step 11:** View Lifecycle Stages for Cloud Services

1. **Action:** Click on a specific instance type or location in the "Cloud services" graph to access a new graph showing
   the impact distribution based on lifecycle stages.
2. **Guidance:** This view provides explanations about the impact and advice on how to reduce it.

![Screenshot showing the lifecycle stages of cloud services and their impact distribution.](../media/25_visualize_impact_cloud_lifecycle.png)

---

## Example Scenario Used

## Functional Documentation

For detailed information about this module, refer to
the [Functional Documentation for Digital Services](../../../../2-functional-documentation/use_cases/uc_digital_services/_index.md).
