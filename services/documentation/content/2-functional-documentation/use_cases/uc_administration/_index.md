---
title: '3. Administration'
description: "All use cases related to administrate users and organization"
weight: 30
mermaid: true 
---

## Table of contents

{{% children description="true" %}}

## Global Concepts

Here is the concept to understand on the G4IT administration.

- **Subscriber** : Customer of the platform, the one who signed a contract
- **Organization** : Subscriber-defined structuring element that allows the creation of a compartmentalized space within the subscription.
- **Subscriber’s administrator** : “Super-user” with the right to manage all organizations associated with the subscriber to whom he/she is associated.
- **Organization’s administrator** : “Super-user” authorized by the subscriber administrator to grant users access to this organization.
- **G4IT User** : Person authorized by an Administrator who accesses G4IT’s modules (information system and/or digital services) for all or part of the Administrator’s organisations. A user can be authorised by different administrators: in this case, he has access to the Organisations of the different Subscriptions for which he is authorised with read/write privileges.

{{< mermaid >}}
flowchart TD;
subgraph G4IT    _SuperAdmin[fa:fa-user G4IT SuperAdmin]
    subgraph Subscriber#1[<i> Subcriber 1...*]
        Subscriber#1_Admin[fa:fa-user Subscriber Admin]
        subgraph Organization#1[<i> Organization 1...*]
            Organization#1_Admin[fa:fa-user  Organization Admin]
            subgraph IS1DS1[<i> Users 0...*]
                IS1("My Information Systems") 
                DS1("My Digital Services")
                Users[fa:fa-user  Users]
            end
        end
    end
end
G4IT_SuperAdmin --> |Manage <br>via DB requests|Subscriber#1_Admin
Subscriber#1_Admin --> |Manage organizations <br>on Admin module|Organization#1_Admin
Organization#1_Admin --> |Manage users <br>on Admin module|Users  --> |Disable, Read or Write <br><i> Read by default|IS1
Users --> |Disable, Read or Write <br><i> Write by default|DS1

{{< /mermaid >}}
