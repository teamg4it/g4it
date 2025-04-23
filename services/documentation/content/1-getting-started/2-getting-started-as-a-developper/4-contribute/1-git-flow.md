---
title: "Git Flow"
description: "Description of the Git Flow"
mermaid: true
weight: 3
---

This page describe the git flows for Development and Release management.

The releated page [Git Commands]({{% ref 2-git-commands %}}) describes the commands.

## Development

Standard feature git flow (feat/n or fix/n) where n is the number of Jira task.

{{<mermaid>}}
gitGraph
    commit
    commit
    branch feat/n
    checkout feat/n
    commit id: "squash commits"
    checkout main
    merge feat/n
    commit
{{< /mermaid >}}

If the feature is too big, children branches can be created from feat/n, called feat/n_m

Example:
One US is called G4IT_BLM_100 and has 1 sub-task G4IT_BLM_101

{{<mermaid>}}
gitGraph
    commit
    commit
    branch feat/100
    checkout feat/100
    branch feat/100_101
    checkout feat/100_101
    commit id: "squash child"
    checkout feat/100
    merge feat/100_101
    commit id: "squash US"
    checkout main
    merge feat/100
    commit
{{< /mermaid >}}

## Releases

Creation of a Git Tag (from Gitlab UI), named for example 2.2.0.

The tag creation triggers __docker images build__.

Example of 2 releases (=2 tags):

{{<mermaid>}}
gitGraph
    commit
    commit
    commit
    branch "tag 2.2.0"
    commit
    checkout main
    commit
    commit
    commit
    branch "tag 2.3.0"
    commit
    checkout main
    commit
    commit
{{</mermaid>}}

The release x.x.x is then deployed as docker image on any target environment and does not change between environments.

## Hotfixes

In case of critial of blocking issue in __production__, a new hotfixed version can be created.

Imagine, the production environment has the __2.2.0__ version.

{{<mermaid>}}
gitGraph
    commit
    commit id: "tag commit"
    branch "hotfix/2.2.0"
    commit
    commit
    commit
    branch "tag 2.2.1"
    commit id: "create tag"
{{</mermaid>}}




