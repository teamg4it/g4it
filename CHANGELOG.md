# G4IT

## 3.5.0

### Major Changes

- 796  |  Add new criteria based on the one available in BoaviztAPI
- 1103 | Give the possibility to disable the data consistency
- 585 | Display only users that can be managed in the administration panel

### Minor Changes

- 1391 | Update spring boot verion
- 1346 | Rename / delete workspace for non subscriber admin
- 1245 | Facilitate the criteria selection

## 3.4.0

### Major Changes

- 994 | User Deletion procedure
- 1357 | Integrate EcoMindAI in G4IT
- 1204 | Import csv files for digital services
- 1263 | Naming network entries in the Digital Service module – Network input
- 1261 | Naming user groups in the Digital Service module – Terminales input
- 740 | Visualize different cloud instances for the same type

### Minor Changes

- 1373 | Sonar fixes
- 950 | Accessibility Corrections
- 1320 | Mobile Screen Responsiveness for Top Header, Left side nav and Welcome page
- 1255 | Add BoaviztAPI link to github
- 1271 | Open Source - Be able to launch a calculation just after first installation
- 1200 | New Bug on electricity consumption : total display different than sum compute manually with export file
- 1193 | DB cleanup:Remove the link between a user and a digital service
- 1257 | Very High level of Digital Service calculated - Click in May
- 1200 | New Bug on electricity consumption : total display different than sum compute manually with export file
- 1174 | When there is one criteria evaluated, multicriteria view should be disabled in digital service module and criteria should be selected in inventory module
- 1251 | Character "%" breaks the note feature
- 1258 | Table "task", the column "created_by" is not alway valuated

## 3.3.0

### Major Changes

- 1079 | [BE]: Migrate digital services from "Demo" workspace to new workspace
- 1001 | Modify the first connection workflow
- 1149 | Visualize ecodesign & accessibility declaration
- 1003 | Remove the link between a user and a digital service

### Minor Changes

- 1166 | [BE]: Allow G4IT to accept a copy/paste of model with special character from DataModel
- 1189 | CalculImpactDSI Inventories have been deleted in Prod
- 1205 | [BE] Error in administation page when changing criteria
- 797 | Lag on the filter when there is a lot of data
- 1218 | Issue on the DS export feature
- 1246 | Inventory : Export available from Demo Workspace

## 3.2.1

### Minor Changes

- 1215 | Back-end is crashing then restarting automatically

## 3.2.0

### Major Changes

- 1004 | Welcome page

### Minor Changes

- 1108 | Workspace navigation from Admin module if i am not admin of the workspace
- 795 | Update the docker image of BoaviztAPI
- 1102 | Regression on total electricity consumption compute
- 1144 | G4IT KO with "not specified" location

## 3.1.0

### Major Changes

- 885 | New Header with Menu added, Left sidebar, Useful information page
- 1038 | Allow to estimate a inventory if only virtual equipments = CLOUD_SERVICES
- 1002 | Add a button to create its own personnal organization

### Minor Changes

- 1035| [Back-end] Improve the test cases and coverage for the new architecture
- 1005| Prod : Manage Users - bug on the users list when organization changes
- 1078| 2 files where without explicit error
- 988 | [Digital service] Edit a value with a decimal reset the values to 0
- 985 | Issue while exporting virtual equipment with new architecture

## 3.0.1

### Minor Changes

- 1071 | Location has not been set during data migration for network
- 1051 | Organization name is displayed in equipment Type on the UI

## 3.0.0

### Major Changes

- 958 | New Arch - Clean module information system and API in the back-end of G4IT
- 745 | New Arch - Clean module digital service and API in the back-end of G4IT
- 746 | New Arch - Clean database

### Minor Changes

