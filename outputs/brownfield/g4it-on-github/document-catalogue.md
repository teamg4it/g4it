---
workflow: brownfield-analysis
subworkflow: document-ingestion
trigger: aggregate-catalogue
date: "2026-11-20"
status: blocked
inputDocuments:
  - .github/skills/ingestion-batching/SKILL.md
  - .github/skills/office-common/SKILL.md
  - .github/skills/ingestion-triage/SKILL.md
  - outputs/brownfield/g4it-on-github/extraction-verification.md
  - outputs/brownfield/g4it-on-github/image-interpretation.md
  - outputs/ingestion/g4it-on-github/ingestion-params.json
  - outputs/ingestion/g4it-on-github/batch-plan.json
  - outputs/ingestion/g4it-on-github/aggregate-coverage.json
  - outputs/ingestion/g4it-on-github/triage-decisions.json
  - "outputs/ingestion/repository/g4it-on-github-*/verify-result.json"
  - outputs/ingestion/repository/g4it-on-github-functional-02/pending-image-members.json
  - outputs/ingestion/repository/g4it-on-github-getstarted-01/pending-image-members.json
  - outputs/ingestion/repository/g4it-on-github-getstarted-02/pending-image-members.json
  - services/documentation/content/_index.md
  - services/documentation/content/2-functional-documentation/_index.md
  - services/documentation/content/2-functional-documentation/global_concepts/_index.md
  - services/documentation/content/2-functional-documentation/global_concepts/environmental_footprint_assessment_methodology/_index.md
  - services/documentation/content/2-functional-documentation/global_concepts/uc1_dataconsistency.md
  - services/documentation/content/2-functional-documentation/global_concepts/uc2_people_equivalent.md
  - services/documentation/content/2-functional-documentation/use_cases/uc_authentification/_index.md
  - services/documentation/content/3-technical-documentation/1-Non_functional_requirements/_index.md
  - services/documentation/content/3-technical-documentation/2-software_architecture/_index.md
  - services/documentation/content/3-technical-documentation/3-technical_architecture/_index.md
  - services/documentation/content/3-technical-documentation/3-technical_architecture/ecomind/_index.md
  - services/documentation/content/3-technical-documentation/4-backend_documentation/1-software_code_architecture/_index.md
  - services/documentation/content/4-help/1-glossary.md
  - services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/_index.md
  - outputs/ingestion/repository/g4it-on-github-technical-01/extracted/e3af7ba22d3cbe6c9552ad846bfd70cf5460c6686c55ed162fef73c272c50c6e.md
changeHistory:
  - date: "2026-11-20"
    change: "Remplacement du placeholder par un catalogue diagnostique partiel : 14 lots revérifiés, agrégation refusée, 15 lectures documentaires attestées ; exclusions et omissions préservées."
  - date: "2026-11-20"
    change: "Reprise de la seule station aggregate-catalogue : 10 exclusions reconnues après triage.py decide ; images.py handoff, 14 vérifications et agrégation relancés. 80 PNG encore pending, écriture du registre groupé refusée ; 15 textes relus."
holisticQualityRating: "partial-evidence-not-production-ready"
overallStatus: blocked
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
station: ingest-documents/aggregate-catalogue
agent: document-ingestion
skill:
  - ingestion-batching
  - office-common
  - ingestion-triage
