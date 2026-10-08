ALTER TABLE purchase_order ADD COLUMN IF NOT EXISTS progress_token VARCHAR(512);
ALTER TABLE purchase_order ADD COLUMN IF NOT EXISTS progress_token_expiry_timestamp TIMESTAMP;
CREATE UNIQUE INDEX IF NOT EXISTS uq_purchase_order_progress_token
    ON purchase_order (progress_token) WHERE progress_token IS NOT NULL;
