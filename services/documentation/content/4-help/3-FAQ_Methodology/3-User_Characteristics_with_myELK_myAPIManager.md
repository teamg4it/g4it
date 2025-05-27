---
title: 'Collect my User Characteristics with my ELK & my API Manager'
weight: 103
---

## Objective 

Retrieve the End-users characteristics to evaluate the impact of their devices using my Digital Service

## Using ELK (Elasticsearch, Logstash, Kibana)

### Monthly Active Users
Use application logs stored in the log repository.
Create a dashboard in Kibana to count the number of login requests.
Extrapolate the data over the desired time period.

### Average Session Duration
Track each navigation session using a unique session ID.
Aggregate the logs by session ID.
Compute the time difference between the first and last request for each session.
Visualize the average duration using Kibana.

### User Location
Use the client’s IP address found in the logs.
Set up a geolocation dashboard in Kibana to visualize user origin.
![3_ELK Information example](../images/3_ELK_Information_example.png)

## Using API Manager (Apigee)

### Device Type
Open the device analytics section in the API Manager.
Browser information provides insight into the type of device used for access.
![3_API_Manager_Example_Apigee.png](../images/3_API_Manager_Example_Apigee.png)

Google's “Apigee” tool (https://cloud.google.com/apigee?hl=fr), serving as API Manager

## Steps in G4IT

### Prerequites
Make hypothesis :
- about User location (maybe you know it is only India users)
- about User devises (maybe you know your application is used only on Mobile)

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