sensitivity: confidential
context_manifest:
  - path: .github/skills/ingestion-batching/SKILL.md
    key_facts:
      - "Skill lu intégralement ; aggregate écrit aggregate-coverage.json et refuse toute source unresolved, y compris les exclusions post-triage."
      - "in_scope = découvertes moins exclusions convenues au triage ; resolved n'est pas une attestation de lecture de l'agent."
  - path: .github/skills/office-common/SKILL.md
    key_facts:
      - "Skill lu intégralement ; les omissions groupées doivent reprendre les membres exacts puis être vérifiées et confirmées par digest."
      - "Omission reste partielle ; explication est du contexte utilisateur ; lecture des artefacts par groupes de 50 maximum."
  - path: .github/skills/ingestion-triage/SKILL.md
    key_facts:
      - "Skill lu intégralement : decide accepte plusieurs --path pour une même décision ; chaque empreinte est calculée par le helper."
      - "Une replanification peut retirer des images isolées si leur dossier ne contient plus de document en périmètre."
  - path: outputs/ingestion/g4it-on-github/ingestion-params.json
    key_facts:
      - "Mode nested, analysis_id=g4it-on-github, source_root=output_root='.', batch_size=100, grouping area-map, legacy_codepage=null."
  - path: outputs/ingestion/g4it-on-github/batch-plan.json
    key_facts:
      - "14 lots, 541 sources en requêtes, derived=0 ; une source sensible hors requêtes."
      - "Les états stockés du plan sont historiques ; les états courants proviennent des vérifications et de batching status."
  - path: outputs/brownfield/g4it-on-github/extraction-verification.md
    key_facts:
      - "La section Current state consigne les 10 exclusions utilisateur post-triage du 2026-11-20."
      - "Le retour temporaire au plan de triage aurait retiré 77 PNG ; il a été annulé pour préserver leur traçabilité."
  - path: outputs/brownfield/g4it-on-github/image-interpretation.md
    key_facts:
      - "Rapport présent, sans interprétation ; son affirmation d'absence de documents extraits est contredite par les vérifications courantes."
  - path: outputs/ingestion/g4it-on-github/aggregate-coverage.json
    key_facts:
      - "Agrégat de cette reprise : allowed=false ; discovered=542, excluded=189, in_scope=353, resolved=273, unresolved_total=80, success_rate=0.7734."
      - "188 excluded dans les requêtes, 272 not_required, 1 extracted, 80 partial ; 11 lots passed, 3 blocked, aucun running."
  - path: outputs/ingestion/g4it-on-github/triage-decisions.json
    key_facts:
      - "Les 10 XLSX/PPTX sont désormais exclude par chemin avec le motif utilisateur daté du 2026-11-20 ; decide a appliqué les 10 décisions, sans source indécise."
  - path: outputs/ingestion/repository/g4it-on-github-functional-02/pending-image-members.json
    key_facts:
      - "23 identités exactes source/source_sha256/locator lues ; locator=image:whole-file."
  - path: outputs/ingestion/repository/g4it-on-github-getstarted-01/pending-image-members.json
    key_facts:
      - "33 identités exactes lues ; request_sha256=55ecf2bdd4c4d819afbefc453368b9581e4acca0871603eb8d139889587f7535."
  - path: outputs/ingestion/repository/g4it-on-github-getstarted-02/pending-image-members.json
    key_facts:
      - "24 identités exactes lues ; aucun pixel inspecté."
  - path: .github/skills/office-common/extract_sources.py
    key_facts:
      - "repository-verify accepte source-root, mais refuse workflow, station, trace-id et legacy-codepage ; ces flags n'ont donc pas été ajoutés."
  - path: .github/skills/office-common/images.py
    key_facts:
      - "Code lu intégralement : trois opérations export/apply/handoff ; aucune commande de création d'omission utilisateur."
      - "handoff a terminé avec allowed=true pour le plan, sans rendre les trois vérifications visuelles finales allowed."
  - path: .github/skills/ingestion-batching/tools/batching.py
    key_facts:
      - "aggregate charge le plan existant et les décisions de triage courantes ; les exclusions request-level vérifiées deviennent reconnues lorsque leur décision de triage est exclude."
      - "Aucun plan --rewrite exécuté : les membres PNG et les requêtes sont conservés."
  - path: outputs/ingestion/repository/g4it-on-github-technical-01/source-coverage.json
    key_facts:
      - "Une seule copie décodée, issue de common_references_tables en UTF-16 LE ; les autres textes du lot sont directs."
  - path: services/documentation/content/3-technical-documentation/2-software_architecture/_index.md
    key_facts:
      - "Le texte décrit SPA Angular, REST Spring Boot, PostgreSQL, stockage de fichiers et authentification Keycloak."
  - path: services/documentation/content/2-functional-documentation/global_concepts/uc1_dataconsistency.md
    key_facts:
      - "Un indicateur non calculable ne doit pas être interprété comme un impact nul."
  - path: services/documentation/content/4-help/1-glossary.md
    key_facts:
      - "Organisation, workspace, inventaire et rôles administrateur/utilisateur sont définis ; anciens termes signalés."
---
<!-- AI_DISCLAIMER v1.0 -->
# Catalogue documentaire — g4it-on-github

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: trois skills lus intégralement, plan et preuves de contrôle relus ; 15 textes relus en groupes de 8 puis 7.

