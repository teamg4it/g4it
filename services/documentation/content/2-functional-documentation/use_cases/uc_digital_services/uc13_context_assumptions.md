---
title: "2.13. Context and Assumptions"
description: "This use case describes how to create and update Context and Assumptions for a digital service version"
weight: 130
mermaid: true
---

## Table of contents

- Description
- State Diagram
- Mockup
- Sequence Diagram

## Description

This use case allows a project stakeholder to document the **Context and Assumptions** associated with a digital service version.

The information is optional and can include:
- Context of the assessment
- Assumptions
- Sources and references
- Methodological choices
- Known limitations

During creation, users enter the Digital Service name, click **Next**, complete the **Context and Assumptions** page if desired, and then click **Validate Creation**. The content is saved with the created version.

After creation, users can edit the Context and Assumptions at any time using the **Edit Context/Assumptions** button and save their changes.

## State Diagram

{{< mermaid align="center" >}}
graph TD
A[Digital Service Name] -->|Next| B[Context and Assumptions]
B --> C[Enter or Skip Context]
C -->|Validate Creation| D[Context Saved with Version]
D -->|Edit Context/Assumptions| E[Update Context]
E -->|Save| F[Updated Context Saved]
{{< /mermaid >}}

## Mockup

**Create Context and Assumptions**

![uc13_create-context_assumptions_updated.png](../images/uc13_create-context_assumptions_updated.png)

**Context and Assumptions with content**

![uc13_after_create_context_updated.png](../images/uc13_after_create_context_updated.png)

**Edit Context and Assumptions**

![uc13_edit_context_updated.png](../images/uc13_edit_context_updated.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group | Elements | Type | Description |
|-----------|-------|----------|------|-------------|
|1|Tab|Context and assumptions|tab|Displayed after the Digital Service name page.|
|2||Helper text|information|Guides the user to document context, assumptions, references, methodology and limitations.|
|3||Context and assumptions|free text|Optional field.|
|4||Validate Creation|button|Saves the Context and Assumptions with the created version.|
|5||Edit Context/Assumptions|button|Opens the editor for an existing version.|
|6||Save|button|Saves the updated Context and Assumptions.|

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor User
participant FrontEnd
participant BackEnd
participant Database

User->>FrontEnd: Next
FrontEnd-->>User: Display Context and Assumptions page
User->>FrontEnd: Enter Context and Assumptions (optional)
User->>FrontEnd: Validate Creation
FrontEnd->>BackEnd: Create version with Context
BackEnd->>Database: Save Context and Assumptions
BackEnd-->>FrontEnd: Creation completed

User->>FrontEnd: Edit Context/Assumptions
FrontEnd->>BackEnd: Update Context
BackEnd->>Database: Save updated Context
BackEnd-->>FrontEnd: Update completed
{{< /mermaid >}}
