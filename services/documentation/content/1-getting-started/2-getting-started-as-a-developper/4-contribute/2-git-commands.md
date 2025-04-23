---
title: "Git Commands"
description: "Description of the Git Commands"
weight: 4
---

## Development

### Clone and refresh a repository

```
# Example for g4it repository
cd C:\G4IT\_repo\g4it

# Refresh main branch
git checkout main
git pull
```

### Feature branch (or fix)

```
# Create feature branch
git checkout -b feat/n
git push -u origin feat/n

# You can create the Merge Request

# Add, commit and push modification (loop)
git add .
git commit -m "[TRI] commit message"
git push
```

### N commits behing main

If the __main__ branch has evolved (n commits behind main)

```
# Your are on your feature branch
git checkout main;git pull;git checkout -;git merge main
# Resolve conflicts
# Then: Add, commit, push
```

### Task branch (US child task)

```
# Checkout US branch
git checkout feat/n
git pull

git checkout -b feat/n_m
git push -u origin feat/n_m

# You can create the Merge Request, pointing to feat/n

# Add, commit and push modification (loop)
git add .
git commit -m "[TRI] commit message"
git push
```

## Releases

### Create a release

Creating a release is very simple, just go to Gitlab, and create a tag with the new release.

Or by git commands (ex, tag=2.2.0):

```
git checkout main
git pull
git tag -a 2.2.0
git push origin 2.2.0
```

### Create a hotfix

Creating a hotfix is more difficult, because tags are not modifiable.

__Don't forget to report changes on the main branch !!!__
- If it's possible, correct the changes on the main branch first
- Then cherry-pick the commit with the command "git cherry-pick" like below
- You can find the commit id in the commits page, the commit that is NOT a Merge

 
Here are the commands (ex, tag=2.2.0, hotfix=2.2.1) :

```
git checkout main
git pull

git checkout -b hotfix/2.2.0 2.2.0
# If you need to cherry pick a commit (ex: bb8a527) to the hotfix branch
# git cherry-pick bb8a527
git push -u origin hotfix/2.2.0

# Verify the correction is OK (pipeline, install on integration) on branch hotfix/2.2.0, then create the hotfix tag
git tag -a 2.2.1
git push origin 2.2.1

# You can delete the temporary hotfix branch
git push -d origin hotfix/2.2.0
```
