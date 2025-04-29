---
title: '5.2.1 Header - About G4IT'
description: ""
weight: 5021
---

## About G4IT

### Description

The "About G4IT" menu provides users with essential resources and useful information.
This menu is designed to help users easily find the support and information they need regarding G4IT's services.

![about_g4it.PNG](../images/about_g4it.PNG)

## Behavior Rules

{{% expand title="Show the detail" expanded="false" %}}

| Reference                        | Group               | Elements                  | Sub-Elements | Type        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|----------------------------------|---------------------|---------------------------|--------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1                                | About G4IT          |                           |              | Menu        | "About G4IT" menu provides users with essential resources and useful information                                                                                                                                                                                                                                                                                                                                                    |
| 2                                |                     |  Useful Information       |              | Link        | Access useful information, G4IT versions, Business hours and support contact.                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 3                                |                     |  Help Center              |              | External Links| Quick access to two important external resources (github and documentation) related to G4IT for additional help or information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |


{{% /expand %}}

## Accessibility 

User can use tab to focus on "About Us" menu button and to navigate to other menu buttons like "My Workspace" menu button.
To access the about us menu list- Arrow up and Arrow down can be used for navigating between menu list.

## Useful information

### Description

For a seamless experience and to make the most of your time on the platform, here's how you can easily find out when it'
s open for use:

1. Click on the "About Us" menu button then click on "Useful information" link located in the navigation bar.

2. Within the useful information page, locate the 'Service Opening Hours' section, Here, you'll find all the information you need
   regarding the platform's operating hours.

#### Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Access the G4IT app and proceed to the 'About Us' menu in menu list we have 'Useful Information' page link.
front ->> back: GET /api/business-hours
back -->> front: Fetch the business hours data from the G4IT business_hours table.

{{< /mermaid >}}

#### Service Opening Hours section view

![business_hours.PNG](../images/business_hours.PNG)

## Support to G4IT

### Description

Need support from G4IT? Here's how:

1. Locate the 'About Us' menu button at top header. On click of menu button a menu list will be visible in that list we have 'Useful Information' page link.  On click of that link it redirect to Useful information page.

2. There is a section of 'Support contact'. Click on the **`support.g4it@soprasteria.com`** button.

3. Your default mail client will then open with the following details pre-filled:

    * Recipient: support.g4it@soprasteria.com
    * Subject: [{SubscriberName}/{OrganizationId}] Support Request

#### Button view

![send_mail_button.PNG](../images/send_mail_button.PNG)

#### Default mail view

![support_mail.PNG](../images/support_mail.PNG)

Feel free to reach out for any support you require!


## Help Center


### Description
For a seamless experience and to make the most of your time on the platform, here's how you can easily find out when it'
s open for use:

1. Click on the "About Us" menu button then click on links link located in the navigation bar.

2. Within the "About Us" Menu, locate the "Go to G4IT GitHub" link, "Go to the documentation" links. On click of these links it will open in new tab.



