---
title: "How to visualize the impact of my Inventory ?"
description: "This section is a user guide to understand how to visualize the data in G4IT"
weight: 30
---
<!-- TOC -->
  * [Visualize the Impact of Your Inventory](#visualize-the-impact-of-your-inventory)
    * [**Step 1:** Evaluate the impact of an inventory](#step-1-evaluate-the-impact-of-an-inventory)
    * [**Step 2:** Select Evaluation Criteria](#step-2-select-evaluation-criteria)
    * [**Step 3:** Visualize the Impact](#step-3-visualize-the-impact)
      * [**Step 3.1:** Common Components](#step-31-common-components)
      * [**Step 3.2:** Equipment View Specificities](#step-32-equipment-view-specificities)
      * [**Step 3.3:** Application Specificities](#step-33-application-specificities)
        * [Navigating in Application View](#navigating-in-application-view)
  * [Functional Documentation](#functional-documentation)
<!-- TOC -->

---

## Visualize the Impact of Your Inventory

### **Step 1:** Evaluate the impact of an inventory

- **Action:**
    - [optional] Use the "Settings" button to choose the criteria on which you want to evaluate the impact (11 criteria are available).
      - See Step 2 for details
    - Click the "LAUNCH ESTIMATE" or "UPDATE ESTIMATE" button to start the evaluation.

![Screenshot showing the "UPDATE ESTIMATE" and "Settings" buttons for starting the evaluation process.](../images/Evaluate_the_impact.png)

### **Step 2:** Select Evaluation Criteria

- **Action:**
    - From the list of criteria, select the one you want to evaluate for the impact of your inventory.
    - If you reset to default, it will select the criteria chosen by your organization's administrator.

![Screenshot showing the criteria selection interface with a list of evaluation criteria.](../images/Choose_criteria.png)

### **Step 3:** Visualize the Impact

After calculating the impact, 2 buttons appear giving you access to 2 separate pages:
1. Equipment View appears if at least one item has been calculated in the inventory. The page will show the impact of all equipment in your inventory. It considers all physical equipment and cloud services owned by your organization. Virtual equipment whose physical servers are owned by the organization are not represented in this view so that their impact is not counted twice.
2. Application appears if at least one application has been calculated. The page (in a Beta Version) will focus only on application infrastructure, i.e., all virtual equipment that allows an application to work (dedicated server, virtual server, cloud services, a piece of hypervisor, a router, etc.). 

![Screenshot showing the 2 buttons Equipment and Application](../images/Equipment_and_Application_button.png)

#### **Step 3.1:** Common Components
These 2 views use common components:
1. **Notes button:** allows you to find the calculation assumptions or any other comments useful for interpreting the graphs.
2. **Export button:** raw data can be downloaded at any time using this button.
3. **The filters:** are contextualized according to the view. They can be used to restrict the view to a defined perimeter.
4. **The List of Evaluated Criteria:** Navigate the tab to visualize the impact of your inventory on each criterion.

![Screenshot showing Common component on information system view](../images/Common_components.png)

#### **Step 3.2:** Equipment View Specificities
This page is divided into three parts, with two buttons:
1. On the left, **Main Graph:** Displays the impact distribution selected in the top right of the graph. When you reach the page, you can see the impact of your inventory, broken down according to the stages in the life cycle of the equipment it contains.
2. On the top right, **Guidance:** gives some explanation of the scale used on the graph on the left.
3. On the bottom right, **Key Indicators:** displays the main characteristics of your inventory that influence the impact observed in the left graph.
4. **Action:** change the distribution view in order to make a first analysis of your inventory impact.
5. **Action:** In some cases, G4IT may not have been able to evaluate the impact on all criteria. This button allows you to see inconsistencies in the graph. You can learn more about this in the [Data Consistency](../../../../2-functional-documentation/global_concepts/uc1_dataconsistency.md) documentation.

![Screenshot showing Equipment View Specificities](../images/Equipment_View_Specificities.png)

#### **Step 3.3:** Application Specificities
This page is divided into three parts with two functions:
1. **Histogram:** displays the impact by criterion in descending order from left to right.
2. **Guidance:** gives some explanation of the scale used on the left-hand histogram.
3. On the bottom right, **Key Indicators:** displays the main characteristics of your inventory that influence the impact observed in the left graph.
4. **Action:** click on the graph to discover where the impact comes from (domain, subdomain, application, and at the end for each virtual equipment).
5. **Action:** In some cases, G4IT may not have been able to evaluate the impact on all criteria. This button allows you to see inconsistencies in the graph. You can learn more about this in the [Data Consistency](../../../../2-functional-documentation/global_concepts/uc1_dataconsistency.md) documentation.

![Screenshot showing Application View Specificities](../images/Application_View_Specificities.png)

##### Navigating in Application View
2 new graphs appear along your navigation:
1. **Lifecycle graph**: displays the impact of the physical equipment that enables applications to provide their services, according to the phase in their lifecycle (manufacture, distribution, use, or end of life).
2. **Environment graph**: displays the impact calculated according to the application deployment environment (as declared in the input data).

![Screenshot showing specificities link to the navigation in Application View](../images/Navigating_in_Application_View.png)

---

## Functional Documentation

For detailed information about this module, refer to the
[Functional Documentation for inventory](../../../../2-functional-documentation/use_cases/uc_digital_services/_index.md).
