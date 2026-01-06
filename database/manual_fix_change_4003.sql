-- Manually fix stuck change ID 4 (BG-20260107-4003)
-- This was approved via workflow but failed to apply due to the previous bug.

-- 1. Update Contract Change Status to 'Passed' (2)
UPDATE t_contract_change 
SET 
    status = 2,
    approved_at = NOW(),
    effective_at = NOW()
WHERE id = 4 AND status = 1;

-- 2. Apply changes to the Contract (ID 1 as seen in correct logs/UI)
-- Only run this if the contract amount is still the old value (1.00)
UPDATE t_contract 
SET 
    amount = 2.00,
    version = 'V2', -- Assuming previous was V1, manually bumping
    updated_at = NOW()
WHERE id = 1 AND amount = 1.00;
