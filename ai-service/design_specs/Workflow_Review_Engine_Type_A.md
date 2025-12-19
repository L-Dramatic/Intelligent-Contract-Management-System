# Workflow Engine Specification: Type A (Construction Engineering)

**Version**: 1.0
**Target System**: Intelligent Contract Review Engine (ICRE)
**Domain**: Civil Engineering / Infrastructure Construction

---

## 1. Module 1: Pre-Flight Integrity Check (Structural)
**Objective**: Hard blocking validation. The contract cannot enter the review queue unless these documents are present.

### 1.1 Mandatory Attachment Checklist
The system must scan the uploaded file package for specific keywords in filenames or document headers.

| Required Document ID | Document Name Keyword (Regex) | Mandate Level | Action on Missing |
| :--- | :--- | :--- | :--- |
| **DOC_A_001** | `(安全生产协议\|Safety Responsibility Agreement)` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_A_002** | `(廉洁诚信\|Integrity Pact)` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_A_003** | `(农民工工资\|Migrant Worker Wage).*账户` | **CRITICAL** | **BLOCK SUBMISSION** |
| **DOC_A_004** | `(履约保证金\|Performance Bond)` | HIGH | Warning (User Confirmation Required) |

### 1.2 Signature Validation
*   **Rule**: Identify "Party B Signature" and "Party B Seal" areas.
*   **Logic**: If `Seal_Count == 0` OR `Signature_Count == 0`, Return `Error: Unsigned Draft`.

---

## 2. Module 2: Consistency & Logic Check (Data)
**Objective**: Extract structured data entities and perform cross-referencing validation.

### 2.1 Entity Extraction Schema
| Entity Key | Description | Data Type | Source |
| :--- | :--- | :--- | :--- |
| `contract_amt` | Total Contract Amount | Decimal | Main Contract Ch. 4 |
| `safe_prod_fee` | Safety Production Fee | Decimal | Main Contract Ch. 4 |
| `retainage_rate` | Quality Retainage Rate | Percentage | Main Contract |
| `project_duration` | Construction Period | Integer (Days) | Main Contract |
| `wage_account_no` | Wage Account Number | String | Attachment 12 |

### 2.2 Logic Validation Rules
```python
def validate_construction_logic(entities):
    errors = []
    
    # Rule 1: Safety Fee Ratio Check (Must be > 1.5% of Total)
    if entities['safe_prod_fee'] / entities['contract_amt'] < 0.015:
        errors.append("Safety Production Fee is below statutory 1.5%")

    # Rule 2: Retainage Rate Check (Must be exactly 3% for SOE standards)
    if entities['retainage_rate'] != 0.03:
        errors.append(f"Retainage Rate {entities['retainage_rate']} deviates from standard 3%")
        
    # Rule 3: Wage Account Existence
    if not entities['wage_account_no']:
        errors.append("Migrant Worker Wage Account Number is missing")

    return errors
```

---

## 3. Module 3: Legal & Compliance Risk Scan (Semantic)
**Objective**: Use NLP/RAG to detect forbidden clauses or high-risk terms specific to construction.

### 3.1 Risk Checkpoints
| Checkpoint ID | Risk Description | Keyword / Semantic Pattern | Risk Level |
| :--- | :--- | :--- | :--- |
| **RISK_A_001** | **Illegal Subcontracting** | `(转包\|分包给\|subcontract to)` | **HIGH (Blocker)** |
| **RISK_A_002** | **Safety Responsibility Shift** | `(甲方承担连带责任\|Party A bears joint liability)` | **HIGH (Blocker)** |
| **RISK_A_003** | **Missing Safety Veto** | Search for `(一票否决\|Veto Power)` in Safety Annex | MEDIUM |
| **RISK_A_004** | **Environmental Violation** | `(无需办理环评\|No need for EIA)` | HIGH |
| **RISK_A_005** | **Pre-payment Violation** | `(预付款\|Advance Payment)` > 30% | MEDIUM |
| **RISK_A_006** | **Undefined Warranty Period** | `(保修期\|Warranty Period)` missing or < 2 years | HIGH |
| **RISK_A_007** | **Audit Obstruction** | `(拒绝审计\|refuse audit)` | HIGH |
| **RISK_A_008** | **Price Adjustment** | `(市场价格波动\|market price fluctuation)` -> `(调价\|adjust price)` | MEDIUM |
| **RISK_A_009** | **Worker Insurance** | `(意外伤害险\|Accident Insurance)` missing | HIGH |
| **RISK_A_010** | **Dispute Resolution** | `(仲裁\|Arbitration)` instead of `(Court)` | LOW (Warning) |

---

## 4. Database Design Suggestion
**Table**: `review_results_type_a`

```json
{
  "contract_id": "CT_2024_001",
  "review_timestamp": "2024-12-20T10:00:00Z",
  "integrity_status": "PASS",
  "logic_check_results": {
    "safety_fee_ratio": 0.02,
    "retainage_rate": 0.03,
    "wage_account_valid": true
  },
  "risk_flags": [
    {
      "checkpoint_id": "RISK_A_001",
      "risk_level": "HIGH",
      "message": "Potential illegal subcontracting clause detected in Article 8.2",
      "source_text": "乙方可以将部分非主体工程转包给第三方...",
      "confidence_score": 0.95
    },
    {
      "checkpoint_id": "RISK_A_006",
      "risk_level": "HIGH",
      "message": "Warranty period not defined",
      "source_text": "NULL",
      "confidence_score": 1.0
    }
  ]
}
```

---

## 5. Frontend Interaction (UI/UX)

### 5.1 Dashboard Widget: "Construction Compliance Health"
*   **Integrity Meter**: Green (All Attachments) / Red (Missing).
*   **Risk Ticker**: Scroll list of detected semantic risks.

### 5.2 Error Presentation Strategy
*   **Blocking Errors (Red)**:
    *   Missing "Migrant Worker Wage Account".
    *   Clause: "Party A bears safety liability".
    *   *Action*: "Submit" button disabled. Tooltip: "Critical Compliance Violation".
*   **Warning Errors (Yellow)**:
    *   Low Safety Production Fee (e.g., 1.6% vs 2.0% target).
    *   Missing "Veto Power" explicit text (require manual check).
    *   *Action*: User must check "I acknowledge the risk" box to proceed.
