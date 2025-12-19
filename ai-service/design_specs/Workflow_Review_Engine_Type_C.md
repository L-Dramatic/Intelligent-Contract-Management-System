# Workflow Engine Specification: Type C (IT Services & Software)

**Version**: 1.0
**Target System**: Intelligent Contract Review Engine (ICRE)
**Domain**: Software Development / System Integration / DICT

---

## 1. Module 1: Pre-Flight Integrity Check (Structural)
**Objective**: Specialized check for Data Security and IP Compliance documents.

### 1.1 Mandatory Attachment Checklist
| Required Document ID | Document Name Keyword (Regex) | Mandate Level | Action on Missing |
| :--- | :--- | :--- | :--- |
| **DOC_C_001** | `(数据安全\|Data Security).*承诺` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_C_002** | `(开源\|Open Source).*承诺` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_C_003** | `(SLA\|服务等级)` | HIGH | Warning |
| **DOC_C_004** | `(报价\|Rate Card\|Price List)` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_C_005** | `(廉洁\|Integrity Pact)` | **CRITICAL** | **BLOCK SUBMISSION** |

---

## 2. Module 2: Consistency & Logic Check (Data)
**Objective**: Validate Rate Cards, Payment Triggers, and Payment logic.

### 2.1 Entity Extraction Schema
| Entity Key | Description | Data Type | Source |
| :--- | :--- | :--- | :--- |
| `dict_flag` | Is DICT Project? | Boolean | Scope / Metadata |
| `payment_mode` | Payment Clause Type | Enum (Standard/Back2Back) | Main Contract Ch. 5 |
| `rate_l5_arch` | Rate for Sr. Architect | Decimal | Annex 4 |
| `data_loc_clause` | Data Localization Clause | Boolean | Annex 2 |
| `uat_duration` | Stable Run Duration | Integer (Months) | Main Contract Ch. 2 |

### 2.2 Logic Validation Rules
```python
def validate_it_logic(entities):
    errors = []
    
    # Rule 1: Pay-when-Paid Enforcement (DICT Specific)
    if entities['dict_flag'] and entities['payment_mode'] != 'Back2Back':
        errors.append("DICT Project must use 'Pay-when-Paid' (Back-to-Back) payment terms")

    # Rule 2: UAT Duration Check (Must be >= 3 months)
    if entities['uat_duration'] < 3:
        errors.append(f"Stable Operation Period {entities['uat_duration']} months is less than required 3 months")

    # Rule 3: Data Localization Sanity
    if not entities['data_loc_clause']:
        errors.append("Explicit Data Localization Clause not found in Analysis")

    return errors
```

---

## 3. Module 3: Legal & Compliance Risk Scan (Semantic)
**Objective**: Deep scan for IP risks, Introduction of Liabilities, and Data Sovereignty.

### 3.1 Risk Checkpoints
| Checkpoint ID | Risk Description | Keyword / Semantic Pattern | Risk Level |
| :--- | :--- | :--- | :--- |
| **RISK_C_001** | **Data Export / Cross-border** | `(境外\|Cross-border\|Overseas)` + `(存储\|传输\|store\|transfer)` | **CRITICAL (Blocker)** |
| **RISK_C_002** | **Viral License Usage** | `(GPL\|AGPL\|SSPL)` | **CRITICAL (Blocker)** |
| **RISK_C_003** | **IP Ownership (Custom)** | `(甲方拥有\|Party A owns)` missing OR `(乙方拥有\|Party B owns)` present for Custom Code | **HIGH (Blocker)** |
| **RISK_C_004** | **Backdoor/Trojan** | `(免责\|Not liable)` for `(病毒\|漏洞\|Virus\|Bug)` | **HIGH (Blocker)** |
| **RISK_C_005** | **Audit Refusal** | `(拒绝\|Refuse)` + `(审计\|Audit)` | HIGH |
| **RISK_C_006** | **Acceptance criteria** | `(上线即验收\|Acceptance upon Go-Live)` (Violation of UAT+3 rule) | HIGH |
| **RISK_C_007** | **Third-party IP Indemnity** | Missing `(侵权赔偿\|Infringement Indemnity)` clause | MEDIUM |
| **RISK_C_008** | **Subcontracting Data** | `(分包商\|Subcontractor)` access data without consent | HIGH |
| **RISK_C_009** | **Source Code Delivery** | `(不提供源代码\|Source code not provided)` | HIGH |
| **RISK_C_010** | **Payment on Time** | `(按时付款\|Time-bound payment)` for DICT (Conflicts with Pay-when-Paid) | HIGH |

---

## 4. Database Design Suggestion
**Table**: `review_results_type_c`

```json
{
  "contract_id": "IT_2024_009",
  "project_type": "DICT",
  "security_scan": {
    "data_export_found": false,
    "gpl_license_found": true,
    "localization_confirmed": true
  },
  "risk_flags": [
    {
      "checkpoint_id": "RISK_C_002",
      "risk_level": "CRITICAL",
      "message": "Viral Open Source License (GPL) detected in Annex 3",
      "source_text": "本项目使用组件: ffmpeg (LGPL), linux-kernel (GPLv2)...",
      "confidence_score": 0.99
    },
    {
      "checkpoint_id": "RISK_C_010",
      "risk_level": "HIGH",
      "message": "Payment clause conflicts with DICT Pay-when-Paid policy",
      "source_text": "甲方应在收到发票后 30 日内付款...",
      "confidence_score": 0.90
    }
  ]
}
```

---

## 5. Frontend Interaction (UI/UX)

### 5.1 Dashboard Widget: "Data & IP Security Shield"
*   **IP Color Code**: Green (CMCC Owned) / Red (Vendor Owned).
*   **Data Border Control**: Map visual showing "Data Localized in China" (Green Lock icon).

### 5.2 Error Presentation Strategy
*   **Blocking Errors (Red)**:
    *   **"Data Export"** detected.
    *   **"GPL License"** detected.
    *   **"Vendor Owns Custom Code"** detected.
    *   *Action*: Immediate rejection. User must upload revised contract.
*   **Warning Errors (Yellow)**:
    *   Missing specific "Indemnity" boilerplates.
    *   Rate card slightly above average (requires financial approval).
    *   *Action*: "Request Legal Waiver" flow.