## Résultat : bloqué, catalogue partiel

Seule la station `ingest-documents/aggregate-catalogue` est exécutée.
L'agrégateur a produit son diagnostic puis refusé le passage : **`allowed: false`, code retour 1**.
Aucune règle ou requête de lot n'a été réécrite pour forcer le succès. Les 10 exclusions confirmées
ont été enregistrées par le helper de triage, sans modification manuelle des empreintes.
Le présent catalogue ne revendique pas l'exhaustivité sémantique du corpus.

| Mesure | Valeur | Sens |
|---|---:|---|
| Sources découvertes | 542 | 541 en requêtes, 1 sensible hors requêtes |
| Exclusions convenues au triage | 189 | 188 en requêtes, dont les 10 XLSX/PPTX, + 1 sensible hors requêtes |
| Sources dans le périmètre agrégé | 353 | 542 − 189 |
| Résolues par le moteur | 273 | 272 textes directs disponibles + 1 copie décodée |
| Non résolues par l'agrégateur | 80 | Seulement les PNG partiels, omissions utilisateur connues mais non enregistrées techniquement |
| Taux du moteur | 77,34 % | 273/353 ; ce n'est pas un taux de lecture ou de compréhension |
| Lots revérifiés | 14 | 11 passed, 3 blocked, 0 running, 0 failed |
| Conversions de format / dérivés déclarés | 0 / 0 | Aucun export accessible déclaré dans le plan |
| Copie issue du décodage | 1 | UTF-16 LE → UTF-8 ; préexistante, pas une conversion exécutée ici |
| Lectures documentaires attestées à cette reprise | 15/273 | 14 originaux Markdown + la copie décodée ; 258 autres non lus |
| Images interprétées / expliquées | 0 / 0 | Aucune observation métier tirée des captures |
| Décisions utilisateur préservées | 90 sources | 10 exclusions justifiées, 80 omissions visuelles sans explication |
| Décisions nouvellement enregistrées | 10 exclusions | triage.py decide allowed=true ; omissions techniques confirmées=0 |

Réconciliation : **542 = 189 exclusions + 273 textes résolus + 80 PNG partiels** ;
**541 = 188 exclusions en requêtes + 273 textes résolus + 80 PNG partiels**.
Les 10 exclusions autrefois post-triage sont désormais convenues au triage et reconnues dans
l'agrégat. Les 80 images restent en périmètre ; aucune omission n'est comptée comme extraction.

## Exécution et preuves techniques

1. Lecture intégrale des trois skills, des paramètres, du plan et du rapport d'extraction ;
   lecture du rapport d'images facultatif, présent mais obsolète.
2. `python3 .github/skills/ingestion-triage/tools/triage.py decide --analysis-id g4it-on-github
   --path <chacun des 10 chemins du tableau des exclusions> --decision exclude --reason <motif confirmé>`
   exécuté en **un seul appel** : `allowed=true`, `applied=10`, aucune source ni catégorie indécise.
   Le helper a calculé les empreintes ; aucun hash n'a été saisi ou modifié manuellement.
   Aucun `plan --rewrite` n'a été lancé : il aurait risqué de retirer 77 PNG comme signalé dans
   l'historique. `aggregate` lit les décisions actuelles avec les exclusions déjà présentes et
   vérifiées dans les requêtes ; il n'exige pas cette replanification.
   Les compteurs et `decisions_sha256` du plan sont historiques, non une photographie des
   décisions actuelles. L'agrégat courant porte les comptes de référence.
3. `python3 .github/skills/office-common/images.py handoff --analysis-id g4it-on-github` :
   terminé, code 0, `allowed=true`. Cela autorise seulement les lacunes visuelles ; les
   vérifications finales de functional-02, getstarted-01 et getstarted-02 restent false.
4. `python3 .github/skills/office-common/extract_sources.py repository-verify
   --analysis-id g4it-on-github-<lot> --source-root .`
   exécuté pour chacun des 14 lots. Les onze lots sans images passent ; les trois lots visuels
   refusent (code 1). Aucun échec non visuel restant dans leurs vérifications.
   Cette sous-commande n'accepte pas `--workflow`, `--station` ni `--trace-id` ; ses métadonnées
   historiques ne sont pas retouchées.
