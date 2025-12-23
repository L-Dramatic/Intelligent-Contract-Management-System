# Approval_Workflow_Engine_Design.md

## 1. Executive Summary
This document outlines the technical design for the **Intelligent Contract Approval Workflow Engine** specifically tailored for China Mobile's "City-Branch" structure. The core design philosophy adheres to the strict **Single Legal Entity** rule, where County branches serve as operational units ("Main Battle") without signing authority, and all contract approvals must escalate to the City Branch ("Management") level.

## Module 1: Role & Authority Mapping

### 1.1 Actor Definitions
The system distinguishes between **Initiators** (who can be at any level) and **Approvers** (who must be City-level).

*   **Initiator**:
    *   **Scope**: City Staff (Z8+) OR County Staff (Z8-Z9).
    *   **Role**: Can draft contract terms, upload attachments, and submit for review.
    *   **Restriction**: Cannot approve or sign.

*   **Approver**:
    *   **Scope**: **City-Level Roles ONLY**.
    *   **Validation**: System rejects any workflow configuration that assigns a County-level Z-node as a decision-maker.

### 1.2 Cross-Level Escalation Logic (The "County-to-City" Bridge)
To enforce the Single Legal Entity rule, a "Vertical Functional Mapping" table is mandated. This maps every County operational unit to its controlling City Department.

**Logic Rule:**
`Next_Approver = Map(Initiator_Line_of_Business, Target_Org_Level="CITY")`

**Example Scenario:**
*   **Context**: A "County Network Manager" (e.g., Head of Maintenance Center, Z11) initiates a fiber maintenance contract.
*   **Routing**:
    1.  **System Check**: Initiator Org Level = "County". Action = ESCALATE.
    2.  **Line of Business**: "Network/Maintenance".
    3.  **Lookup**: Find City-Level Parent Dept for "Network". -> "City Network Department".
    4.  **Target Role**: "General Manager" of that City Dept (Z12).
    5.  **Result**: Request routes strictly to **City Network Dept GM (Z12)**.

## Module 2: Dynamic Routing Algorithm (Pseudocode)

### 2.1 Algorithm Overview
The `get_approval_chain` function continuously calculates the next required node based on dynamic contract context (Amount, Type, Current Stage).

### 2.2 Pseudocode Implementation

```python
def get_approval_chain(contract_context):
    """
    Generates the list of required approval nodes based on CMS constraints.
    
    Args:
        contract_context (dict): Contains amount, type, initiator_info, etc.
    """
    chain = []
    
    # 1. Functional Dept Review (The "County-to-City" Escalation)
    # If Initiator is County, jump directly to City Functional Manager
    if contract_context.initiator_org_level == 'COUNTY':
        city_functional_mgr = get_city_counterpart(contract_context.initiator_dept)
        chain.append({
            "role": city_functional_mgr, 
            "z_level": "Z12",
            "step_name": "City Functional Dept Review"
        })
    else:
        # City Initiator -> Direct Manager
        chain.append({
            "role": "City_Dept_Manager", 
            "z_level": "Z12",
            "step_name": "Dept Manager Approval"
        })

    # 2. Mandatory Gate: Finance & Budget
    chain.append({
        "role": "City_Finance_Dept",
        "step_name": "Budget & Compliance Check"
    })

    # 3. Mandatory Gate: Legal
    chain.append({
        "role": "City_Legal_Dept",
        "step_name": "Legal Risk Review"
    })

    # 4. Authority Threshold Logic (Based on Table 2)
    amount = contract_context.total_amount_rmb
    
    if amount < 10000:
        # Final decision by Dept Manager (Already in chain, mark as Final if needed or add check)
        # In this logic, the specific financial approval might suffice, or Dept Mgr sign-off is final.
        pass # Flow ends at Functional + Finance/Legal checks

    elif 10000 <= amount <= 500000:
        # Requires City VP (Z13)
        chain.append({
            "role": "City_VP_in_Charge",
            "z_level": "Z13",
            "step_name": "City Vice General Manager Approval"
        })

    elif amount > 500000:
        # "Triple-One" High Value Logic
        chain.append({
            "role": "City_VP_in_Charge",
            "z_level": "Z13",
            "step_name": "City VP Review"
        })
        chain.append({
            "role": "General_Manager_Office_Meeting",
            "node_type": "COLLECTIVE_DECISION", # Requires Meeting Minutes upload
            "step_name": "GM Office Meeting Decision"
        })
        chain.append({
            "role": "City_General_Manager",
            "z_level": "Z15",
            "step_name": "City General Manager Veto/Sign-off"
        })

    return chain
```

## Module 3: Workflow Data Structure (JSON)

This JSON structure represents the state of a workflow instance, strictly enforcing City-level authority.

```json
{
  "workflow_id": "WF-20251220-CMCC-098",
  "contract_id": "CT-ZJ-HZ-2025-001",
  "current_state": "PENDING_APPROVAL",
  "initiator": {
    "user_id": "u8821",
    "role": "Network_Maintenance_Staff",
    "org_level": "COUNTY",
    "z_level": "Z9"
  },
  "approval_path": [
    {
      "step_id": 1,
      "step_type": "REVIEW",
      "step_name": "City Network Dept Manager Review",
      "assignee_role": "City_Network_Dept_GM",
      "target_org_level": "CITY",
      "required_z_level": "Z12",
      "status": "PENDING",
      "escalation_reason": "COUNTY_NO_SIGNING_AUTHORITY"
    },
    {
      "step_id": 2,
      "step_type": "GATE",
      "step_name": "Financial Budget Check",
      "assignee_role": "City_Finance_Manager",
      "target_org_level": "CITY",
      "required_z_level": "Z12",
      "status": "WAITING"
    },
    {
      "step_id": 3,
      "step_type": "DECISION",
      "step_name": "City VP Approval",
      "assignee_role": "City_VP_Network",
      "target_org_level": "CITY",
      "required_z_level": "Z13",
      "status": "WAITING",
      "condition": "AMOUNT_GT_10K"
    }
  ],
  "compliance_meta": {
    "is_triple_one": false,
    "legal_entity": "China Mobile Group [City] Branch"
  }
}
```

## Module 4: Post-Approval Actions

### 4.1 Seal Management (The "Physical" Lock)
The digital workflow generates a unique "Seal Authorization Code" only after the final node accepts.

*   **Task**: `Apply_Seal`
*   **Assignee**: **City Seal Administrator** (Fixed Role).
*   **Input**:
    1.  Printed Contract (from watermarked PDF).
    2.  Seal Authorization Code (System Generated).
*   **Action**: Admin verifies Code in system -> System shows "APPROVED" + "Hash of Document" -> Admin applies physical/electronic seal.

### 4.2 Archiving
*   **Physical**: Original signed paper copies must be stored in the **City Branch Archives**.
*   **Digital**:
    1.  Scanned copy of the sealed contract uploaded to the system.
    2.  System validates scan against original hash (OCR/Vision check optional).
    3.  Status marked as `ARCHIVED`.
    4.  Notification sent to County Initiator: "Contract Active".

---
**Design Note**: This architecture prioritizes **Compliance** over **Speed**, ensuring that no County-level personnel can inadvertently bind the company legally.
