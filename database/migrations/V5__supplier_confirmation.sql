ALTER TABLE purchase_order ADD COLUMN IF NOT EXISTS confirmation_token VARCHAR(512);
ALTER TABLE purchase_order ADD COLUMN IF NOT EXISTS token_expiry_timestamp TIMESTAMP;
CREATE UNIQUE INDEX IF NOT EXISTS uq_purchase_order_confirmation_token
    ON purchase_order (confirmation_token) WHERE confirmation_token IS NOT NULL;
ALTER TABLE purchase_order DROP CONSTRAINT IF EXISTS ck_purchase_order_status;
ALTER TABLE purchase_order ADD CONSTRAINT ck_purchase_order_status CHECK
    (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'COMMUNICATED_TO_SUPPLIER', 'ACKNOWLEDGED',
                'PARTIALLY_RECEIVED', 'COMPLETED', 'REJECTED', 'ORDERED', 'RECEIVED', 'CANCELLED',
                'SHIPPED', 'DELAYED'));
