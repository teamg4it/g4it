---
title: 'Conditions and roles needed to access Ecomind'
description: 'Get to know the different conditions and roles needed to have access to Ecomind on G4IT'
weight: 10
---

There are 3 different conditions for a user to have access to Ecomind on G4IT : 

### Global Ecomind module activation

You can check [how to enable Ecomind module](../../1-getting-started/3-getting-started-as-a-maintainer/1-how-to/ecomind/enable_ecomind_module/_index.md) page to know how to globally enable Ecomind. 

- If this variable is set to false then no one will be able to access Ecomind, including a super admin.

### Ecomind module activation for a particular subscriber

You can check [how to enable Ecomind module for a subscriber](../../1-getting-started/3-getting-started-as-a-maintainer/1-how-to/ecomind/enable_ecomind_for_a_subscriber/_index.md) page to know how to enable Ecomind for a particular subscriber.

- If the global Ecomind module is set to true and Ecomind is set to false for a subscriber then no one will be able to access Ecomind on this subscriber, including a super admin.

- If the global Ecomind module is set to true and Ecomind is set to true for a subscriber, the access will depend on the user's roles. A super admin has access to Ecomind.

### Ecomind module roles for a particular user

- If the global Ecomind module is set to true, if Ecomind is set to true for a subscriber and if a user has not the role "ROLE_ECO_MIND_AI_READ" or "ROLE_ECO_MIND_AI_WRITE" then he has no access to Ecomind

- If the global Ecomind module is set to true, if Ecomind is set to true for a subscriber and if a user has the role "ROLE_ECO_MIND_AI_READ" or "ROLE_ECO_MIND_AI_WRITE" then he has access to Ecomind

When these 3 conditions are true, you should see this :   

![All access](images/ecomindallaccess.png)
