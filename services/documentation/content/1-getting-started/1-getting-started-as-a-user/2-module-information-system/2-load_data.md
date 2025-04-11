---
title: "How to load the collected data in G4IT ?"
description: "This section is a user guide to explain where the data need to be loaded in the application"
weight: 20
---

## **Step 1**: Transform your collected data
This step takes place out of G4IT. The aim is to create files understandable by G4IT, so concretely respect the format describe in the data model file.
![screen to see where retrieved the data model](../images/Datamodel_download.png "screen to see where retrieved the data model")

4 type of files can be load in G4IT:  

- **_Datacenter_** : permit to take into account efficiency of a datacenter
- **_Physical Equipment_** : it is a key component : G4IT's objective is to bring digital back to its physical dimension, which is a source of impact.
- **_Virtual Equipment_** : It represents actually a part of Physical Equipment and permit to compute impact of an application. It can be Virtual Machine, Pod, part of an hypervisor or a router...
- **_Application_** : an application is a sum of Virtual Equipment.

You will be able to load each type of file in several stages to complete your inventory
You can also load the same item several times, G4IT will take the last characteristic sent (the distinction is made on the first column on each file). In consequence, be sure of the uniqueness of this data.
You can produce .csv, .xlsx ou .odt files.

To produce this file you can have 3 approach:
- _Manually_ : starting  
  - from scratch with an empty template that you can find in the loading side panel 
  - with an sample file or pre-filled that are available in that page. 
- _Automatically_ : using 
  - an ETL
  - a python program (Beta version on demand)
  - a solution as PowerQuery or BigQuery 

You can find in the following paragraphs Pre Filled and a sample file for each type of files.

### **1.1**: Datacenter file
A Datacenter file is necessary for all physical equipment located in data centres (so mainly servers). It permits for now to take into account the efficiency of this datacenter that is measure with PUE indicator. Power usage effectiveness (PUE) is a ratio that describes how much energy is used by the computing equipment in contrast to cooling and other overhead that supports the equipment.

| type       | Templates                                                |
|------------|----------------------------------------------------------|
| pre-filled | [pre-filled file](../documents/datacenter_toFillIn.xlsx) |
| example    | [Example file](../documents/datacenter_Sample.csv)       |

### **1.2**: Physical Equipment file
A Physical Equipment file is the most important file in G4IT. It often comes from an extract of a CMDB (Configuration management database).  

| type       | Templates                                                       |
|------------|-----------------------------------------------------------------|
| pre-filled | [pre-filled file](../documents/physicalEquipment_toFillIn.xlsx) |
| example    | [Example file](../documents/physicalEquipment_Sample.csv)       |

> Note : you can play with the quantity for a shared equipment (example : for a TV shared with several teams half-time, you can put 0,5 in the quantity to take intro account only the part dedicated to your perimeter)

### **1.3**: Virtual Equipment file

| type       | Templates                                                      |
|------------|----------------------------------------------------------------|
| pre-filled | [pre-filled file](../documents/virtualEquipment_toFillIn.xlsx) |
| example    | [Example file](../documents/virtualEquipment_Sample.csv)       |

### **1.4**: Application file

| type       | Templates                                                   |
|------------|-------------------------------------------------------------|
| pre-filled | [pre-filled file](../documents/application_toFillIn.xlsx)   |
| example    | [Example file](../documents/application_Sample.csv)         |

---

## **Step 2**: Load inventory data in G4IT

### **Step 2.1**: Access the Inventory Module

- **Action**: Click on the earth icon to access the module.

![Screenshot showing the user interface with an icon for accessing the Inventory Module.](../images/01_IS_Module_access.png "Screenshot of the user interface with an icon for accessing the Inventory Module.")

### **Step 2.2**: Creating a New Inventory

Click on the "New Inventory" button to begin creating a new Inventory.

![Screenshot showing the "New Inventory" button highlighted on the interface.](../images/02_create_an_inventory_1.png "Screenshot of the button 'New Inventory' button highlighted on the interface.")

A side panel is opening to put details on your inventory.
![Screenshot showing the inventory creation form on the interface.](../images/02_create_an_inventory_2.png "Screenshot of the New inventory forms")

1) Choose your type of inventory you want to create
    - use "IS version" mode to make an official photography of your inventory. A version is defined by a date (a month and a year)
    - use "simulation" for other use case. A simulation is just defined by a character string.
2) Load your file (not mandatory to create a new inventory). See next paragraph to have more details.
3) Notes that you can Load several .csv, .ods or .xls file, by click on the "add" button.
4) A starter pack provide you template file (empty file without any data) and a data model file details all what is expected by G4IT to do the evaluations.
5) When you have selected at least 1 files, click on "Add" button to create your inventory

After this last click, you will return on the list of inventory with a focus on the one that has just been created.
![Screenshot showing the inventory creation form on the interface.](../images/02_create_an_inventory_2.png "Screenshot of the New inventory forms")

---

## **Step 3**: load your files
At each moment, you can complete your inventory adding new files (screen is quite similar to that one describe in previous paragraph, only with frame 2 to 5).
By default, a kind of each type of file is loadable, but to make a first estimation, you need at least one Physical Equipment.

---

## **Step 4**: control your inventory

At the end of the loading, an icon says the status of your load :

| use case                 | GUI behaviour                                  | Action expected                                                                                                                                                                                                                                                                                                                                                                                           |
|--------------------------|------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| all data are well loaded | ![Loading OK](../images/loadOK.png)            | Nothing !                                                                                                                                                                                                                                                                                                                                                                                                 |
| Some error appears       | ![Load in error](../images/loadInError.png)    | you can view only the line in error by downloaded the error file (click on the icon in the right side). This will automatically download in your browser and .Zip file. If you open it, you will see only line in error (the other lines are well loaded) with a message in the rightmost column to explain what is expected by G4IT. You can correct the line directly in this error file and reload it. |
| something unexpected has happened | ![Loading KO](../images/loadIKO.png)  | probably an error on header, unexpected format, disconnect...       contact you administrator if you don't find the reason.                                                                                                                                                                                                                                                                               |

You can also control the number of item loaded regarding on the left side of the inventory
![Screenshot showing the number of item loaded on the interface.](../images/control_number_of_item.png "Screenshot of inventory control zone")

---

## **Step 5**: Estimate your impact

It is not necessary to have a green icon to do an estimation, just at least one physical equipment.
![Screenshot showing the launch or update estimation button.](../images/update_estimation.png "Screenshot of launch estimation button")

> Note that the calculation uses hardware resources, so use the function sparingly !



---

---

For detailed information about this module, refer to the [Functional Documentation for Information System Module](../../../../2-functional-documentation/use_cases/uc_inventory/_index.md).
