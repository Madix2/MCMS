-- Cashier ownership is mandatory for every sale.
ALTER TABLE sale ADD COLUMN IF NOT EXISTS cashier_id BIGINT;

DO $$
DECLARE
    fallback_cashier BIGINT;
BEGIN
    SELECT id INTO fallback_cashier FROM app_user
    WHERE role IN ('CASHIER', 'SALES') ORDER BY id LIMIT 1;
    IF EXISTS (SELECT 1 FROM sale WHERE cashier_id IS NULL) AND fallback_cashier IS NULL THEN
        RAISE EXCEPTION 'Cannot backfill sale.cashier_id: no CASHIER or SALES user exists';
    END IF;
    UPDATE sale SET cashier_id = fallback_cashier WHERE cashier_id IS NULL;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_sale_cashier') THEN
        ALTER TABLE sale ADD CONSTRAINT fk_sale_cashier FOREIGN KEY (cashier_id) REFERENCES app_user(id);
    END IF;
END $$;

ALTER TABLE sale ALTER COLUMN cashier_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_sale_cashier ON sale (cashier_id);

ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_role_check;
ALTER TABLE app_user ADD CONSTRAINT app_user_role_check CHECK
    (role IN ('ADMIN', 'ADMINISTRATOR', 'MANAGER', 'CASHIER', 'SUPPLIER', 'SALES',
              'INVENTORY', 'PROCUREMENT', 'FINANCE', 'HR', 'MARKETING'));

UPDATE app_user SET role = 'CASHIER' WHERE username = 'sales2' AND role = 'SALES';
