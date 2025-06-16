---
title: "How to Visualize the Impact of My Inventory"
description: "A user guide to understanding and navigating data visualization in G4IT."
weight: 30
---

<!-- TOC -->
  * [Visualize the Impact of Your Inventory](#visualize-the-impact-of-your-inventory)
    * [Step 1: Evaluate the Impact of an Inventory](#step-1-evaluate-the-impact-of-an-inventory)
    * [Step 2: Select Evaluation Criteria](#step-2-select-evaluation-criteria)
    * [Step 3: Visualize the Impact](#step-3-visualize-the-impact)
      * [Step 3.1: Common Components](#step-31-common-components)
      * [Step 3.2: Equipment View Specificities](#step-32-equipment-view-specificities)
      * [Step 3.3: Application Specificities](#step-33-application-specificities)
        * [Navigating in Application View](#navigating-in-application-view)
  * [Functional Documentation](#functional-documentation)
<!-- TOC -->

---

## Visualize the Impact of Your Inventory

### Step 1: Evaluate the Impact of an Inventory

**Actions:**

- *(Optional)* Click the **Settings** button to select evaluation criteria (11 available).
    - See [Step 2](#step-2-select-evaluation-criteria) for details.
- Click **LAUNCH ESTIMATE** or **UPDATE ESTIMATE** to begin evaluation.

![Interface showing 'UPDATE ESTIMATE' and 'Settings' buttons used to start the evaluation process.](../images/Evaluate_the_impact.png)

---

### Step 2: Select Evaluation Criteria

**Actions:**

- Select the criteria you wish to apply to your impact evaluation.
- Use the **Reset to Default** option to revert to your organization’s predefined settings.

![Interface for selecting evaluation criteria, including checkboxes and reset option.](../images/Choose_criteria.png)

---

### Step 3: Visualize the Impact

After completing an evaluation, two visualization options may become available:

1. **Equipment View**
    - Displays impacts of physical and cloud-based equipment.
    - Excludes virtual equipment hosted on owned physical servers (to avoid duplication).

2. **Application View (Beta)**
    - Shows infrastructure that supports applications (e.g., virtual servers, hypervisors, routers).

![Interface displaying buttons for Equipment and Application views.](../images/Equipment_and_Application_button.png)

---

#### Step 3.1: Common Components

Both views share the following components:

- **Notes button**: Access calculation assumptions and contextual notes.
- **Export button**: Download raw data.
- **Filters**: Context-specific tools to refine results.
- **List of Evaluated Criteria**: Tabbed navigation by impact criterion.

![Common interface elements shared across both views.](../images/Common_components.png)

---

#### Step 3.2: Equipment View Specificities

This view is divided into three primary areas:

1. **Main Graph (Left)**
    - Shows impact distribution across lifecycle stages (manufacturing, use, etc.).

2. **Guidance Panel (Top-Right)**
    - Explains scale and visual indicators.

3. **Key Indicators (Bottom-Right)**
    - Highlights key attributes of your inventory.

**Additional Actions:**

- Switch distribution view for alternative analysis.
- Check **Data Consistency** to identify missing or incomplete calculations.  
  Refer to [Data Consistency Documentation](../../../../2-functional-documentation/global_concepts/uc1_dataconsistency.md).

![Screenshot of the Equipment View including graphs and indicators.](../images/Equipment_View_Specificities.png)

---

#### Step 3.3: Application Specificities

This view focuses on virtual infrastructure and their impacts.

1. **Histogram (Left)**
    - Displays criterion-based impact from highest to lowest.

2. **Guidance Panel (Top-Right)**
    - Details graph interpretation.

3. **Key Indicators (Bottom-Right)**
    - Displays key application infrastructure stats.

**Additional Actions:**

- Click the histogram to drill down by domain, subdomain, application, and equipment.
- Check **Data Consistency** as needed.  
  See [Data Consistency Documentation](../../../../2-functional-documentation/global_concepts/uc1_dataconsistency.md).

![Application View with criterion-based impact histogram.](../images/Application_View_Specificities.png)

---

##### Navigating in Application View

During drill-down exploration, two additional graphs are displayed:

1. **Lifecycle Graph**
    - Shows lifecycle stage impacts of underlying physical infrastructure.

2. **Environment Graph**
    - Displays impact based on the declared application deployment environment.

![Lifecycle and environment graphs in Application View.](../images/Navigating_in_Application_View.png)

---

## Functional Documentation

For deeper insights and technical details, consult the  
[Functional Documentation for Inventory Module](../../../../2-functional-documentation/use_cases/uc_digital_services/_index.md).