- 1043 | Deletion of digital services is not possible and set default criteria if no active criteria
- 1040 | Default domain and subdomain for applications
- 803 | Pen test : Content spoofing
- 1042| versions not displayed in ui
- 1044| Details is not visible for network in bar graph in case of data consistency error in digital services
- 971 | Get country and electricity mix impact based on the new architecture

## 2.11.1

### Minor Changes

- 993 | Improve traceability of user actions
- 996 | Display loading loop in production
- 989 | Impossible to make calculation of some inventories in prod

## 2.11.0

### Major Changes

- 840 | Add a control on new architecture to verify if the physical or the virtual equipment is consistent
- 822 | Allow G4IT to accept different files type to load data

### Minor Changes

- 951 | Digital Service migration issue
- 948 | Error occured while migration
- 936 | Differences between visualization on application view in new architecture
- 944 | Control on the value of typeInfrastructure in virtualEquipment.csv
- 883 | Update the documentation based on the new Architecture
- 921 | Super admin functionality
- 882 | VMs are not deleted for the new architecture
- 900 | In digital services remove hover content visible
- 827 | In digital services Non cloud server values mismatching in old and new Arch
- 844 | Inventory file should not be part of export
- 820 | Allow to have decimal in annual electricity consumption
- 868 | In DS and IS set new Architecture as default
- 812 | Data model documentation improvement

## 2.10.0

### Major Changes

- 669 | New Arch - Migration code to copy inventory/ds data into the new mode and the rollback
- 695 | [Cloud IS] Modification of the export feature to integrate data generated by BoaviztAPI
- 691 | [Cloud IS] Import cloud data using virtual equipment file
- 695 | Modification of the export feature to integrate data generated by BoaviztAPI
- 692 | Modify evaluation batch to call BoaviztAPI
- 625 | New Architecture - Digital service inputs and indicators adaptation
- 624 | New Arch - Backend API evaluation
- 667 | New Architecture - Backend impact on existing GET APIs for indicators
- 624 | New Architecture - Backend API evaluation
- 748 | TS - Upgrade dependencies

### Minor Changes

- 847 | IS Criteria Graph In FR Category Others is not correctly displayed
- 842 | Resize of Cloud Image Space in filter icon and text Empty Entity Handling in Frontend
- 836 | Issues while loading data containing cloud equipment
- 841 | DS - Dedicated Non cloud server Datacenter not saved correctly
- 821 | DS - Non-cloud servers evaluation is incorrect with VMs
- 837 | Issues associated to the impact visualization
- 834 | Digital Service-Dedicated and shared servers have the same values: old architecture
- 831 | Sometimes progress bar is not dispalyed and Criteria values are doubled in new architecture evaluation
- 838 | Digital Service- recursive API calls for datacenter and server host
- 833 | IS- application module breaking
- 807 | Bugs in Inventory Service file import -old architecture
- 830 | DS For only one criteria selected multicriteria view is opening in old Arch instead of criteria view
- 758 | Tooltip displayed with no content
- 764 | Overlaping of 2 components on the IS Equipment view
- 826 | Digital Service graph elements sorting mismatch with Old and New Archiecture
- 823 | New Arch - Non-cloud servers with VMs number of VM is incorrect
- 816 | Filters are not working with new Architecture
- 814 | Aggregation issue on digital services with new arch
- 640 | G4IT Usage supervision (Matomo)
- 730 | Display indicators produced by BoaviztAPI in the front end for Application
- 694 | Display indicators produced by BoaviztAPI in the front end for Equipment view
- 757 | Accessibility Improvement : Constrast correction with cypress config
- 753 | Improve security on new APIs
- 747 | SipImpact column is null for DS export
- 700 | Frontend Critical Sonar fixes
- 723 | Sonar - frontend security issues
- 722 | Accessibility Improvement : table and aria
- 315 | Inventory footprint view : visualize when a filter is set
- 709 | Frontend - dev server migration to Vite
- 720 | Accessibility Improvement : label
- 763 | Fixing hour on front-end
- 794 | Replace low indicator empty by 0
- 760 | Upload button is clickable without any files
- 821 | Fixing non-cloud servers evaluation with VMs

