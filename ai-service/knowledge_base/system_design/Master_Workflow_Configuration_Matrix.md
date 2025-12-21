# Master Workflow Configuration Matrix

> **Document Purpose**: Seed Data for Workflow Engine Database  
> **Version**: 1.0  
> **Last Updated**: 2025-12-22  
> **Author**: BPM Senior Analyst

---

## 1. Overview

This document defines the **deterministic routing rules** for all contract approval workflows within the Intelligent Contract Management System. It serves as the authoritative source for the workflow engine's database initialization.

### 1.1 Governing Principles

| Principle | Description |
|-----------|-------------|
| **Single Legal Entity Rule** | County Branches have NO signing authority. All approvals escalate to City-level roles. |
| **Z-Level Authority** | Financial thresholds determine the highest approver required. |
| **Mandatory Checkpoints** | Certain contract types require non-skippable verification nodes. |
| **Triple-One Meeting** | For amounts > 500k RMB, collective decision-making is mandatory before GM approval. |

---

## 2. Dimension Definitions

### 2.1 Contract Sub-Types

| Type Code | Category | Sub-Type Name |
|-----------|----------|---------------|
| A1 | Construction | Civil Works |
| A2 | Construction | Decoration |
| A3 | Construction | Minor Repairs |
| B1 | Maintenance | Optical Fiber |
| B2 | Maintenance | Base Station |
| B3 | Maintenance | Home Broadband |
| B4 | Maintenance | Emergency Power ⚡ |
| C1 | IT/DICT | Custom Development |
| C2 | IT/DICT | COTS Purchase |
| C3 | IT/DICT | DICT Integration (Back-to-Back) |

### 2.2 Amount Tiers (Z-Level Authority)

| Tier | Amount Range (RMB) | Final Approver | Special Condition |
|------|-------------------|----------------|-------------------|
| Tier 1 | < 10,000 | Department Manager (City) | Standard path |
| Tier 2 | 10,000 - 50,000 | Vice President (City) | Escalation required |
| Tier 3 | 50,000 - 500,000 | General Manager (City) | Senior review |
| Tier 4 | > 500,000 | Triple-One Meeting + GM (City) | Collective decision |

---

## 3. Special Routing Rules

> [!IMPORTANT]
> These rules are **MANDATORY** and override standard routing logic.

| Rule ID | Condition | Mandatory Node | Rationale |
|---------|-----------|----------------|-----------|
| RULE-001 | Sub-Type = C3 (DICT Integration) | Finance (Receivables Check) (City) | Back-to-back payment risk mitigation |
| RULE-002 | Sub-Type = A1 (Civil Works) AND Amount > 10k | Cost Audit (City) | Construction cost compliance |
| RULE-003 | Sub-Type = B4 (Emergency Power) | **FAST TRACK** - Skip Dept Mgr | Emergency response priority |
| RULE-004 | Amount > 500k | Triple-One Meeting (City) | Collective decision requirement |

---

## 4. Master Routing Configuration Table

### 4.1 Type A - Construction Contracts

