---
title: "Enable Ecomind module for a particular subscriber"
description: "How to enable Ecomind module for a particular subscriber on G4IT"
weight: 3
---

### Default behavior

By default, Ecomind module is disabled for all subscribers, so it is not accessible at all (frontend and backend) for all subscribers and users.

You have to enable it manually for each subscriber.

To do so, a new column has been added to the **g4it_subscriber** table.

This column is called **ecomindai** and is false by default. This column can only be edited via the database.

![The ecomindai column](images/ecomindai.png)

### How to enable Ecomind for a subscriber ?

To enable Ecomind for a subscriber you have to do it manually via the database.

To do so, you need to change the value of the "ecomindai" column of the g4it_subscriber table.

Here the ecomindai column isn't checked meaning the Ecomind module is disabled for the subscriber test.
![Ecomind module disabled for a subscriber](images/false.png)

Now the ecomindai column is checked meaning the Ecomind module is enabled for the subscriber test.
![Ecomind module disabled for a subscriber](images/true.png)
