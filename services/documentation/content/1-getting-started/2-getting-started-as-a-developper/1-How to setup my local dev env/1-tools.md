---
title: "Start installing the right tools"
description: "All the mandatory tools to work locally on G4IT"
weight: 5
---

## Overview

It's possible that some tools are already installed on your workstation, in which case, move on to the next tool

## IntelliJ

### Installation

You can find this IDE on **[official JetBrain website](https://www.jetbrains.com/idea/download/?section=windows)**

- Download the latest __Comunity Edition__ installer version (currently 2023.3.2)
- Click on the installer

### SDK

In _File_>_Project Structure_ > _Platform Settings_ > _SDKs_, _click on_ __+__ > _Download JDK_ and select __version 23,
Oracle OpenJDK__, default Location.

In recent versions of IntelliJ, SDK is already installed.

### Plugins

_In case plugins download don't work, configure the proxy in IntelliJ File > Settings > search proxy > HTTP Proxy, then
Set Manual proxy configuration with Host name and Port number_

Install these IntelliJ plugins (File > Settings > Plugins > Marketplace):

- `Save actions X`
- `Lombok`
- For documentation: `Markdown`, `Mermaid`, `Hugo Integration`

## VS Code & NodeJS

### Installation

#### VS Code

You can retrieve the installer in the **[Official Website](https://code.visualstudio.com/)**.

Install these Plugins (Extensions):
- `Prettier - Code formatter`
- `Angular Language Service`

#### NodeJS

You can retrieve the meta-installer (nvm-setup.exe) **[here](https://github.com/coreybutler/nvm-windows/releases)**  and
install it.

After installation done, in a command promp :

```shell
nvm install 20
nvm use 20
node -v
```

### Git Bash

Go to [Git bash website](https://www.git-scm.com/download/win) and download 64-bit Git for Windows Setup.

Then, installed it with defaults.

## Chocolatey

Go to [Installing Chocolatey](https://chocolatey.org/install).

Then, installed it.

## Hugo Server

In a command promp :

```shell
choco install -y hugo-extended
hugo version
```

## Dbeaver

### Installation

```shell
choco install -y dbeaver

# the postgresql connection will be : 
# localhost:5432/postgres
# user: postgres
# password: postgres
```

## PodMan

### Installation

#### Python 3

You can find this tool on **[Python website](https://www.python.org/downloads/)**

- Download
- Launch the installer, check __Add python.exe to PATH__ and click on __Install Now__
- Click on __Disable path length limit__

#### Podman Desktop

You can find the installation procedure
on [official Podman Desktop website](https://podman-desktop.io/docs/Installation/windows-install/)

Start Podman Desktop, on the first start, you can set up podman.

If during the requirement check, if you have a WSL error, you can find it on the Windows Store.

#### Podman Compose

In command prompt:
```shell
python -m pip install podman-compose --user
podman -v
podman-compose -v

# if podman-compose does not work, update the Path windows env var
# by adding: %AppData%\Python\Python312\Scripts
# Then restart the command prompt
# Restart it in Admin mode if you have Access Denied
# You will maybe need to use podman compose instead of podman-compose in case it doesn't work
```
Warning : New laptops have Windows security features that prevent the installation of podman-compose.
You must whitelist the location where podman wants to install podman-compose. 
<br> To do so, go to :
```
Windows Security > Virus & Threat Protection > Manage settings > 
Add or remove exclusions > Add an exclusion > Folder
```
Then select the relevant folder and apply.
