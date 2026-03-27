Dear Chaminda,

Thank you for your email. 

Please find below the requested VM details along with some additional context that we believe is important for planning the migration and WAF/network configuration.

## **Current Status Overview**

We currently have three actively running production systems:

| Application | VM Name | IP Address | Status |
| :---- | :---- | :---- | :---- |
| CloudHIMS | cHIMS-HDC | 192.168.1.78 | Active (Production) |
| NCHIS | NCHIS-HDC | 192.168.1.72 | Active (Production) |
| ObjectDB *(CloudHIMS DB)* | ObjectDB Main DB | 192.168.1.155 | Active (Production) |

In addition, the following VMs have been provisioned, but not used yet, But they are part of our active migration plan:

| Application | VM Name | IP Address | Status |
| :---- | :---- | :---- | :---- |
| DMIS | DIMS | 192.168.1.100 | Provisioned – Setup in Progress |
| FMIS App Server | — | 192.168.1.49 | Provisioned – Setup in Progress |
| FMIS DB Server | — | 192.168.1.51 | Provisioned – Setup in Progress |
| ObjectDB Backup | ObjectDB Backup | 192.168.1.139 | Provisioned – Standby |

**Important Request:** We kindly request that **all of the above VMs be kept active and not decommissioned** during the new Data Centre setup. The provisioned VMs (DMIS, FMIS, ObjectDB Backup) are part of our planned migration away from NCHIS, and we will need them to remain accessible throughout the transition period.

---

## **VM Configurations**

### 1\. CloudHIMS

| Parameter | Details |
| :---- | :---- |
| VM Name | cHIMS-HDC |
| IP Address | 192.168.1.78 |
| CPU | 48 vCPUs (Intel Xeon Gold 6226 @ 2.70GHz) |
| RAM | 110 GB |
| Storage | 256 GB |
| OS | Ubuntu 20.04.6 LTS |

---

### 2\. NCHIS

| Parameter | Details |
| :---- | :---- |
| VM Name | NCHIS-HDC |
| IP Address | 192.168.1.72 |
| CPU | 16 vCPUs (Intel Xeon Gold 6226 @ 2.70GHz) |
| RAM | 58 GB |
| Storage | 400 GB |
| OS | Ubuntu 20.04.5 LTS |

**Note:** NCHIS is currently our most critical VM. In addition to the NCHIS application, both **DMIS** and **FMIS** are temporarily hosted on this same VM pending their migration to dedicated servers. This VM must remain protected and accessible at all times during the transition.

---

### 3\. DMIS *(Dedicated VM – Migration Pending)*

| Parameter | Details |
| :---- | :---- |
| VM Name | DIMS |
| IP Address | 192.168.1.100 |
| CPU | 4 vCPUs (Intel Xeon Gold 6226 @ 2.70GHz) |
| RAM | 15 GB |
| Storage | 200 GB |
| OS | Ubuntu 20.04.6 LTS |

**Note:** This is the dedicated VM for DMIS, currently being set up. DMIS will be migrated here from NCHIS. The WAF/network configuration for this VM should be prepared in anticipation of the upcoming migration.

---

### 4\. FMIS *(Dedicated VMs – Migration Pending)*

| Parameter | Details |
| :---- | :---- |
| App Server IP | 192.168.1.49 |
| DB Server IP | 192.168.1.51 |

**Note:** Dedicated VMs for FMIS have been allocated at the above IPs but are not currently powered on. FMIS is currently running within NCHIS and will be migrated to these dedicated servers. Specs will be confirmed once the VMs are commissioned. Please include these IPs in the WAF/network planning.

---

### 5\. ObjectDB – Main Database Server *(CloudHIMS)*

| Parameter | Details |
| :---- | :---- |
| VM Name | ObjectDB Main DB |
| IP Address | 192.168.1.155 |
| CPU | 32 vCPUs (Intel Xeon Gold 6226 @ 2.70GHz) |
| RAM | 90 GB |
| Storage | 300 GB |
| OS | Ubuntu 20.04.6 LTS |

**⚠ Urgent Note:** ObjectDB is a critical production server serving as the primary database for CloudHIMS. We are currently observing **84 GB of 90 GB RAM in use (approximately 93%)**, which represents a critical resource constraint. We strongly recommend this server be given the same level of priority as CloudHIMS in both WAF protection and migration planning.

---

### 6\. ObjectDB – Backup Server

| Parameter | Details |
| :---- | :---- |
| VM Name | ObjectDB Backup |
| IP Address | 192.168.1.139 |

**Note:** This is the standby/backup instance for ObjectDB. The VM is currently not powered on and we are unable to confirm the exact specifications at this time. Please ensure this VM is also retained and included in the network configuration review.

---

## **Summary Table**

| Application | VM Name | IP Address | vCPUs | RAM | Storage | Status |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| CloudHIMS | cHIMS-HDC | 192.168.1.78 | 48 | 110 GB | 256 GB | Active |
| NCHIS *(+ DMIS & FMIS temporarily)* | NCHIS-HDC | 192.168.1.72 | 16 | 58 GB | 400 GB | Active |
| DMIS (dedicated) | DIMS | 192.168.1.100 | 4 | 15 GB | 200 GB | Setup in Progress |
| FMIS App Server | — | 192.168.1.49 | — | — | — | Currently Off |
| FMIS DB Server | — | 192.168.1.51 | — | — | — | Currently Off |
| ObjectDB Main | ObjectDB Main DB | 192.168.1.155 | 32 | 90 GB | 300 GB | Active ⚠ |
| ObjectDB Backup | ObjectDB Backup | 192.168.1.139 | — | — | — | Currently Off |

---

We would appreciate your team's support in ensuring that all the above VMs are preserved and included in the WAF and network configuration review, including the provisioned VMs that are currently being set up. Decommissioning any of these during the transition period would impact our migration timeline.

Please do not hesitate to reach out if you need any further clarification or wish to schedule a joint session with your team.

Best regards, Buddhika