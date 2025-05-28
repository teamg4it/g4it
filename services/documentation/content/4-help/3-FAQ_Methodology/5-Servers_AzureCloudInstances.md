---
title: 'Collect Azure Cloud instances characteristics'
weight: 105
---

## Objective 
Retrieve the characteristics of my Cloud instances managed on Azure

## First Option : Steps on Azure Console

### Step 1
Log to Azure Console

### Step 2
Navigate to Virtual Machine category
![Azure Virtual Machine category](../images/5-Azure_Virtual_Machine_category.png)

### Step 3
- Retrieve for each instance:
- The name of the size
- The Region
- The automatic shutdown policy
![Azure instance](../images/5-Azure_Instance.png)

## Second Option : Steps on Az CLI

### Step 1
Log in to the CLI and select the correct subscription.az login

### Step 2
List the VMs and their characteristics
az vm list --query "[].{ResourceGroup:resourceGroup, Name:name, template:hardwareProfile.vmSize}" --output tsv

### Step 3
Retrieve for each instance:
- The name of the size
- The Region
- The automatic shutdown policy

## Steps in G4IT

### Prerequites
Prepare the csv file with the data collected according to the Azure template on the page [Help with pre-filled templates](../../1-getting-started/1-getting-started-as-a-user/2-module-information-system/1-collect_data/01_Help_yourself_with_pre-filled_templates.md).
Note :
- Put Name of size in lowercase;
- For Average load, we propose you to put 25% (source) and let you adapt as per the information you have.

The automatic shutdown policy will help to estimate annual usage time:
- Without automatic shutdown :  365/* 24 =8760 hours
- With automatic shutdown from 7:00PM to 7:00 AM : 365/*12= 4380 hours

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


### Step 1
On G4IT Information System Module, create a new inventory and load your prepared file as Virtual Equipment file
![Azure Inventory](../images/AzureInventory.png)
### Step 2
Launch Estimate

### Step 3
Visualize the impact clicking on the Equipment button
