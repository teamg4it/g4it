---
title: "Hide Reference Values"
description: "Explanations of hiding reference values in traces"
weight: 520
---

## Table of contents

<!-- TOC -->
  * [Table of contents](#table-of-contents)
  * [Description](#description)
  * [Objective](#objective)
  * [Feature Overview](#feature-overview)
  * [Scope of the feature](#scope-of-the-feature)
  * [Trace Transformation](#trace-transformation)
<!-- TOC -->

## Description

Hide Reference Values – Business Explanation: The **Hide Reference Values feature** allows the G4IT platform to **mask sensitive reference data in exported traces**, in order to **protect licensed datasets** while still preserving trace readability.

---

## Objective

To ensure that **licensed or restricted reference values** are not exposed in exported traces, while still allowing:

- Transparency of calculations
- Identification of the reference used
- Compliance with data licensing constraints

---

## Feature Overview

The feature introduces a mechanism to:

- Replace sensitive numerical reference values with `"hidden data"`
- Keep **reference metadata** (name, source, type) visible
- Apply masking only during **export of traces**

---

## Scope of the feature

- Applies **only to exported traces**
- Does **not impact internal calculations**
- Does **not modify stored data**
- For Sopra Steria SaaS:
    - Data with source **"Base IMPACTS ®Version 2.01"** → **NOT hidden**

---

## Trace Transformation

### Example 1 – Physical Equipment (Manufacturing / Distribution / End of Life)

#### Before

```json
{
  "formule": "ImpactEquipementPhysique = (Quantité(10.0) * referentielFacteurCaracterisation(7476.3392) * TauxUtilisation(1.0)) / dureeVie(6.005479452054795)",
  "ReferentielFacteurCaracterisation": {
    "valeur": 7476.3392,
    "source": "NegaOctet-20220211",
    "impact source": "MODELE"
  }
}
```

#### After

```json
{
  "formule": "ImpactEquipementPhysique = (Quantité(10.0) * referentielFacteurCaracterisation(\"hidden data\") * TauxUtilisation(1.0)) / dureeVie(6.005479452054795)",
  "ReferentielFacteurCaracterisation": {
    "valeur": "hidden data",
    "name": "[reference name]",
    "source": "NegaOctet-20220211",
    "impact source": "MODELE"
  }
}
```

### Example 2 – Usage Stage

#### Before

```json
{
  "formule": "ImpactEquipementPhysique = Quantité(10.0) * ConsoElecAnMoyenne(17082.0) * MixElectrique(0.14638050000000002) * TauxUtilisation(1.0)",
  "consoElecAnMoyenne": {
    "valeur": 17082,
    "valeurReferentielConsoElecMoyenne": 17082,
    "sourceReferentielFacteurCaracterisation": "NegaOctet-20220211",
    "impact source": "REELLE"
  }
}
```

#### After

```json
{
  "formule": "ImpactEquipementPhysique = Quantité(10.0) * ConsoElecAnMoyenne(\"hidden data\") * MixElectrique(0.14638050000000002) * TauxUtilisation(1.0)",
  "consoElecAnMoyenne": {
    "valeur": "hidden data",
    "valeurReferentielConsoElecMoyenne": "hidden data",
    "sourceReferentielFacteurCaracterisation": "NegaOctet-20220211",
    "impact source": "REELLE"
  },
  "mixElectrique": {
    "valeur": 0.14638050000000002,
    "name": "[reference name]",
    "dataCenterPue": 1.8,
    "valeurReferentielMixElectrique": "hidden data",
    "sourceReferentielMixElectrique": "Base IMPACTS ®Version 2.01",
    "serveur": true
  }
}
```
