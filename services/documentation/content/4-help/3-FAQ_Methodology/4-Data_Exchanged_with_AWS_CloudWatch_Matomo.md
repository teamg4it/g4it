---
title: 'Collect quantity of Data Exchanged with AWS CloudWatch & Matomo'
weight: 104
---

## Objective 

Retrieve the End-users characteristics to evaluate the impact of their devices using my Digital Service
- AWS CloudWatch monitor your AWS resources and the applications you run on AWS in real time https://docs.aws.amazon.com/cloudwatch/
- Matomo is a web analytics solution, more details on https://matomo.org/

## Steps on Matomo

### Prerequites
Matomo is in placed and monitors your digital service

### Step 1
Log in to the Matomo application and define the time period you want to explore (1 month is the minimum to be representative).

### Step 2
Access to the Visitors / Devices page in order to retrieve the user devices type repartitions

![Matomo Visitors Devices](../images/1_Matomo_Visitors_Devices.png)
We will assume that for this application there are **98% of Desktop users and 2% of Smartphone users.**

### Step 3
Access to the Visitors / Locations page in order to retrieve the user country repartitions

![Matomo Visitors Locations](../images/1_Matomo_Visitors_Locations.png)
We will assume that for this application there are **80% of French users, 16% of US users and 4% of Spain users.**

## Steps on CloudWatch

### Prerequites
Your digital service is on AWS infrastructure

### Step 1
Log to CloudWatch and then to the Metrics > Explore section

### Step 2
Select a period representative of the digital service's activity (be careful to be within the data retention period)

### Step 3
Select the "NetworkIn" and "NetworkOut" metrics (in sum mode), filter on the EC2 instances used as entry points, and finally group by "Sum“
![CloudWatch Network Metrics](../images/4-CloudWatch_NetworkMetrics.png)

### Step 4
Export your data and considering the observed period, calculated the quantity exchange by year
![Export Data from CloudWatch](../images/4-Export-Data.png)
We will assume that for this application there are 185 To for 5 months, so **444 To per year.**

## Steps in G4IT

### Prerequites
Prepare your quantity of data exchange repartition tab by type of network and country.
*Note : You must maybe make an approximation by associating each type of device with a type of network (example Smartphone with Mobile line and Desktop with Landline).*
Example :

| Type              | Number of unique users  |
|-------------------|-------------------------|
| Landline - France | 0.8\*0.98*444000=348096 |
| Mobile - France   | 0.8\*0.02*444000=7104   |
| Landline - Europe | 0.2\*0.98*444000=87024  |
| Mobile - Europe   | 0.2\*0.02*444000=1776   |

### Step 1
On G4IT Digital Service Module, click on Evaluate New Service
![Evaluate New Service](../images/1_Evaluate_New_Service.png)

### Step 2
On the Network tab, click on the «Add Network» button
![Add Network](../images/4-AddNetwork.png)

### Step 3
Fill in accordance with your quantity of data exchange repartition tab prepared in prerequites.
![Define Network](../images/4-DefineNetwork.png)
