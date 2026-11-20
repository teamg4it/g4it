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
  - outputs/ingestion/repository/g4it-on-github-technical-01/source-coverage.json
  - services/documentation/content/_index.md
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
    change: "Contexte partiel fondé sur 15 lectures documentaires ; agrégation refusée (90 sources non résolues), sans modification du périmètre ni des décisions utilisateur."
  - date: "2026-11-20"
    change: "Reprise : 10 exclusions enregistrées par triage.py decide ; images.py handoff puis 14 vérifications et agrégation ; 80 PNG seuls non résolus, écriture des décisions groupées refusée. Les 15 textes ont été relus en groupes de 8 et 7."
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
      - "Skill lu intégralement : allowed exige tous les lots passed et aucune source unresolved ; seules les exclusions convenues au triage quittent le dénominateur."
  - path: .github/skills/office-common/SKILL.md
    key_facts:
      - "Skill lu intégralement : omission et explication passent par une décision confirmée liée aux membres et aux hashes ; une omission n'est pas une interprétation."
      - "Lecture des artefacts par groupes de 50 au maximum ; empreinte et extraction ne prouvent pas la compréhension."
  - path: .github/skills/ingestion-triage/SKILL.md
    key_facts:
      - "Skill lu intégralement : decide --path calcule les hashes et consigne une exclusion motivée ; aucune écriture manuelle des requêtes."
  - path: outputs/ingestion/g4it-on-github/triage-decisions.json
    key_facts:
      - "Les 10 XLSX/PPTX sont désormais exclude par décision explicite de chemin, motif utilisateur du 2026-11-20."
  - path: outputs/brownfield/g4it-on-github/extraction-verification.md
    key_facts:
      - "10 exclusions XLSX/PPTX post-triage, justifiées par la décision utilisateur du 2026-11-20."
      - "80 PNG, répartis en 23, 33 et 24 lacunes ; aucun échec textuel non visuel restant."
  - path: outputs/brownfield/g4it-on-github/image-interpretation.md
    key_facts:
      - "Aucune interprétation effectuée ; le texte affirmant l'absence de documents extraits est obsolète au regard des couvertures revérifiées."
  - path: outputs/ingestion/g4it-on-github/aggregate-coverage.json
    key_facts:
      - "allowed=false ; 542 découvertes, 189 exclusions de triage, 353 dans le périmètre, 273 résolues, 80 non résolues ; success_rate=0.7734."
      - "11 lots passed, 3 blocked, aucun running ; seules les 80 sources PNG partial restent non résolues."
  - path: .github/skills/office-common/images.py
    key_facts:
      - "Interface lue intégralement : export/apply/handoff uniquement ; apply vise les observations modèle, pas la création d'une omission utilisateur."
      - "handoff --analysis-id g4it-on-github a terminé avec allowed=true ; cela n'est pas la vérification finale."
  - path: services/documentation/content/3-technical-documentation/2-software_architecture/_index.md
    key_facts:
      - "SPA Angular, API REST Spring Boot, PostgreSQL, stockage de fichiers, Keycloak, NumEcoEval et Boavizta sont décrits dans le texte."
  - path: services/documentation/content/3-technical-documentation/3-technical_architecture/ecomind/_index.md
    key_facts:
      - "Le texte et les diagrammes Mermaid décrivent un appel asynchrone EvaluateAiService et un service Ecomind externe."
  - path: services/documentation/content/3-technical-documentation/1-Non_functional_requirements/_index.md
    key_facts:
      - "Les délais de 2 secondes pour une page et d'une minute pour chargement/estimation sont des exigences, pas des mesures."
  - path: services/documentation/content/2-functional-documentation/global_concepts/uc1_dataconsistency.md
    key_facts:
      - "Un indicateur non calculable ne signifie pas un impact nul ; les limites de périmètre doivent rester visibles."
  - path: outputs/ingestion/repository/g4it-on-github-technical-01/extracted/e3af7ba22d3cbe6c9552ad846bfd70cf5460c6686c55ed162fef73c272c50c6e.md
    key_facts:
      - "Copie UTF-8 d'un original UTF-16 LE, source_sha256=329b6e0f25cf562622098d8cce03c69b4f56468fa66cbac53cea97410a1c0c1d."
      - "Décrit ref_criterion, ref_lifecycle_step et ref_sustainable_individual_package."