5. Lecture des listes canoniques des 80 membres visuels exacts : groupes de 23, 33 et 24.
   Tentative d'enregistrement natif de la première décision groupée (23 membres functional-02),
   `decision=omit`, `explanation=""`, `omission_reason=""`,
   `shared_explanation_confirmed=false`, `confirmed=false`, en attente du digest.
   **Refus preToolUse : `write target is outside every running station's output location`.
   Emplacement attendu annoncé : `outputs/brownfield/g4it-on-github/document-catalogue.md`.**
   Chemin refusé : `outputs/ingestion/repository/g4it-on-github-functional-02/bulk-image-decisions.json`.
   Aucun registre groupé n'est présent après ce refus ; aucune tentative de contournement
   par script, autre chemin ou décisions individuelles. Les deux autres registres ne sont pas créés.
   Le cycle digest → confirmation → nouvelle vérification n'a donc pas pu être engagé.
   `images.py` n'offre pas d'opération `omit` : `apply` consomme des observations modèle,
   qui seraient incompatibles avec la décision de ne pas interpréter. Aucun export de pixels lancé.
6. `python3 .github/skills/ingestion-batching/tools/batching.py aggregate
   --analysis-id g4it-on-github --workflow document-ingestion
   --station ingest-documents/aggregate-catalogue --trace-id 4d1e4282-cc4b-4ea6-83da-14dd38c0b892` :
   diagnostic écrit dans `outputs/ingestion/g4it-on-github/aggregate-coverage.json`, refus attendu.
   **Résultat : 80 non-résolus, et non plus 90 ; les 10 exclusions sont reconnues.**
7. `python3 .github/skills/ingestion-batching/tools/batching.py status --analysis-id g4it-on-github` :
   aucun lot en exécution, aucune requête
   périmée ; son `allowed: true` signifie seulement que le statut est disponible, pas que la
   station d'agrégation passe.

Les outils de vérification ne réextraient pas les documents et n'attestent pas leur compréhension.
Les originaux sensibles n'ont pas été ouverts. Aucun OCR, macro, accès réseau, installation,
nouvelle extraction ou modification de `outputs/runs/`. Tous les scripts ont été appelés via
`python3`, sans Python inline. Les labels workflow/station/trace ont été passés à `aggregate`,
qui les accepte ; `decide`, `images.py`, `repository-verify` et `status` ne les acceptent pas.

**Contrôle sur disque après écriture native des deux Markdown :** relecture de leurs métadonnées,
du manifeste, des sections de fin et des comptes ; dix exclusions utilisateur retrouvées dans
`aggregate-coverage.json` avec `origin=request`, `verified=true` et le motif confirmé ;
comptage des PNG dans les trois requêtes inchangées = 23 + 33 + 24 ;
aucun `bulk-image-decisions.json` présent. Le refus reste bloquant, sans succès simulé.

## Couverture par lot

Les chemins ci-dessous sont relatifs à la racine du workspace. Pour chaque lot, le résultat de
vérification est le fichier frère `verify-result.json` du `source-coverage.json` indiqué.
La requête est `outputs/ingestion/requests/g4it-on-github/<lot>.json`.
Ces couvertures conservent les sources originales, empreintes, `read_path`, localisateurs et motifs
d'exclusion individuels ; un état `passed` ne signifie pas que tous ces textes ont été lus ici.

| Lot | Sources | Textes résolus | Exclus triage initiaux + ajoutés ici | PNG partiels | État | Couverture |
|---|---:|---:|---|---:|---|---|
| backend-01 | 100 | 21 | 79 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-backend-01/source-coverage.json` |
| backend-02 | 25 | 19 | 4 + 2 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-backend-02/source-coverage.json` |
| docs-01 | 1 | 1 | 0 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-docs-01/source-coverage.json` |
| frontend-01 | 98 | 4 | 94 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-frontend-01/source-coverage.json` |
| frontend-02 | 3 | 3 | 0 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-frontend-02/source-coverage.json` |
| functional-01 | 54 | 54 | 0 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-functional-01/source-coverage.json` |
| functional-02 | 53 | 28 | 0 + 2 | 23 | blocked | `outputs/ingestion/repository/g4it-on-github-functional-02/source-coverage.json` |
| getstarted-01 | 58 | 19 | 1 + 5 | 33 | blocked | `outputs/ingestion/repository/g4it-on-github-getstarted-01/source-coverage.json` |
| getstarted-02 | 57 | 32 | 0 + 1 | 24 | blocked | `outputs/ingestion/repository/g4it-on-github-getstarted-02/source-coverage.json` |
| help-01 | 10 | 10 | 0 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-help-01/source-coverage.json` |
| keycloak-01 | 1 | 1 | 0 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-keycloak-01/source-coverage.json` |
| releases-01 | 28 | 28 | 0 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-releases-01/source-coverage.json` |
| schemacrawler-01 | 1 | 1 | 0 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-schemacrawler-01/source-coverage.json` |
| technical-01 | 52 | 52 | 0 + 0 | 0 | passed | `outputs/ingestion/repository/g4it-on-github-technical-01/source-coverage.json` |
| **Total** | **541** | **273** | **178 + 10** | **80** | **11 passed / 3 blocked** | Agrégat ci-dessus |