## 2.9.2

### Minor Changes

- XXX | Backend - add environment variable : CORS_ALLOWED_ORIGINS
- 735 | Production - Digital service cloud servers quantity is not used for visualization

## 2.9.1

### Minor Changes

- 733 | Production - An error occurs during export
- 734 | Production - Digital service cloud servers are not well cleaned

## 2.9.0

### Major Changes

- 608 | UC digital service : Evaluate Cloud impact using BoaviztAPI
- 597 | Digital Service Module : Create a cloud instance using BoaviztAPI
- XXX | Improve global accessibility on the frontend

### Minor Changes

- 726 | Bugs on Cloud feature
- 690 | Export Cloud data
- 716 | Digital Service- Cloud Services: edit not working as expected
- 623 | New Architecture - backend api load input files
- 682 | Frontend Major and Minor Sonar fixes
- 613 | Give access to a user where domain name is not an authorized domain
- 703 | New inventory : Incorrect behavior when cleaning the date previously filled in
- 704 | Update the title of the Application and Environment pages on My Information System
- 628 | Add key indicator on criteria page
- 710 | Fix issue App-information-card in information-digital-service is not updated on navigation
- 702 | Data consistency show 100% of errors when export files contains no error
- 682 | Frontend Major and Minor Sonar fixes
- 698 | Allow mode to run without security
- 672 | New Architecture - Backend apis for in out objects
- 622 | New Architecture - Backend API referential
- 664 | Add organization name on digital services list view
- 693 | Bug on application view to get indicators in application view
- 676 | Upgrade frontend and backend dpendencies
- 679 | Equipment view : filters on empty value are not working
- 680 | Upgrade Keycloak version to 26.0.0 to remove vulnerability
- XXX | Upgrade Keycloak version to 26.0.0 to remove vulnerability
- 651 | Fixing sonar issues
- 666 | Equipment view - quantity empty when no date set
- 675 | Low impact electricity mix bad error management
- 643 | New Architecture - Technical basis for cloud
- 659 | first setup improvement and open-source documentation
- 705 | Add a control on server name

## 2.8.0

### Major Changes