---
<!-- AI_DISCLAIMER v1.0 -->
# Contexte documentaire consolidé — partiel et bloqué

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Périmètre et niveau de preuve

CONTEXT LOADED: trois skills lus intégralement, preuves de contrôle relues et 15 artefacts textuels relus ; manifeste ci-dessus.

Ce document est un **diagnostic partiel**, pas une consolidation exhaustive validée.
L'agrégateur refuse la station : `allowed: false`, code retour 1,
273/353 sources résolues par le moteur (77,34 %), 80 non résolues.
Ce taux ne mesure ni les lectures de cet agent ni la compréhension.

Lectures documentaires de cette reprise : **15/273 artefacts textuels**, en deux groupes de 8 puis 7,
dont 14 originaux Markdown et une copie
décodée. Les 258 autres ne sont pas attestés comme lus. Aucun pixel, enregistrement d'observation
visuelle ou document XLSX/PPTX exclu n'a été interprété. Les 15 références et attestations sont
identifiées C01–C15 dans `document-catalogue.md`. Les faits ci-dessous sont des affirmations
documentaires paraphrasées, non une vérification du code, du déploiement ou des performances.

## Finalité, domaines et vocabulaire

- **C01, C14** : la documentation présente G4IT comme un outil d'évaluation et de réduction de
  l'empreinte environnementale des systèmes d'information et des services numériques. Le guide
  propose évaluation, identification des principaux contributeurs, plan d'action et amélioration
  continue ; il distingue données réelles et hypothèses pour un service encore en conception.
- **C13** : le glossaire distingue organisation cliente, workspace compartimenté et inventaire
  réunissant datacenters, équipements physiques/virtuels et applications. Les administrateurs
  gèrent les accès ; les anciens termes organisation/subscriber sont signalés dans la source.
- **C04** : trois moteurs sont cités : NumEcoEval, Boavizta et EcoLogits ; ce dernier est associé
  aux services d'IA. Les méthodologies externes liées n'ont pas été consultées.

## Architecture décrite

- **C09, C10** : SPA Angular, backend REST Spring Boot, PostgreSQL et stockage de fichiers.
  Keycloak assure l'authentification avec délégation possible à un fournisseur d'identité.
  Les composants sont décrits comme conteneurisés, déployables via Docker ou Kubernetes.
  Les versions Angular 12/Spring Boot 3 sont celles du texte, non des versions installées vérifiées.
- **C09, C10** : cloisonnement par organisation/workspace ; le texte présente un contrôle
  logiciel des droits et, comme variante, des bases et stockages séparés par organisation.
  Cela ne prouve pas qu'une instance donnée utilise l'une ou l'autre stratégie.
- **C12** : interfaces et modèles API générés depuis OpenAPI ; organisation en controller,
  business, model/modeldb, repository et mapper. Les diagrammes Mermaid ont été lus comme texte,
  sans exécution ni rendu d'images.
- **C11** : la page Ecomind décrit configuration IA, calcul asynchrone et récupération de résultats
  et recommandations via le backend. Le service Ecomind est décrit comme externe. Aucun appel
  réseau n'a été réalisé et aucune topologie n'est inférée de captures non vues.
- **C07** : la documentation décrit un retour vers Keycloak lors d'un 401 lié à l'expiration
  du jeton, puis un retour à l'URL courante ; les autres 401 conduisent à une page d'erreur.

## Données, calculs et exigences

- **C05** : une impossibilité de produire un indicateur représente une partie non évaluée,
  pas un impact nul. Les causes documentées incluent référentiel incomplet, durée de vie absente
  et indisponibilité de données dans un moteur. Les tableaux comportementaux sont textuels ;
  les captures liées n'ont pas été inspectées.
- **C06, C15** : la notion d'équivalent-personne est rattachée aux limites planétaires et à un
  budget individuel soutenable. La copie décodée décrit les tables de critères, d'étapes du cycle
  de vie et de conversion en équivalent-personne. Aucune valeur de référentiel n'est validée.
- **C08** : les objectifs documentés sont une page en moins de 2 secondes, les appels synchrones
  backend en moins d'une seconde et le chargement/estimation d'un inventaire en moins d'une minute,
  sous les hypothèses de volumétrie de la page. Ce sont des exigences, **pas des résultats de test**.
  La section consommation de ressources y est incomplète.