## Catalogue des textes effectivement lus

**Attestation de cette reprise, agent `document-ingestion`, 2026-11-20** : les 15 artefacts C01–C15 ont été
relus intégralement, sans troncature de leur texte, via l'outil natif de lecture. Les images
référencées, médias distants et liens externes n'ont pas été ouverts. Les identifiants de lot
renvoient exactement aux couvertures du tableau précédent.

Progression consignée : **8/273** (C01–C08), puis **15/273** (C09–C15).
Chaque groupe est inférieur à 50 documents. Arrêt de la consolidation exhaustive sur le refus
de l'agrégateur ; les 258 autres textes ne sont ni résumés ni attestés comme lus.
Aucun enregistrement d'observation visuelle n'a été lu ou produit ; aucune vision réussie n'est revendiquée.

Pour C01–C14 : provenance = original Markdown lu directement, `status=not_required` au sens
« extraction inutile », non « lecture inutile ». Les hashes et découpages `original-lines` sont
référencés dans le record `source` de la couverture du lot. Les résumés ci-dessous sont des
paraphrases de texte, jamais des descriptions de captures.

| ID | Source originale exacte | Lot | Sections indexables / constat textuel | Lecture et limites |
|---|---|---|---|---|
| C01 | `services/documentation/content/_index.md` | docs-01 | Getting started ; Why choose G4IT? ; What is G4IT? ; Who should use G4IT? ; In a nutshell. Présentation de la plateforme et des publics. | Texte entier lu ; vidéos liées non vues ; assertions promotionnelles non validées. |
| C02 | `services/documentation/content/2-functional-documentation/_index.md` | functional-01 | Table of contents ; titre Functional documentation. | Texte entier lu ; index Hugo dynamique, pas une preuve de lecture de ses enfants. |
| C03 | `services/documentation/content/2-functional-documentation/global_concepts/_index.md` | functional-01 | Table of contents ; titre Global Concepts. | Texte entier lu ; index Hugo dynamique uniquement. |
| C04 | `services/documentation/content/2-functional-documentation/global_concepts/environmental_footprint_assessment_methodology/_index.md` | functional-01 | Description ; Table of contents. NumEcoEval, Boavizta et EcoLogits. | Texte entier lu ; méthodologies externes non consultées. |
| C05 | `services/documentation/content/2-functional-documentation/global_concepts/uc1_dataconsistency.md` | functional-01 | Description ; Formula to explain the calculation ; Inventory / Global view ; Digital service. Données non calculables et affichage de leurs limites. | Texte et tableaux entiers lus ; illustrations liées non interprétées. |
| C06 | `services/documentation/content/2-functional-documentation/global_concepts/uc2_people_equivalent.md` | functional-01 | Description. Définition d'équivalent-personne à partir des limites planétaires. | Texte entier lu ; rapport et illustration liés non lus. |
| C07 | `services/documentation/content/2-functional-documentation/use_cases/uc_authentification/_index.md` | functional-01 | Activity Diagram ; Session Expiration Handling. Comportement documenté lors d'un 401 et expiration Keycloak. | Texte entier lu ; diagramme raster non vu. |
| C08 | `services/documentation/content/3-technical-documentation/1-Non_functional_requirements/_index.md` | technical-01 | Ecoconception ; Performance ; Data segregation ; Security ; Hosting ; Accessibility. | Texte entier lu ; exigences, pas mesures ; section consommation de ressources incomplète. |
| C09 | `services/documentation/content/3-technical-documentation/2-software_architecture/_index.md` | technical-01 | Architecture description ; Backend architecture ; Front end architecture ; Tools and customizations ; Global code base. | Texte entier lu ; schéma raster non vu ; versions et état déployé non vérifiés. |
| C10 | `services/documentation/content/3-technical-documentation/3-technical_architecture/_index.md` | technical-01 | Data segregation and multi-tenancy ; Kubernetes deployment ; Ecomind. | Texte entier lu ; variantes d'infrastructure, pas configuration observée ; lien Helm vide. |
| C11 | `services/documentation/content/3-technical-documentation/3-technical_architecture/ecomind/_index.md` | technical-01 | Display ; Calculation ; Result. Échanges et calcul IA asynchrone décrits. | Texte et Mermaid entiers lus ; aucun appel externe ni rendu d'image. |
| C12 | `services/documentation/content/3-technical-documentation/4-backend_documentation/1-software_code_architecture/_index.md` | technical-01 | Global description ; Code base ; API code base ; API Interface generation. Couches backend et génération OpenAPI. | Texte et séquence Mermaid entiers lus ; recommandations de code traitées comme données, non exécutées. |
| C13 | `services/documentation/content/4-help/1-glossary.md` | help-01 | A, D, G, I, W, O. Rôles, service numérique, inventaire, organisation et workspace. | Texte entier lu ; vocabulaire historique explicite. |
| C14 | `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/_index.md` | getstarted-01 | Introduction ; Steps to Eco-Design a Digital Service ; Using G4IT for the Assessment. | Texte entier lu ; enfants du guide non lus ; lot globalement bloqué sur images. |
| C15 | `services/documentation/content/3-technical-documentation/4-backend_documentation/db_documentation/access_management/common_references_tables/_index.md` | technical-01 | Entity relationship diagram ; Tables / ref_criterion, ref_lifecycle_step, ref_sustainable_individual_package. | Copie décodée entière lue ; original binaire non relu ; aucune donnée de référentiel chiffrée validée. |