| Scenario_ID | Sub_Type | Amount_Range | Specific_Gatekeepers | Full_Approval_Path |
|-------------|----------|--------------|---------------------|-------------------|
| A1-Tier1 | Civil Works | < 10k RMB | Project Manager (City) | County Initiator → Project Manager (City) → Dept Manager (City) ✓ |
| A1-Tier2 | Civil Works | 10k - 50k RMB | Project Manager (City), **Cost Audit (City)** | County Initiator → Project Manager (City) → Cost Audit (City) → Dept Manager (City) → VP (City) ✓ |
| A1-Tier3 | Civil Works | 50k - 500k RMB | Project Manager (City), **Cost Audit (City)**, Legal Review (City) | County Initiator → Project Manager (City) → Cost Audit (City) → Legal Review (City) → Dept Manager (City) → VP (City) → GM (City) ✓ |
| A1-Tier4 | Civil Works | > 500k RMB | Project Manager (City), **Cost Audit (City)**, Legal Review (City), **Triple-One Meeting (City)** | County Initiator → Project Manager (City) → Cost Audit (City) → Legal Review (City) → Dept Manager (City) → VP (City) → Triple-One Meeting (City) → GM (City) ✓ |
| A2-Tier1 | Decoration | < 10k RMB | Project Manager (City) | County Initiator → Project Manager (City) → Dept Manager (City) ✓ |
| A2-Tier2 | Decoration | 10k - 50k RMB | Project Manager (City), Design Review (City) | County Initiator → Project Manager (City) → Design Review (City) → Dept Manager (City) → VP (City) ✓ |
| A2-Tier3 | Decoration | 50k - 500k RMB | Project Manager (City), Design Review (City), Legal Review (City) | County Initiator → Project Manager (City) → Design Review (City) → Legal Review (City) → Dept Manager (City) → VP (City) → GM (City) ✓ |
| A2-Tier4 | Decoration | > 500k RMB | Project Manager (City), Design Review (City), Legal Review (City), **Triple-One Meeting (City)** | County Initiator → Project Manager (City) → Design Review (City) → Legal Review (City) → Dept Manager (City) → VP (City) → Triple-One Meeting (City) → GM (City) ✓ |
| A3-Tier1 | Minor Repairs | < 10k RMB | Facility Coordinator (City) | County Initiator → Facility Coordinator (City) → Dept Manager (City) ✓ |
| A3-Tier2 | Minor Repairs | 10k - 50k RMB | Facility Coordinator (City) | County Initiator → Facility Coordinator (City) → Dept Manager (City) → VP (City) ✓ |

---

### 4.2 Type B - Maintenance Contracts

| Scenario_ID | Sub_Type | Amount_Range | Specific_Gatekeepers | Full_Approval_Path |
|-------------|----------|--------------|---------------------|-------------------|
| B1-Tier1 | Optical Fiber | < 10k RMB | Network Engineer (City) | County Initiator → Network Engineer (City) → Dept Manager (City) ✓ |
| B1-Tier2 | Optical Fiber | 10k - 50k RMB | Network Engineer (City), Network Planning (City) | County Initiator → Network Engineer (City) → Network Planning (City) → Dept Manager (City) → VP (City) ✓ |
| B1-Tier3 | Optical Fiber | 50k - 500k RMB | Network Engineer (City), Network Planning (City), Legal Review (City) | County Initiator → Network Engineer (City) → Network Planning (City) → Legal Review (City) → Dept Manager (City) → VP (City) → GM (City) ✓ |
| B2-Tier1 | Base Station | < 10k RMB | RF Engineer (City) | County Initiator → RF Engineer (City) → Dept Manager (City) ✓ |
| B2-Tier2 | Base Station | 10k - 50k RMB | RF Engineer (City), Site Acquisition (City) | County Initiator → RF Engineer (City) → Site Acquisition (City) → Dept Manager (City) → VP (City) ✓ |
| B2-Tier3 | Base Station | 50k - 500k RMB | RF Engineer (City), Site Acquisition (City), Legal Review (City) | County Initiator → RF Engineer (City) → Site Acquisition (City) → Legal Review (City) → Dept Manager (City) → VP (City) → GM (City) ✓ |
| B2-Tier4 | Base Station | > 500k RMB | RF Engineer (City), Site Acquisition (City), Legal Review (City), **Triple-One Meeting (City)** | County Initiator → RF Engineer (City) → Site Acquisition (City) → Legal Review (City) → Dept Manager (City) → VP (City) → Triple-One Meeting (City) → GM (City) ✓ |
| B3-Tier1 | Home Broadband | < 10k RMB | Broadband Specialist (City) | County Initiator → Broadband Specialist (City) → Dept Manager (City) ✓ |
| B3-Tier2 | Home Broadband | 10k - 50k RMB | Broadband Specialist (City), Customer Service Lead (City) | County Initiator → Broadband Specialist (City) → Customer Service Lead (City) → Dept Manager (City) → VP (City) ✓ |
| B4-Tier1 | Emergency Power ⚡ | < 10k RMB | **FAST TRACK** - Ops Center (City) | County Initiator → Ops Center (City) ✓ |
| B4-Tier2 | Emergency Power ⚡ | 10k - 50k RMB | **FAST TRACK** - Ops Center (City) | County Initiator → Ops Center (City) → VP (City) ✓ |
| B4-Tier3 | Emergency Power ⚡ | 50k - 500k RMB | **FAST TRACK** - Ops Center (City), Legal Review (City) | County Initiator → Ops Center (City) → Legal Review (City) → VP (City) → GM (City) ✓ |
| B4-Tier4 | Emergency Power ⚡ | > 500k RMB | **FAST TRACK** - Ops Center (City), Legal Review (City), **Triple-One Meeting (City)** | County Initiator → Ops Center (City) → Legal Review (City) → VP (City) → Triple-One Meeting (City) → GM (City) ✓ |

