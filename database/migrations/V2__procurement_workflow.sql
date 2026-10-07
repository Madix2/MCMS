-- PostgreSQL 14+. Safe to run repeatedly after the base MCMS schema exists.
CREATE TABLE IF NOT EXISTS supplier_product_map (
    product_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    supplier_sku VARCHAR(100) NOT NULL,
    unit_cost NUMERIC(12,2) NOT NULL CHECK (unit_cost > 0),
    delivery_lead_time_days INTEGER NOT NULL DEFAULT 3 CHECK (delivery_lead_time_days >= 0),
    CONSTRAINT pk_supplier_product_map PRIMARY KEY (product_id, supplier_id),
    CONSTRAINT fk_spm_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_spm_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id)
);

ALTER TABLE purchase_order ADD COLUMN IF NOT EXISTS approval_timestamp TIMESTAMP WITH TIME ZONE;
ALTER TABLE purchase_order ADD COLUMN IF NOT EXISTS approved_by BIGINT;
ALTER TABLE purchase_order DROP CONSTRAINT IF EXISTS purchase_order_status_check;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'purchase_order'
              AND column_name = 'approved_by' AND data_type <> 'bigint') THEN
        ALTER TABLE purchase_order ALTER COLUMN approved_by TYPE BIGINT
            USING CASE WHEN approved_by ~ '^[0-9]+$' THEN approved_by::BIGINT ELSE NULL END;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_purchase_order_approved_by') THEN
        ALTER TABLE purchase_order ADD CONSTRAINT fk_purchase_order_approved_by
            FOREIGN KEY (approved_by) REFERENCES app_user(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_purchase_order_status') THEN
        ALTER TABLE purchase_order ADD CONSTRAINT ck_purchase_order_status CHECK
            (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'COMMUNICATED_TO_SUPPLIER',
                        'PARTIALLY_RECEIVED', 'COMPLETED', 'REJECTED', 'ORDERED', 'RECEIVED', 'CANCELLED',
                        'SHIPPED', 'DELAYED'));
    END IF;
END $$;

ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS delta_diff JSONB;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS client_token_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_audit_log_delta_diff_gin ON audit_log USING GIN (delta_diff);

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS supplier_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_app_user_supplier') THEN
        ALTER TABLE app_user ADD CONSTRAINT fk_app_user_supplier
            FOREIGN KEY (supplier_id) REFERENCES supplier(id);
    END IF;
END $$;