## Lacunes et contradictions conservées

1. **10 XLSX/PPTX exclus, pas extraits** : décision utilisateur du 2026-11-20, motif conservé :
   `Excluded by user decision 2026-11-20: office-common parser dependencies unavailable; non-essential XLSX templates / PPTX screenshot decks`.
   Ils sont désormais reconnus comme exclusions convenues au triage par l'agrégateur, après
   `triage.py decide --path` (10 chemins dans un seul appel, hashes calculés par le helper).
   Ils ne comptent ni parmi les 353 sources en périmètre ni parmi les textes résolus. Leur contenu ne soutient
   aucun fait ci-dessus. Liste exacte, hashes et couvertures : catalogue, section des exclusions.
2. **80 PNG volontairement omis/non interprétés** : décision utilisateur conservée, aucune
   explication métier fournie. Répartition : functional-02=23, getstarted-01=33, getstarted-02=24.
   Les identités source/hash/locator ont été lues dans les trois `pending-image-members.json`.
   Les couvertures restent `partial`, avec `omitted=0` et `pending=80` au niveau du vérificateur.
   La nouvelle tentative d'écriture native du registre de functional-02 a été refusée par le contrôle d'emplacement
   des sorties ; aucun digest de confirmation ni succès d'omission technique n'est revendiqué.
3. Le rapport d'images existant affirme à tort qu'aucun document n'est extrait. Les vérifications
   actuelles montrent 272 lectures directes disponibles et une copie décodée. Le rapport n'a
   pas été réécrit, car cette station seule est exécutée.
4. Les schémas raster, captures, vidéos et liens externes cités par les textes ne sont pas des
   preuves visuelles lues. L'absence de lacune dans une couverture textuelle n'atteste pas ces médias.
5. Aucune lecture des secrets, installation, conversion supplémentaire, réextraction ou modification
   de l'état sous `outputs/runs/`. La source sensible reste hors requêtes et non lue. Le plan et les
   requêtes n'ont pas été réécrits : les 80 PNG sont conservés. Les compteurs et le digest de décisions
   du plan restent ceux de sa création ; l'agrégation courante utilise les décisions de triage actuelles.

## Reprise nécessaire

L'enregistrement manquant est `bulk-image-decisions.json` dans chacun des répertoires
`outputs/ingestion/repository/g4it-on-github-functional-02/`,
`outputs/ingestion/repository/g4it-on-github-getstarted-01/` et
`outputs/ingestion/repository/g4it-on-github-getstarted-02/`.
Le contrôle natif a refusé la première écriture : `write target is outside every running station's output location`,
avec pour emplacement attendu `outputs/brownfield/g4it-on-github/document-catalogue.md`.
Aucun registre groupé n'a été créé ; aucun digest n'a donc pu être confirmé.
L'orchestrateur doit permettre ce flux audité : copier les membres canoniques inchangés, `decision=omit`,
`explanation=""`, `shared_explanation_confirmed=false`, vérifier pour obtenir le digest,
enregistrer la confirmation utilisateur déjà donnée, puis vérifier de nouveau et agréger.
Ne pas utiliser `images.py apply` pour inventer des observations : aucune interprétation n'est demandée.
Le blocage des 10 exclusions est résolu ; aucune replanification supprimant des PNG n'est nécessaire.
Les décisions utilisateur ne sont pas à réinventer.
La lecture complète des 258 autres artefacts reste nécessaire avant toute consolidation exhaustive.

## Attention Points

- Station bloquée ; ne pas utiliser ce contexte comme preuve d'exhaustivité, de conformité ou d'état de production.
- Les 10 exclusions et 80 omissions ne constituent ni extraction ni compréhension métier.
- La décision d'omettre est connue ; son enregistrement technique confirmé est empêché, non assimilé à un consentement absent.
- Gate final conservé à false ; 11/14 lots passent, 80 PNG pending et aucun job en cours.

## Production confidence

**Faible pour un usage exhaustif en production.** Diagnostic chiffré fondé sur les vérifications
locales ; synthèse limitée aux 15 lectures déclarées. Aucune validation humaine ou exécution
applicative attestée. `overallStatus: blocked`.