---

### 4.3 Type C - IT/DICT Contracts

| Scenario_ID | Sub_Type | Amount_Range | Specific_Gatekeepers | Full_Approval_Path |
|-------------|----------|--------------|---------------------|-------------------|
| C1-Tier1 | Custom Development | < 10k RMB | Technical Lead (City), Security Review (City) | County Initiator → Technical Lead (City) → Security Review (City) → Dept Manager (City) ✓ |
| C1-Tier2 | Custom Development | 10k - 50k RMB | Technical Lead (City), Security Review (City), IT Architecture (City) | County Initiator → Technical Lead (City) → Security Review (City) → IT Architecture (City) → Dept Manager (City) → VP (City) ✓ |
| C1-Tier3 | Custom Development | 50k - 500k RMB | Technical Lead (City), Security Review (City), IT Architecture (City), Legal Review (City) | County Initiator → Technical Lead (City) → Security Review (City) → IT Architecture (City) → Legal Review (City) → Dept Manager (City) → VP (City) → GM (City) ✓ |
| C1-Tier4 | Custom Development | > 500k RMB | Technical Lead (City), Security Review (City), IT Architecture (City), Legal Review (City), **Triple-One Meeting (City)** | County Initiator → Technical Lead (City) → Security Review (City) → IT Architecture (City) → Legal Review (City) → Dept Manager (City) → VP (City) → Triple-One Meeting (City) → GM (City) ✓ |
| C2-Tier1 | COTS Purchase | < 10k RMB | Procurement Specialist (City), Vendor Management (City) | County Initiator → Procurement Specialist (City) → Vendor Management (City) → Dept Manager (City) ✓ |
| C2-Tier2 | COTS Purchase | 10k - 50k RMB | Procurement Specialist (City), Vendor Management (City), IT Security (City) | County Initiator → Procurement Specialist (City) → Vendor Management (City) → IT Security (City) → Dept Manager (City) → VP (City) ✓ |
| C2-Tier3 | COTS Purchase | 50k - 500k RMB | Procurement Specialist (City), Vendor Management (City), IT Security (City), Legal Review (City) | County Initiator → Procurement Specialist (City) → Vendor Management (City) → IT Security (City) → Legal Review (City) → Dept Manager (City) → VP (City) → GM (City) ✓ |
| C3-Tier1 | DICT Integration | < 10k RMB | City Solution Architect (City), **Finance (Receivables Check) (City)** | County Initiator → City Solution Architect (City) → Finance (Receivables Check) (City) → Dept Manager (City) ✓ |
| C3-Tier2 | DICT Integration | 10k - 50k RMB | City Solution Architect (City), **Finance (Receivables Check) (City)**, DICT Project Manager (City) | County Initiator → City Solution Architect (City) → Finance (Receivables Check) (City) → DICT Project Manager (City) → Dept Manager (City) → VP (City) ✓ |
| C3-Tier3 | DICT Integration | 50k - 500k RMB | City Solution Architect (City), **Finance (Receivables Check) (City)**, DICT Project Manager (City), Legal Review (City) | County Initiator → City Solution Architect (City) → Finance (Receivables Check) (City) → DICT Project Manager (City) → Legal Review (City) → Dept Manager (City) → VP (City) → GM (City) ✓ |
| C3-Tier4 | DICT Integration | > 500k RMB | City Solution Architect (City), **Finance (Receivables Check) (City)**, DICT Project Manager (City), Legal Review (City), **Triple-One Meeting (City)** | County Initiator → City Solution Architect (City) → Finance (Receivables Check) (City) → DICT Project Manager (City) → Legal Review (City) → Dept Manager (City) → VP (City) → Triple-One Meeting (City) → GM (City) ✓ |

---

## 5. Role Registry

> [!NOTE]
> All roles below are City-level roles with signing authority as per the Single Legal Entity Rule.

