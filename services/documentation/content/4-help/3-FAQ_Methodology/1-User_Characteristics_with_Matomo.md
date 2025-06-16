---
title: 'Collect my User Characteristics with Matomo'
weight: 101
---

## Objective 
Retrieve the End-users characteristics to evaluate the impact of their devices using my Digital Service
Matomo is a web analytics solution, more details on https://matomo.org/

## Steps in Matomo

### Prerequites
Matomo is in placed and monitors your digital service

### Step 1
Log in to the Matomo application and define the time period you want to explore (1 month is the minimum to be representative).

### Step 2
Access to the Visitors / Overview (left menu) in order to retrieve the number and average visit duration

![Matomo Visitor Overview](../images/1_Matomo_Visitor_Overview.png)
We will assume that for this application there are 500 visits of approximately 10 minutes per month, so **we have 500 users who use the application for 120 minutes per year**.

### Step 3
Access to the Visitors / Devices page in order to retrieve the user devices type repartitions

![Matomo Visitors Devices](../images/1_Matomo_Visitors_Devices.png)
We will assume that for this application there are **98% of Desktop users and 2% of Smartphone users.**

### Step 4
Access to the Visitors / Locations page in order to retrieve the user country repartitions 

![Matomo Visitors Locations](../images/1_Matomo_Visitors_Locations.png)
We will assume that for this application there are **80% of French users, 16% of US users and 4% of Spain users.**

## Steps in G4IT

### Prerequites
Prepare your users répartition tab by type of devise and country
Example :

| Country            | Type of device | Number of unique users | Average time spent per user per year |
|--------------------|----------------|------------------------|--------------------------------------|
| French             | Smartphone     | 0.2\*0.8*500=80        | 1.2                                  |
| French             | Laptop         | 0.8\*0.8*500=320       | 1.2                                  |
| US                 | Smartphone     | 0.2\*0.16*500=16       | 1.2                                  |
| US                 | Laptop         | 0.8\*0.16*500=64       | 1.2                                  |
| Spain              | Smartphone     | 0.2\*0.04*500=4        | 1.2                                  |
| Spain              | Laptop         | 0.8\*0.04*500=16       | 1.2                                  |


### Step 1
On G4IT Digital Service Module, click on Evaluate New Service
![Evaluate New Service](../images/1_Evaluate_New_Service.png)
### Step 2
On the Terminals tab, click on the «Add Device» button
![Add Device](../images/1_Add_Device.png)

### Step 3
Fill in in accordance with your users répartition tab prepared in prerequites.
![New Device](../images/1_New_Device.png)
