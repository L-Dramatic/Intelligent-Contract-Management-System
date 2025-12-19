# Workflow Engine Specification: Type B (Maintenance Services)

**Version**: 1.0
**Target System**: Intelligent Contract Review Engine (ICRE)
**Domain**: Network Operations / Facility Maintenance

---

## 1. Module 1: Pre-Flight Integrity Check (Structural)
**Objective**: Ensure the Framework + Order structure is complete and Maintenance-specific attachments are present.

### 1.1 Mandatory Attachment Checklist
| Required Document ID | Document Name Keyword (Regex) | Mandate Level | Action on Missing |
| :--- | :--- | :--- | :--- |
| **DOC_B_001** | `(技术规范书\|Technical Specification)` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_B_002** | `(考核细则\|Appraisal Rules)` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_B_003** | `(报价明细\|Quotation\|Price List)` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_B_004** | `(安全生产责任\|Safety Responsibility)` | **CRITICAL** | **BLOCK SUBMISSION** |

### 1.2 Resource Config Check
*   **Rule**: Verify "Annex 16: Resource Configuration" exists if `Contract_Type == "Labor Intensive"`.

---

## 2. Module 2: Consistency & Logic Check (Data)
**Objective**: Validate Framework Limits and SLA Parameters.

### 2.1 Entity Extraction Schema
| Entity Key | Description | Data Type | Source |
| :--- | :--- | :--- | :--- |
| `framework_cap` | Framework Agreement Upper Limit | Decimal | Main Framework |
| `po_sum` | Sum of Purchase Order Amounts | Decimal | Linked POs (DB) |
| `sla_avail_target` | Network Availability Target | Percentage | Annex 1/2 |
| `penalty_ratio` | Penalty Ratio per 0.1% drop | Percentage | Annex 2 |
| `unit_prices` | List of Unit Prices | Array<Object> | Annex 13 |

### 2.2 Logic Validation Rules
```python
def validate_maintenance_logic(entities, current_po_amount):
    errors = []
    
    # Rule 1: Cap Overflow Check
    if entities['po_sum'] + current_po_amount > entities['framework_cap']:
        errors.append(f"PO exceeds Framework Cap by {entities['po_sum'] + current_po_amount - entities['framework_cap']}")

    # Rule 2: SLA Target Sanity Check (Must be >= 99.5%)
    if entities['sla_avail_target'] < 0.995:
        errors.append("SLA Availability Target is abnormally low (< 99.5%)")

    # Rule 3: Penalty Mechanism Existence
    if entities['penalty_ratio'] <= 0:
        errors.append("No Penalty Ratio defined for SLA breach")

    return errors
```

---

## 3. Module 3: Legal & Compliance Risk Scan (Semantic)
**Objective**: Detect Operational Risks, Fraud Risks, and unfair clauses.

### 3.1 Risk Checkpoints
| Checkpoint ID | Risk Description | Keyword / Semantic Pattern | Risk Level |
| :--- | :--- | :--- | :--- |
| **RISK_B_001** | **Fake Records** | `(伪造\|falsify)` -> `(不予处罚\|no penalty)` | **HIGH (Blocker)** |
| **RISK_B_002** | **Timeout Penalty Missing** | `(故障\|Fault)` + `(超时\|Timeout)` -> No numeric penalty detected | HIGH |
| **RISK_B_003** | **Assessment Score Threshold** | `(全额付款\|Full Payment)` threshold < 90 points | MEDIUM |
| **RISK_B_004** | **Material Managment** | `(甲供材\|Party A Material)` -> `(乙方不承担\|Party B not liable)` | **HIGH (Blocker)** |
| **RISK_B_005** | **Safety Isolation** | `(甲方承担\|Party A bears)` + `(伤亡\|casualty)` | **HIGH (Blocker)** |
| **RISK_B_006** | **Subcontracting** | `(转包\|Subcontract)` allowed without written consent | HIGH |
| **RISK_B_007** | **Data Falsification** | `(代打卡\|GPS spoofing)` not mentioned as violation | MEDIUM |
| **RISK_B_008** | **Price Adjustment** | `(单价调整\|Price Adjustment)` allowed unilaterally | HIGH |
| **RISK_B_009** | **Vehicle Config** | `(车辆\|Vehicle)` configuration missing | MEDIUM |
| **RISK_B_010** | **Payment Term** | `(预付款\|Advance)` mentioned (Maintenance usually post-paid) | HIGH |

---

## 4. Database Design Suggestion
**Table**: `review_results_type_b`

```json
{
  "framework_id": "FW_2024_ZJ_001",
  "po_id": "PO_2024_03_005",
  "cap_status": {
    "limit": 10000000.00,
    "used": 8500000.00,
    "current_po": 200000.00,
    "remaining": 1300000.00
  },
  "risk_flags": [
    {
      "checkpoint_id": "RISK_B_004",
      "risk_level": "HIGH",
      "message": "Clause exculpating Party B from Party A Material loss found",
      "source_text": "对于甲供材料的丢失，乙方不承担赔偿责任。",
      "confidence_score": 0.98
    }
  ]
}
```

---

## 5. Frontend Interaction (UI/UX)

### 5.1 Dashboard Widget: "Cap Utilization Monitor"
*   **Visual**: Progress Bar showing "Used Amount" vs "Framework Cap".
*   **Alert**: Flashing Red if `Used + Current > 95% Cap`.

### 5.2 Error Presentation Strategy
*   **Blocking Errors (Red)**:
    *   Exceeding Framework Cap.
    *   Missing "Safety Isolation" clause (Party A bearing liability).
*   **Warning Errors (Yellow)**:
    *   SLA constraints too loose (e.g., 8h fix time for P1).
    *   Missing "GPS/Anti-fraud" clauses (Operational risk).
    *   *Action*: "Escalate to Manager" button required for approval.