### Provenance de la copie C15

- Artefact lu : `outputs/ingestion/repository/g4it-on-github-technical-01/extracted/e3af7ba22d3cbe6c9552ad846bfd70cf5460c6686c55ed162fef73c272c50c6e.md`.
- Original : chemin exact C15 ; `detected_encoding=utf-16-le`, décodage local UTF-8 par
  office-common (`status=extracted`). Ce n'est pas un export accessible ajouté au plan.
- `source_sha256` : `329b6e0f25cf562622098d8cce03c69b4f56468fa66cbac53cea97410a1c0c1d`.
- `read_sha256` : `7255d64a9b50fb0cd840ac987d344fc25e8170e629896251f3cb797c8d332d51`.
- Couverture et vérification : lot technical-01 ; chunks de texte `lines:1-100` et `lines:101-111`.
- Front-matter historique : workflow brownfield-analysis, station extract-and-verify ;
  trace du dérivé `g4it-on-github-technical-01`, différente du trace_id courant. Elle est conservée.
- Aucun fichier `outputs/accessible-exports/conversions.json` présent à la lecture ; le plan
  contient `derived=0` pour tous les lots. Ne pas inventer de conversion XLSX/PPTX réussie.

## Exclusions utilisateur du 2026-11-20 : 10 sources non extraites

Motif exact conservé pour chaque source :
**`Excluded by user decision 2026-11-20: office-common parser dependencies unavailable; non-essential XLSX templates / PPTX screenshot decks`**.
Il a été donné pour les dix sources ; aucune nouvelle justification métier n'est ajoutée.
Les hashes ci-dessous sont recopiés des couvertures vérifiées, pas calculés ou inventés par l'agent.
Les sources exclues n'ont été ni ouvertes ni résumées ici.

