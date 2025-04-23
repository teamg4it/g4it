---
title: '5.2.1 Header - About G4IT'
description: ""
weight: 5021
---

## Useful information

### Description

For a seamless experience and to make the most of your time on the platform, here's how you can easily find out when it'
s open for use:

1. Click on the "Settings" icon located in the navigation bar.

2. Within the settings menu, locate the 'Service Opening Hours' section, Here, you'll find all the information you need
   regarding the platform's operating hours.

#### Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Access the G4IT app and proceed to the 'Settings' icon on bottom left.
front ->> back: GET /api/business-hours
back -->> front: Fetch the business hours data from the G4IT business_hours table.

{{< /mermaid >}}

#### Service Opening Hours section view

![business_hours.PNG](../images/business_hours.PNG)

## Support to G4IT

### Description

Need support from G4IT? Here's how:

1. Locate the "Settings" icon in the navigation bar.

2. Click on the **`support.g4it@soprasteria.com`** button.

3. Your default mail client will then open with the following details pre-filled:

    * Recipient: support.g4it@soprasteria.com
    * Subject: [{SubscriberName}/{OrganizationId}] Support Request

#### Button view

![send_mail_button.PNG](../images/send_mail_button.PNG)

#### Default mail view

![support_mail.PNG](../images/support_mail.PNG)

Feel free to reach out for any support you require!
