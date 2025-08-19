---
title: "2.3.4.2. Add Cloud Services by importing files"
description: "This use case describes how to upload files to add Cloud Service equipments to a digital service"
weight: 30
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Import Cloud Services import](#import-cloud-services)

## Description

This use case allows a **project team** to upload Cloud Services equipments by importing the files, into a digital service previously created.
The file types are **datacenter**, **physical equipment** and **virtual equipment**.

The files should be in `.csv` or `.xlsx` format. For **CSV files** specifically, both commas and semicolons are
supported as delimiters. Regarding encoding formats, **UTF-8**, **UTF-8 with BOM**, and **Windows-1252** are supported;
other encoding formats, while untested, may also work.

**Navigation Path**

-   My Digital Services / My Digital Service / Import button  / Import Cloud Servers

## Import Cloud Services
![uc3_add_visualize_equipments_CloudServiceImport.png](../../../images/uc3_add_visualize_equipments_CloudServiceImport.png)


{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group              | Elements                   | Type   | Description                                                               |
|-----------|--------------------|----------------------------|--------|---------------------------------------------------------------------------|
| 1         | Cloud service form |         | Form   | <li><u>_initialization rules_</u>: That form enables the import of files. |
| 2         |                    | Virtual equipment template | button | action rules: To help you start, template file is available to be downloaded.     |
| 3         |                    | Browse file                | button                 | action rules: Click on it to open a window to select one file on the user computer.                                                                                                                                                                                                                                                                                                                                       |
| 4         |                    | Start Upload               | button                 | action rules: Click the upload button to start the loading of attached file.                                                                                                                                                                                                                                                                                                                                              |
| 5         | Loading history    |                | label                  |                                                                                                                                                                                                                                                                                                                                                                                                                           |
|           |                    | Status icon                | label                  | <li><u>_initialization rules_</u>: 5 existing types: Pending (If any other digital service loading or estimation task is in progress), In Progress (Loading in progress), FAILED/Error (Loading could not be performed), Completed with errors (Loading is completed with errors) and Completed (Loading is completed).                                                                                                   |
|           |                    | Dates/ Time                | label                  |                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 6         |                    | Rejected file Download     | button                 | <li><u>_initialization rules_</u>: The button is displayed in case of "Completed with errors" which means some items could not be loaded on the digital service or in case of a 'Failed' task. Button with 'Completed with errors' trigger the download of a file containing the items in error. Items can be then corrected in the file and uploaded later. <br> Button with a 'Failed' task displays the error details. |
| 7         |                    | Previous                   | button                 | action rules: Click to go on the non- cloud equipments import.                                                                                                                                                                                                                                                                                                                                                            |                                                                                                                          |
| 8         |                    | Finish                     | button                 | action rules: To close the import form and go to the digital service view.                                                                                                                                                                                                                                                                                                                                                |                                                                                                            |

{{% /expand %}}

## Rules to upload the files
***Virtual equipment file***
| Field Name               | Value Pattern / Possible Values      | PK? | Consistency / Reference                                                                                     | Mandatory? | Specific Rule          |
|--------------------------|--------------------------------------|-----|------------------------------------------------------------------------------------------------------------|------------|------------------------|
| nomEquipementVirtuel     | ^[A-Za-z0-9_-]+$                     | Yes | N/A                                                                                                        | Yes        |                        |
| typeInfrastructure       | ***CLOUD_SERVICES***                 | No  | Only one value                                                                                             | Yes        |                        |
| quantite                 | ^\d+$                                | No  | N/A                                                                                                        | Yes        |                        |
| provider                 |                                      | No  | Consistent with the list of cloud provider available in BoaviztAPI                                         | Yes        |                        |
| typeInstance             |                                      | No  | Consistent with the list of instances available in BoaviztAPI                                              | Yes        |                        |
| location                 |                                      | No  | Consistent with the list of locations available in BoaviztAPI                                              | Yes        |                        |
| chargeMoy                | ^(\d+)?(\.\d+)?$                     | No  | N/A                                                                                                        | Yes        |                        |
| dureeUtilisationAnnuelle | ^(\d+)?(\.\d+)?$                     | No  | N/A                                                                                                        | Yes        |                        |


## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as project team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Import" button in digital service view
front ->> back: GET /subscribers/{subscriber}/organizations/{organization}/template-files
back -->> front: Return the information of template files
RND ->> front: Click on the template to download.
front ->> back: GET /subscribers/{subscriber}/organizations/{organization}/template-files/{name}
back -->> front: Template file downloaded in user's local machine
RND ->> front: Click on "Start Upload" button in the loading files view
front ->> back: POST /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/load-input-files
front -->> RND : Display the 'pending' button if another task is in progress
front -->> back: Resume loading once no other task is in progress
back -->> DataBase: Validate and load virtual equipment in the in_virtual_equipment table
back -->> front: Update the loading history
front ->> back: GET /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}
back-->> front: Get the updated digital service
back ->> front: Display the updated loading history
front ->> RND : Display the 'completed' button if all the uploaded data is correct
front -->> RND : Display the 'failed' button if most of the uploaded data is incorrect or <br> mandatory headers missing
front ->> RND : Display the 'completed with errors' and 'download' button if some of the uploaded data is incorrect
RND ->> front : Click the 'completed with errors' button to download the rejected data
front -->> back: GET /subscribers/{subscriber}/organizations/{organization}/download-reject/{taskId}
back ->> front : Rejected files downloaded to the user's local machine

{{< /mermaid >}}
