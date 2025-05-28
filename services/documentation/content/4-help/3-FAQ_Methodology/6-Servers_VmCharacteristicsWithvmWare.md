---
title: 'Collect VM characteristics with vmWare'
weight: 106
---

## Objective 
Retrieve the VM characteristics on vmWare to evaluate the impact of the Digital Service

## Steps in VmWare

### Step 1
Install the vmWare powershell client :
{{< highlight bash >}}
Install-Module -Name VMware.PowerCLI -Scope CurrentUser
{{< /highlight >}}

### Step 2
Connect to VMWare vCenter Server :
{{< highlight bash >}}
Connect-VIServer -Server <vCenter_Server_IP_or_FQDN> -User <username> -Password <password>
{{< /highlight >}}

### Step 3
Récupération des informations de VM:
{{< highlight bash >}}
Get-VM | Select-Object Name,
@{Name="vCPU";Expression={$_.NumCpu}},
@{Name="MemoryGB";Expression={$_.MemoryGB}},
@{Name="DiskSizeGB";Expression={[math]::Round(($_.HardDisks | Measure-Object -Property CapacityGB -Sum).Sum, 2)}}
{{< /highlight >}}

### Step 4
Export CSV
{{< highlight bash >}}
Get-VM | Select-Object Name,
@{Name="vCPU";Expression={$_.NumCpu}},
@{Name="MemoryGB";Expression={$_.MemoryGB}},
@{Name="DiskSizeGB";Expression={[math]::Round(($_.HardDisks | Measure-Object -Property CapacityGB -Sum).Sum, 2)}} |
Export-Csv -Path "VM_Info.csv" -NoTypeInformation
{{< /highlight >}}

## Steps in G4IT

### Prerequites
The **Environmental footprint of a virtual machine** is directly linked to the **environmental footprint of the underlying physical equipment** on which the virtual machine is hosted.

If you don’t know the characteristics of your physical equipment, we recommend you select the server as :
- **Shared server**, that allocate a portion of the impact assessed for the underlying physical equipment
- **Compute or Storage**, depending on the main object of your server (treatment or storage)
- **Server S, M or L** where the size of server whose vCPU is closest to the sum of the vCPUs of all VMs. If one server is not sufficient, distribute its VMs across several servers.

### Step 1
On G4IT Digital Service Module, click on Evaluate New Service
On the Non-Cloud Servers tab, click on the «Add Server » button
![Add Server](../images/6-AddServer.png)

### Step 2
Select Physical server as per the choices made in the prerequisites step
![Select Physical Server](../images/6-SelectPhysicalServer.png)

### Step 3
Add VM on each server and precise vCPU and Annual Operation time
![Add VM](../images/6-AddVM.png)
