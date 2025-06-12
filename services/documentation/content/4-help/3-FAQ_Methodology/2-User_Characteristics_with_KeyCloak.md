---
title: 'Collect my User Characteristics with Keycloak'
weight: 102
---

## Objective 
Retrieve the End-users characteristics to evaluate the impact of their devices using my Digital Service
Keycloak has built-in support to connect to existing LDAP or Active Directory servers., more details on https://www.keycloak.org/

## Steps in Keycloak

### Prerequites
Keycloak is in placed on your digital service

### Step 1
Log in to the Keycloak Administration Console and configure the User events settings on Login and Logout (configure / Realm settings / Events) with enough retention time to be representative
![Keycloak Administration Console](../images/2-Keycloak_Administration_Console.png)

### Step 2
Access to Events menu / User events tab to see the events
![Keycloak User events](../images/2_Keycloak_User_events.png)
We will assume that for this application there are **500 connections per year** and **average time between login and logout of 1 hour.**

### Step 3
Other option, connect directly on Keycloak database and with sql query, retrieve your statistics 
*SELECT COUNT(\*) FROM EVENT_ENTITY WHERE EVENT_TYPE = 'LOGIN' AND EVENT_TIME BETWEEN :start AND :end;*

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

### Step 4
Fill in in accordance with your users répartition tab prepared in prerequites.
![User Repartition Filled In](../images/2-UserRepartitionFilledIn.png)

