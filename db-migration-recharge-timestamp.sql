-- ================== RECHARGE TABLE MIGRATION ==================
-- Add created_at timestamp column to recharge table
-- Run this migration after rebuilding the RechargeProcessing service

-- Add the created_at column if it doesn't exist
ALTER TABLE recharge 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;

-- Set created_at for any existing recharges (optional: adjust based on actual creation time if available)
UPDATE recharge 
SET created_at = CURRENT_TIMESTAMP 
WHERE created_at IS NULL;

-- Add index on created_at for better query performance on dashboard charts
CREATE INDEX IF NOT EXISTS idx_recharge_created_at ON recharge(created_at);

-- Optional: Add an index on status and created_at together for common dashboard queries
CREATE INDEX IF NOT EXISTS idx_recharge_status_created ON recharge(status, created_at);