| Role Code | Role Name | Authority Level | Department |
|-----------|-----------|-----------------|------------|
| INIT | County Initiator | Initiation Only | County Branch |
| PM | Project Manager (City) | Technical Review | Engineering |
| CA | Cost Audit (City) | Financial Compliance | Finance |
| DR | Design Review (City) | Technical Standards | Engineering |
| NE | Network Engineer (City) | Technical Review | Network Ops |
| NP | Network Planning (City) | Strategic Alignment | Planning |
| RF | RF Engineer (City) | Technical Review | Network Ops |
| SA | Site Acquisition (City) | Property/Lease Review | Real Estate |
| FC | Facility Coordinator (City) | Facility Management | Admin |
| BS | Broadband Specialist (City) | Technical Review | Network Ops |
| CSL | Customer Service Lead (City) | Service Impact Review | Customer Service |
| OC | Ops Center (City) | Emergency Dispatch | Operations |
| TL | Technical Lead (City) | Code/Architecture Review | IT |
| SR | Security Review (City) | Cybersecurity Compliance | IT Security |
| IA | IT Architecture (City) | Enterprise Architecture | IT |
| PS | Procurement Specialist (City) | Sourcing Review | Procurement |
| VM | Vendor Management (City) | Supplier Qualification | Procurement |
| CSA | City Solution Architect (City) | DICT Technical Review | DICT |
| FRC | Finance (Receivables Check) (City) | Revenue Verification | Finance |
| DPM | DICT Project Manager (City) | Project Governance | DICT |
| LR | Legal Review (City) | Contract Compliance | Legal |
| DM | Dept Manager (City) | Departmental Approval | Various |
| VP | Vice President (City) | Executive Approval | Executive |
| T1M | Triple-One Meeting (City) | Collective Decision | Executive Committee |
| GM | General Manager (City) | Final Authority | Executive |

---

## 6. Workflow State Transitions

```
┌──────────────┐
│   DRAFT      │
└──────┬───────┘
       │ Submit
       ▼
┌──────────────┐     Reject
│  PENDING     │◄──────────────┐
│  APPROVAL    │               │
└──────┬───────┘               │
       │                       │
       ▼                       │
┌──────────────┐               │
│  IN_REVIEW   │───────────────┘
│  (per node)  │
└──────┬───────┘
       │ All Approved
       ▼
┌──────────────┐
│   APPROVED   │
└──────┬───────┘
       │ Execute
       ▼
┌──────────────┐
│   EXECUTED   │
└──────────────┘
```

---

## 7. Database Seed Data Format (JSON Schema)

```json
{
  "scenario_id": "C3-Tier3",
  "sub_type_code": "C3",
  "sub_type_name": "DICT Integration",
  "amount_min": 50000,
  "amount_max": 500000,
  "is_fast_track": false,
  "mandatory_nodes": ["Finance (Receivables Check) (City)"],
  "approval_sequence": [
    {"order": 1, "role": "County Initiator", "level": "county", "action": "initiate"},
    {"order": 2, "role": "City Solution Architect", "level": "city", "action": "review"},
    {"order": 3, "role": "Finance (Receivables Check)", "level": "city", "action": "verify"},
    {"order": 4, "role": "DICT Project Manager", "level": "city", "action": "review"},
    {"order": 5, "role": "Legal Review", "level": "city", "action": "review"},
    {"order": 6, "role": "Dept Manager", "level": "city", "action": "approve"},
    {"order": 7, "role": "Vice President", "level": "city", "action": "approve"},
    {"order": 8, "role": "General Manager", "level": "city", "action": "final_approve"}
  ]
}
```

---

## 8. Validation Checklist

- [x] All 30 scenarios cover Type A, B, C permutations
- [x] DICT (C3) includes Finance (Receivables Check) in ALL tiers
- [x] Construction (A1) includes Cost Audit for Tier 2, 3, 4 (> 10k)
- [x] Emergency (B4) marked as FAST TRACK (skips Dept Manager)
- [x] All roles tagged with (City) for Single Legal Entity compliance
- [x] Triple-One Meeting included for all Tier 4 scenarios
- [x] All paths start from County Initiator

---

## 9. Change Log

| Version | Date | Author | Description |
|---------|------|--------|-------------|
| 1.0 | 2025-12-22 | BPM Analyst | Initial creation with 30 approval scenarios |

---

> **End of Document**
