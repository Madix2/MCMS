# MegaMart Central Management System — Deployment & Testing Guide

Developed by **Red-Code Company LTD** for **MegaMart Retail (Pty) Ltd**.

- **Stack:** Jakarta EE 10 (JAX-RS + JPA + CDI), custom JWT auth, PostgreSQL
- **Deploy target:** Payara 6 / WildFly ~28+ (Jakarta EE 10)
- **Context root:** `/mcms` — browse to `http://localhost:8080/mcms/`
- **REST base path:** `/mcms/api`

---

## 1. Prerequisites

| Component | Version | Notes |
|-----------|---------|-------|
| JDK | 17+ | Project compiles with `--release 17`. Java 25 works too. |
| Maven | 3.9+ | Build tool |
| PostgreSQL | 14+ | Database |
| Payara 6 / WildFly | Jakarta EE 10 | Application server |
| PostgreSQL JDBC driver | 42.7.3 | Bundled in the WAR (runtime scope) |

---

## 2. Build

```powershell
# From the project root
& "C:\Users\kaemo\AppData\Local\Maven\bin\mvn.cmd" clean package
```

Produce a `target/mcms.war`.

> **Note:** If the build reports `Could not transfer artifact ... Connection reset`, it is a transient
> network failure downloading plugins. Simply re-run `mvn package` — Maven resumes where it stopped.

---

## 3. Database Setup (PostgreSQL)

```sql
CREATE USER mcms WITH PASSWORD 'mcmspassword';
CREATE DATABASE mcms OWNER mcms;
GRANT ALL PRIVILEGES ON DATABASE mcms TO mcms;
```

No tables need to be created manually — Hibernate runs with
`hibernate.hbm2ddl.auto=update` and creates/updates the schema automatically on
first deploy. The `SeedDataService` (`@Startup`) seeds demo data on first run.

---

## 4. Application Server Setup

The app expects a JTA datasource bound to **`java:/jdbc/mcms`** (see
`src/main/resources/META-INF/persistence.xml`, persistence unit `MCMS-PU`).

### Payara 6 (recommended)

Either set the datasource via the admin console (`Resources → JDBC`) or run:

```bash
asadmin create-jdbc-connection-pool \
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
  --restype javax.sql.DataSource \
  --property "user=mcms:password=mcmspassword:DatabaseName=mcms:ServerName=localhost:PortNumber=5432" \
  mcmsPool

asadmin create-jdbc-resource --connectionpoolid mcmsPool jdbc/mcms
```

The `create-jdbc-resource` name `jdbc/mcms` is bound to JNDI
`java:/jdbc/mcms` on Payara.

### WildFly 29

Add a datasource in `standalone.xml` (or via the CLI):

```
/subsystem=datasources/data-source=mcmsDS:add(
  jndi-name="java:/jdbc/mcms",
  driver-name="postgresql",
  connection-url="jdbc:postgresql://localhost:5432/mcms",
  user-name="mcms",
  password="mcmspassword"
)
/subsystem=datasources/jdbc-driver=postgresql:add(
  driver-name="postgresql",
  driver-module-name="org.postgresql"
)
```

> On Payara the PostgreSQL driver bundled in the WAR is sufficient. On WildFly you
> may need to install the driver as a module.

---

## 5. Deploy the WAR

Copy `target/mcms.war` into the server's autodeploy folder, or use the admin
console / CLI. On first boot the schema is created and demo data is seeded.

Browse to **`http://localhost:8080/mcms/`**.

### JWT secret (optional but recommended)

The JWT is signed with the env var `MCMS_JWT_SECRET` (falls back to a dev
secret if unset). Set it in the server environment for production:

```powershell
$env:MCMS_JWT_SECRET = "a-long-random-secret-value"
```

---

## 6. Demo Credentials

All passwords are bcrypt-hashed and seeded on first run.

| Role | Username | Password |
|------|----------|----------|
| Administrator | `admin` | `admin123` |
| Manager | `manager` | `manager123` |
| Sales | `sales` | `sales123` |
| Inventory | `inventory` | `inventory123` |
| Procurement | `procurement` | `procurement123` |
| Finance | `finance` | `finance123` |
| HR | `hr` | `hr123` |
| Marketing | `marketing` | `marketing123` |

---

## 7. End-to-End Demo Scenario

This walks through the integrated purchase-order workflow across modules.

1. **Login as `sales`** → complete a POS sale:
   - Go to **Sales → New Sale**, add products, select a payment method, enter
     amount tendered, click **Complete Sale** → receipt shown.
2. **Inventory deduction:** the system automatically reduces product stock and
   records a `StockMovement` (check **Inventory → Stock Movements**).
3. **Low-stock alert:** if any product is now at/below its minimum, it appears in
   **Inventory → Low Stock**.
4. **Login as `procurement`** → **Procurement → Purchase Orders → + New PO**:
   - Pick a supplier, add the low-stock product, create the PO (status `DRAFT`).
   - Click **Submit** → status becomes `PENDING_APPROVAL`.
5. **Login as `manager`** → **Procurement → Approvals**:
   - **Approve** the PO (status → `APPROVED`) — managers/administrators only;
     other roles get a 403.
6. **Back as `procurement`** → on the PO, click **Receive** (status → `RECEIVED`):
   - Inventory is increased and a `StockMovement` recorded for each line.
7. **Login as `finance`** → **Finance Dashboard**: revenue and PO expenses appear.
8. **Login as `admin`** → **Dashboard** reflects updated totals; **Audit Logs**
   shows the recorded actions.

---

## 8. Quick API Smoke Test

```powershell
# Login
$body = '{"username":"admin","password":"admin123"}'
$r = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/mcms/api/auth/login" `
     -ContentType "application/json" -Body $body
$token = $r.token

# Authed call
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/mcms/api/dashboard/summary" `
     -Headers @{ Authorization = "Bearer $token" }
```

---

## 9. REST Endpoints (Base: `/mcms/api`)

| Area | Path | Notes |
|------|------|-------|
| Auth | `POST /auth/login`, `POST /auth/change-password` | login public |
| Dashboard | `GET /dashboard/summary`, `/top-products`, `/inventory-by-category`, `/revenue-trend`, `/activity`, `/notifications` | |
| Products | `GET/POST /products`, `PUT/DELETE /products/{id}`, `GET /products/low-stock`, `GET /products/{id}/movements`, `POST /products/{id}/adjust` | |
| Sales | `GET/POST /sales`, `GET /sales/{id}` | POST completes sale transactionally |
| Purchase Orders | `GET/POST /purchase-orders`, `POST /purchase-orders/{id}/submit`, `PUT .../{id}/approve`, `PUT .../{id}/reject`, `POST .../{id}/receive`, `GET /purchase-orders/pending-approval` | |
| Customers / Suppliers | `GET/POST` + `PUT/DELETE /{id}` | |
| Finance | `GET /finance/summary` | |
| HR | `GET/POST /hr/employees`, `/hr/attendance` | |
| Marketing | `/marketing/promotions`, `/campaigns`, `/social` | |
| Reports | `GET /reports/{...}` (JSON + CSV) | CSV download opens a new tab (no auth header — demo limitation) |
| Notifications / Audit | `GET /notifications`, `GET /audit` | |

Fine-grained role protection is enforced in the service layer via
`AuthContext.requireRole(...)`; `ADMIN` always passes. The `SecurityFilter`
enforces authentication only (401 for missing/invalid token).