- 579 | In the DS module, add an option to select the criteria used for the digital service evaluation
- 578 | In the IS module, add an option to select the criteria used for the inventory evaluation
- 572 | In the Administration module, add an option to select the criteria used for the evaluation (by subscriber / by organization's module)
- 573 | Add the 11 missing criteria that negaOctet have in database into G4IT
- 571 | Set up a criteria parameter for subscribers, organizations and modules

### Minor Changes

- 605 | Data quality in UC IS : Display the percentage of indicators with status 'ERREUR' in G4IT on the application view
- 596 | Data quality in UC IS : Display the percentage of indicators with status 'ERREUR' in G4IT on the equipment view
- 663 | Data consistency : Allow navigation through graph
- 604 | Data quality in UC DS : Display the percentage of indicators with status 'ERREUR' in G4IT
- 657 | Last feedback on the criteria selection
- 652 | Data is not displayed in tooltip
- 653 | defects/enhancements in existing implementations
- 573 | fix digital service view
- 630 | Note format broken
- 631 | Labels have not been translated in FR
- 611 | Encoding special characters in the right way while uploading files
- 573 | Add the 11 missing criteria that negaOctet have in database into G4IT
- 588 | Filter refactoring on the application view
- 589 | Display the number of people having access to a digital service
- 614 | Issue fixed on 564 - Digital service export to csv still have header fields
- 609 | Control on VM sum is not implemented correctly
- 564 | Delete all mention of "nbJourUtiliseAn" and "goTelecharge"
- 590 | Unlink instead of delete shared digital services
- 589 | Display the number of people having access to a digital service
- 560 | Add a new key indicator : total electricity consumption
- 602 | Compute server reset to default one when editting

## 2.7.0

### Major Changes

### Minor Changes

- 586 | Display a message when no user is found
- 583 | Organization is not deleted after one week
- 485 | Export data from a digital service
- 566 | Aggregate indicators data
- 558 | Frontend filters management refactoring
- 580 | The note is not visible in the equipment/application view
- 509 | Share a digital service with another person
- 549 | Add raw value to the radar graph of digital services impact visualization
- 519 | display informations about the connected user
- 565 | Change GUI of Information system module
- 507 | Create custom login and register page in keycloak
- 563 | Change the file name on the front end

## 2.6.0

### Major Changes

- 499 | Edit and delete roles for a user on an organization
- 497 | Add and edit organizations related to a subscriber

### Minor Changes

- 552 | Add compression to API REST
- 561 | first connection without having an existing demo organization
- 551 | Keep the same order in the radar graph for the multi criteria view
- 442 | Show and download template files and data model in frontend
- 548 | Set the server compute instead of storage by default
- 477 | Delete old roles at backend startup
- 527 | Issue - Recurring API call to inventory view by equipment
- 526 | Issue - Adding a note delete all the digital service's information
- 431 | UC Digital service : redirect user by clicking on the first graph
- 562 | UC Digital service : redirect user to the visualization after a calculation
- 567 | UC Digital Service : impossible to edit the server's type

## 2.5.0

### Major Changes

- 476 | Add Keycloak software to authenticate to G4IT
- 483 | Delete organizations related to a subscriber
- 496 | Add users on an organization

### Minor Changes

- 481 | Add Matomo tracking client
- 447 | Filters reset on tab change
- 512 | Allow decimal in Gb Network and hours spend in Terminal
- 508 | Change icon for IS and digital service module
- 517 | Warning before cancel on the "Note" side panel
- 512 | Allow decimal in Gb Network and hours spend in Terminal
- 508 | Change icon for IS and digital service module
- 517 | Warning before cancel on the "Note" side panel
- 498 | Industrialize service opening hours visualization
- 223 | MailTo support g4it
- 511 | [Digital Service] Change PUE by defaut for a new DC

## 2.4.0

### Minor Changes

- 513 | Choose english translation by default when browser is in another language
- 514 | Filter on one result isn't working (no result available)
- 187 | Change indicator low carbon to low impact on IS equipment
- 504 | Modify workflow at first connection
- 503 | Get firstname, lastname, sub from user at first connection

## 2.3.0

### Major Changes

- 502 | Refactoring urls with subscribers and organizations on backend and frontend

### Minor Changes

- 446 | Enhance visualization with a lot of entity
- 501 | Key indicators years and low carbon should be displayed as integer
- 500 | Header of application view is not correctly set
- 449 | Automate data export
- 430 | Bringing consistency to numbers
- 472 | Download Output files by API in backend
- 495 | Defect - Cannot load files twice without refreshing the view
- 475 | Fix multi-tenant backend configuration
- 474 | Opening access to an organization at the first connection
- 439 | Change equipment lifespan on a digital service
- 429 | Add a Note on inventory items and digital services
- 486 | Annual electricity consumption by default is null
- 487 | Label Calculate is not visible when button is disabled
- 400 | Give read-only access to the application
- 469 | Defect - Sum of VM quantity in Digital Service

## 2.2.0

### Minor Changes

- 463 | Add licence, copyright, readme and contribute
- 475 | Backend authentication in error
- 285 | Add simulation panel
- 295 | Display the completeness and progress rate of indicator calculation and inventory loading
- 426 | Improvement on application view by criteria
- 367 | Complete data purge after 2 years
- 452 | Change translation to be more accurate
- 365 | Automatic deletion of inventory files : work, export and output
- 208 | Show deployed application in Frontend (G4IT and NumEcoEval versions)
- 437 | No Data on applicative impact - Unspecified case
