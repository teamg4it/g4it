---
title: "2.3.2.2. Add Network services by importing files"
description: "This use case describes how to upload files to add network to a digital service"
weight: 30
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Import Networks](#import-networks)

## Description

This use case allows a **project team** to upload Network equipments by importing the files, into a digital service version previously created.
The file type is **physical equipment**.

The files should be in `.csv` or `.xlsx` format. For **CSV files** specifically, both commas and semicolons are
supported as delimiters. Regarding encoding formats, **UTF-8**, **UTF-8 with BOM**, and **Windows-1252** are supported;
other encoding formats, while untested, may also work.

**Navigation Path**

-   My Digital Services / Digital Service Version view / Import button  / Import Networks
-   My Digital Services / Digital Service Version view / Networks / Modify Networks

## Import Networks
![uc3_add_visualize_equipments_networksImport.png](../../../images/uc3_add_visualize_equipments_networksImport.png)


{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group           | Elements                                  | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                                               |
|-----------|-----------------|-------------------------------------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1         | Networks form   |                                           | Form   | <li><u>_initialization rules_</u>: That form enables the import of files.                                                                                                                                                                                                                                                                                                                                                 |
| 2         |                 | physical equipment template               | button | action rules: To help you start, template files are available to be downloaded.                                                                                                                                                                                                                                                                                                                                           |
| 3         |                 | Browse file                               | button                 | action rules: Click on it to open a window to select one file on the user computer.                                                                                                                                                                                                                                                                                                                                       |
| 4         |                 | Start Upload                              | button                 | action rules: Click the upload button to start the loading of attached file.                                                                                                                                                                                                                                                                                                                                              |
| 5         | Loading history |                                           | label                  |                                                                                                                                                                                                                                                                                                                                                                                                                           |
|           |                 | Status icon                               | label                  | <li><u>_initialization rules_</u>: 5 existing types: Pending (If any other digital service loading or estimation task is in progress), In Progress (Loading in progress), FAILED/Error (Loading could not be performed), Completed with errors (Loading is completed with errors) and Completed (Loading is completed).                                                                                                   |
|           |                 | Dates/ Time                               | label                  |                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 6         |                 | Rejected file Download                    | button                 | <li><u>_initialization rules_</u>: The button is displayed in case of "Completed with errors" which means some items could not be loaded on the digital service or in case of a 'Failed' task. Button with 'Completed with errors' trigger the download of a file containing the items in error. Items can be then corrected in the file and uploaded later. <br> Button with a 'Failed' task displays the error details. |
| 7         |                 | Finish                                    | button                 | action rules: To close the import form and go to the digital service view.                                                                                                                                                                                                                                                                                                                                                |                                                                                                            |
| 8         |                 | Next                                      | button                 | action rules: Navigate to the cloud equipment import form.                                                                                                                                                                                                                                                                                                                                                                |                                                                                                                          |

{{% /expand %}}

## Rules to upload the files
***Physical equipment file***
| Field Name            | Value Pattern / Possible Values              | PK? | Consistency / Reference                                    | Mandatory?                        | Specific Rule / Comment                                        |
|-----------------------|----------------------------------------------|-----|------------------------------------------------------------|-----------------------------------|----------------------------------------------------------------|
| nomEquipementPhysique | ^[A-Za-z0-9_-]+$                             | Yes | N/A                                                        | Yes                               |                                                                |
| nomEntite             | ^[A-Za-z0-9_-]+$                             | No  | N/A                                                        | No                                |                                                                |
| nomSourceDonnee       | ^[A-Za-z0-9_-]+$                             | No  | N/A                                                        | No                                |                                                                |
| modele                | (ref_network_type.reference)                 | No  | N/A                                                        | Yes                               |                                                                |
| quantite              | ^\d+$                                        | No  | N/A                                                        | Yes                               |                                                                |
| type                  | ***Network***                                | No  | N/A                                                        | Yes                               | Only 1 value possible                                          |
| statut                | ^[A-Za-z0-9_-]+$                             | No  | N/A                                                        | No                                |                                                                |
| paysDUlisation        | ^[A-Za-z0-9_-]+$                             | No  | Consistent with G4IT countries list                        | Yes                               |                                                                |
| utilisateur           | ^[A-Za-z0-9_-]+$                             | No  | N/A                                                        | No                                |                                                                |
| dateAchat             | Always '2020-01-01'                          | No  | N/A                                                        | No                                | Set to '2020-01-01' automatically                             |
| dateRetrait           | Always '2020-01-01'                          | No  | N/A                                                        | No                                | Set to '2020-01-01' automatically                             |
| nomCourtDatacenter    | ^[A-Za-z0-9_-]+$                             | No  | Must reference an existing datacenter                      | No                                | Should be associated to an existing datacenter                 |
| consoElecAnnuelle     | ^(\d{1,7}(\.\d{1,3})?)$                      | No  | N/A                                                        | No                                |                                                                |
| fabricant             | ^[A-Za-z0-9_-]+$                             | No  | N/A                                                        | No                                |                                                                |

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
front ->> back: POST /subscribers/{subscriber}/organizations/{organization}/digital-service-version/{digitalServiceVersionUid}/load-input-files
front -->> RND : Display the 'pending' button if another task is in progress
front -->> back: Resume loading once no other task is in progress
back -->> DataBase: Validate and load datacenter/s in the in_datacenter table
back -->> DataBase: Validate and load physical equipment/s in the in_physical_equipment table
back -->> DataBase: Validate and load virtual equipment/s in the in_virtual_equipment table
back -->> front: Update the loading history
front ->> back: GET /subscribers/{subscriber}/organizations/{organization}/digital-service-version/{digitalServiceVersionUid}
back-->> front: Get the updated digital service version
back ->> front: Display the updated loading history
front ->> RND : Display the 'completed' button if all the uploaded data is correct
front -->> RND : Display the 'failed' button if most of the uploaded data is incorrect or <br> mandatory headers missing
front ->> RND : Display the 'completed with errors' and 'download' button if some of the uploaded data is incorrect
RND ->> front : Click the 'completed with errors' button to download the rejected data
front -->> back: GET /subscribers/{subscriber}/organizations/{organization}/download-reject/{taskId}
back ->> front : Rejected files downloaded to the user's local machine

{{< /mermaid >}}