| Lot | Source originale exclue | SHA-256 |
|---|---|---|
| backend-02 | `services/backend/storage/--INTERNAL-G4IT--/0/templates/G4IT_Datamodel_and_reference_data_v3.2.0.xlsx` | `471b2339a8e4707c4c24ef5d1a9349dd200bb839a3dc568a94e91e01eedfaed0` |
| backend-02 | `services/backend/storage/--INTERNAL-G4IT--/0/templates/G4IT_Datamodel_workspace_reference_data_v1.0.0.xlsx` | `5ed4ab80f9c0e92c46d0ab0cf46eb1507be7df190a442b8fd9ec8d406e516c6b` |
| functional-02 | `services/documentation/content/2-functional-documentation/use_cases/uc_navigation_menu/images/Welcome page.pptx` | `0e8be015236c0d8cd78c73a0df5af4c1f8eec25bb3e57246ca74ae8750c67e06` |
| functional-02 | `services/documentation/content/2-functional-documentation/use_cases/uc_navigation_menu/uc2_header/images/Create new organization.pptx` | `1dd84773bb48daa0a1d5a59d465a7c7a8d224357eb20b835d5d3ef74934b0637` |
| getstarted-01 | `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/screenshot.pptx` | `9c67c86b5efc760d14c20d1f8f268d6784669c9183637560f11f04162fac4c02` |
| getstarted-01 | `services/documentation/content/1-getting-started/1-getting-started-as-a-user/2-module-information-system/documents/application_toFillIn.xlsx` | `77bf22475fa44875a3db212ab7ac83c631ef8c160b592aa1326a709f02cbff4b` |
| getstarted-01 | `services/documentation/content/1-getting-started/1-getting-started-as-a-user/2-module-information-system/documents/datacenter_toFillIn.xlsx` | `f603ba43762a2c7bd725399f3c0288a7bd90027926773d8131ec26bccf31afaf` |
| getstarted-01 | `services/documentation/content/1-getting-started/1-getting-started-as-a-user/2-module-information-system/documents/physicalEquipment_toFillIn.xlsx` | `e51d70dc86756eb6360eea90c2a82872cae95027c5034ee8f99b33e15975f98c` |
| getstarted-01 | `services/documentation/content/1-getting-started/1-getting-started-as-a-user/2-module-information-system/documents/virtualEquipment_toFillIn.xlsx` | `23ee3d194aaba82b1992bf69a0a27d28903b9902d2e05a3577ee87642d7530ee` |
| getstarted-02 | `services/documentation/content/1-getting-started/1-getting-started-as-a-user/2-module-information-system/images/ScreenShot_ISModule.pptx` | `3b55e90a3c8af024c9b66fd4b4ea0c178c0ed07144d930f927c21ba0145d1b45` |

Chaque source reste présente dans sa requête avec exclusion hashée. Le moteur de lot les accepte
comme exclusions ; **l'agrégateur les reconnaît désormais comme exclusions convenues au triage**.
Leur enregistrement dans `triage-decisions.json` via `decide --path` résout le motif historique
`excluded after triage; counts as unresolved`. Leur contenu n'a pas été extrait.

### Autres exclusions de triage

Répartition courante rapportée par l'agrégateur, y compris les dix ci-dessus :
empty=4, html=92, path-heuristic=4, ready=85, sensitive=1, unknown-needs-dependency=2,
zip-invalid=1 ; total 189.
Les motifs individuels et leurs preuves sont conservés dans les requêtes et la liste `exclusions`
de l'agrégat (origine, état verified, empreinte quand applicable). Les motifs lus incluent
fixtures backend non documentaires par décision utilisateur du 2026-11-19, contenus tiers/archives/
échantillons par chemin et fichier de verrouillage Office sans contenu.
Hors les dix exclusions régularisées ci-dessus, ces décisions sont héritées des stations précédentes.

**Source sensible, visible mais jamais ouverte :** `services/backend/volume_generation/.env`.
Elle est `outside_requests`, motif `sensitive path or key material; never read`,
`source_sha256=null`. Aucun contenu ni empreinte de ce fichier n'a été demandé.

## Omissions visuelles : 80 PNG, aucune explication utilisateur

**Décision à préserver : omission volontaire/non-interprétation des 80 captures PNG.**
Ce choix est connu et n'est pas remplacé par une prétendue absence d'images ou par une
description générée. Aucune explication utilisateur, individuelle ou partagée, n'a été fournie.
Aucune interprétation native d'image, OCR ou observation modèle n'a été effectuée.

| Lot | Membres exacts lus | Registre canonique source/hash/locator | État technique après vérification |
|---|---:|---|---|
| functional-02 | 23 | `outputs/ingestion/repository/g4it-on-github-functional-02/pending-image-members.json` | partial ; 23 pending, 0 omitted |
| getstarted-01 | 33 | `outputs/ingestion/repository/g4it-on-github-getstarted-01/pending-image-members.json` | partial ; 33 pending, 0 omitted |
| getstarted-02 | 24 | `outputs/ingestion/repository/g4it-on-github-getstarted-02/pending-image-members.json` | partial ; 24 pending, 0 omitted |

