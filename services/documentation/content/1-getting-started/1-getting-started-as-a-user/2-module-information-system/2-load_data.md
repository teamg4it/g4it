---
title: "How to Load the Collected Data into G4IT?"
description: "This section is a user guide explaining where and how to load collected data in the application."
weight: 20
---
<!-- TOC -->
  * [**Step 1**: Prepare Your Collected Data](#step-1-prepare-your-collected-data)
    * [File Preparation Methods](#file-preparation-methods)
    * [**Step 1.1**: Datacenter File](#step-11-datacenter-file)
    * [**Step 1.2**: Physical Equipment File](#step-12-physical-equipment-file)
    * [**Step 1.3**: Virtual Equipment File](#step-13-virtual-equipment-file)
    * [**Step 1.4**: Application File](#step-14-application-file)
  * [**Step 2**: Load Your Data into G4IT](#step-2-load-your-data-into-g4it)
    * [**Step 2.1**: Access the Inventory Module](#step-21-access-the-inventory-module)
    * [**Step 2.2**: Create a New Inventory](#step-22-create-a-new-inventory)
      * [Configure Your Inventory:](#configure-your-inventory)
  * [**Step 3**: Add More Files Anytime](#step-3-add-more-files-anytime)
  * [**Step 4**: Review Your Upload Status](#step-4-review-your-upload-status)
  * [**Step 5**: Estimate the Impact](#step-5-estimate-the-impact)
<!-- TOC -->

## **Step 1**: Prepare Your Collected Data

> This step occurs **outside** G4IT. The goal is to format your data to match G4IT’s expected structure, as described in
> the data model file.

![Screenshot showing where to download the data model file in the interface.](../images/Datamodel_download.png "Interface screenshot for downloading the G4IT data model.")

G4IT accepts four types of data files:

- **Datacenter**: To assess the efficiency of data centers.
- **Physical Equipment**: A critical file—G4IT aims to reconnect digital services to their physical impact.
- **Virtual Equipment**: Represents a portion of physical infrastructure used by applications (VMs, Pods, routers,
  etc.).
- **Application**: Represents a collection of virtual resources.

You can upload these files in multiple stages. If the same item is uploaded multiple times, G4IT will use the most
recently uploaded version (identified by the first column of the file). Ensure each entry is unique.

Supported formats: `.csv`, `.xlsx`, `.odt`

### File Preparation Methods

You can create these files using:

- **Manual Input**:
    - Start from a blank template available in the loading panel.
    - Use the pre-filled or sample files provided below.

- **Automated Processes**:
    - ETL tools (Extract, Transform, Load)
    - Python script (Beta version available on request)
    - Tools like PowerQuery or BigQuery

Below are links to pre-filled templates and sample files for each file type.

---

### **Step 1.1**: Datacenter File

This file is required for physical equipment located in data centers (primarily servers). It enables G4IT to assess data
center efficiency using the PUE (Power Usage Effectiveness) metric.

| File Type  | Templates                                                         |
|------------|-------------------------------------------------------------------|
| Pre-filled | [Download pre-filled file](../documents/datacenter_toFillIn.xlsx) |
| Example    | [Download example file](../documents/datacenter_Sample.csv)       |

---

### **Step 1.2**: Physical Equipment File

This is the most important file in G4IT, typically exported from a CMDB (Configuration Management Database).

| File Type  | Templates                                                                |
|------------|--------------------------------------------------------------------------|
| Pre-filled | [Download pre-filled file](../documents/physicalEquipment_toFillIn.xlsx) |
| Example    | [Download example file](../documents/physicalEquipment_Sample.csv)       |

> Note: For shared equipment, you can use fractional quantities.  
> Example: A TV shared half-time between two teams can be entered with a quantity of `0.5`.

---

### **Step 1.3**: Virtual Equipment File

| File Type  | Templates                                                               |
|------------|-------------------------------------------------------------------------|
| Pre-filled | [Download pre-filled file](../documents/virtualEquipment_toFillIn.xlsx) |
| Example    | [Download example file](../documents/virtualEquipment_Sample.csv)       |

---

### **Step 1.4**: Application File

| File Type  | Templates                                                          |
|------------|--------------------------------------------------------------------|
| Pre-filled | [Download pre-filled file](../documents/application_toFillIn.xlsx) |
| Example    | [Download example file](../documents/application_Sample.csv)       |

---

## **Step 2**: Load Your Data into G4IT

### **Step 2.1**: Access the Inventory Module

- Click the earth icon in the navigation bar to open the Inventory Module.

![Screenshot showing where to access the Inventory Module in the user interface.](../images/01_IS_Module_access.png "Access Inventory Module.")

---

### **Step 2.2**: Create a New Inventory

1. Click **"New Inventory"**.
2. A side panel will open where you can configure your inventory.

![Screenshot of the inventory creation interface with the 'New Inventory' button highlighted.](../images/02_create_an_inventory_1.png "Create a new inventory.")

![Screenshot of the inventory creation form.](../images/02_create_an_inventory_2.png "Inventory configuration panel.")

#### Configure Your Inventory:

1. **Choose the Inventory Type**:
    - **IS Version**: For official snapshots, defined by month and year.
    - **Simulation**: For test cases or scenarios, labeled with a custom string.

2. **Load Your Files** (Optional at creation).

3. **Multiple File Uploads**: Click **"Add"** to include multiple `.csv`, `.ods`, or `.xlsx` files.

4. **Starter Pack**:
    - Includes empty templates and a detailed data model for G4IT compatibility.

5. Once at least one file is selected, click **"Add"** to finalize inventory creation.

Afterward, you'll return to the inventory list, with your newly created inventory highlighted.

---

## **Step 3**: Add More Files Anytime

You can continue uploading files at any time after inventory creation. The upload interface is similar to the one used
during initial creation.

Note: At a minimum, one Physical Equipment file is required to perform an impact estimation.

---

## **Step 4**: Review Your Upload Status

After uploading, G4IT displays an icon to indicate the result:

| Status                       | Icon                                       | Action                                                                                                                 |
|------------------------------|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| All data loaded successfully | ![Green checkmark](../images/loadOK.png)   | No action needed.                                                                                                      |
| Some errors found            | ![Warning icon](../images/loadInError.png) | Click the icon to download a `.zip` file with only the erroneous rows. Fix and re-upload the corrected file.           |
| Unexpected issue             | ![Error icon](../images/loadKOpng.png)     | Likely due to a format issue or disconnection. Check your file headers and format. Contact an administrator if needed. |

You can also verify the number of loaded items from the inventory overview panel.

![Screenshot showing the number of items loaded.](../images/control_number_of_item.png "Number of items loaded in the inventory.")

---

## **Step 5**: Estimate the Impact

At least one valid Physical Equipment file is required to run an estimation.

![Screenshot showing the estimation launch button.](../images/update_estimation.png "Button to launch or update impact estimation.")

Note: The estimation process uses computing resources. Please avoid frequent unnecessary runs.

---

For detailed information, see the [Functional Documentation for the Inventory Module](../../../../2-functional-documentation/use_cases/uc_inventory/_index.md)