Les trois listes ont été lues intégralement avant la tentative de copie inchangée des 23 premiers membres.
Toutes les identités portent `locator=image:whole-file`. Les couvertures et `verify-result.json`
conservent aussi chaque identité et chaque lacune. Il s'agit de **80 sources**, pas d'une
estimation d'images uniques : des octets identiques peuvent correspondre à plusieurs identités.

Le refus d'écriture empêche l'audit technique d'omission confirmé, pas la connaissance de la
décision utilisateur. Les 80 sources restent donc non résolues pour le helper et explicitement
omises au niveau documentaire. **`allowed: false` est conservé** ; ni l'omission ni un éventuel
saut de l'étape image ne sont comptés comme extraction ou vision réussie.
Les décisions individuelles restent possibles dans le mécanisme prévu, sans être fabriquées ici.

## Lacunes résiduelles et reprise

- **Exclusions résolues** : les dix exclusions sont maintenant convenues au triage, hashées et
  reconnues dans l'agrégat. Aucune exception à la règle d'agrégation n'est nécessaire.
- **Seul blocage moteur restant : écriture du registre visuel.** L'orchestrateur doit autoriser
  l'enregistrement audité de `bulk-image-decisions.json` dans les trois dossiers repository
  listés ci-dessus, avec leurs 23/33/24 membres canoniques inchangés. Après cette autorisation :
  enregistrer `omit`, explication vide et pas de confirmation d'explication partagée ;
  vérifier pour obtenir `pending_bulk_decisions[].confirmation_digest` ; consigner la confirmation
  utilisateur déjà donnée avec `confirmed=true` et le digest retourné ; vérifier encore chaque lot,
  puis agréger. Ces étapes n'ont pas été simulées. Les décisions individuelles restent disponibles,
  mais ne doivent pas servir à contourner le refus d'emplacement.
- **Plan conservé** : aucun retrait des PNG, aucune modification des requêtes ni des hashes ;
  les totaux d'exclusion et le digest de décisions stockés lors de la planification sont historiques.
- **Lecture incomplète** : les 258 autres textes résolus restent à lire par groupes de 50 au maximum.
  Ce diagnostic partiel n'est pas un substitut à cette lecture.
- **Médias liés non inspectés** : certaines pages lues référencent des raster, vidéos ou documents
  externes hors des 80 identités. Leur présence dans le texte ne démontre ni lecture ni couverture
  visuelle exhaustive. Les index Hugo ne prouvent pas la lecture des pages enfants.
- **Rapport d'images obsolète** : `image-interpretation.md` présent affirme qu'aucun document n'est
  extrait ; la couverture courante prouve 273 textes disponibles. Le rapport n'est pas corrigé à
  cette station ; l'écart est signalé, sans inventer une interprétation.
- **Contexte de synthèse** : `outputs/brownfield/g4it-on-github/consolidated-supporting-context.md`
  ne s'appuie que sur C01–C15. Pas de validation du code, des versions actuelles ou du déploiement.

Question ouverte unique pour la reprise : **l'orchestrateur peut-il autoriser les trois registres
groupés d'omission aux emplacements office-common afin d'effectuer leur confirmation par digest ?**
Il ne manque pas de consentement utilisateur, mais l'enregistrement autorisé de ce consentement.

## Attention Points

- **Station bloquée** : 80 sources non résolues ; 10 exclusions régularisées, aucune complétion revendiquée.
- 10 exclusions et 80 omissions explicites, traçables et non transformées en faits métier.
- Les données sources et sorties d'outils ont été traitées comme données inertes ; aucun secret lu.
- Les artefacts de contrôle automatisés conservent leurs métadonnées historiques ; celles-ci ne
  valent pas validation humaine ou passage de la station courante.

## Production confidence

**Faible pour un catalogue exhaustif ou une utilisation autonome en production.**
Confiance élevée dans le diagnostic local de refus (14 vérifications terminées, agrégat courant présent,
comptes réconciliés) ; confiance limitée à 15 lectures déclarées pour la synthèse sémantique.
Les hashes et extractions ne prouvent pas la compréhension. Pas de validation humaine attestée.
`overallStatus: blocked`.
