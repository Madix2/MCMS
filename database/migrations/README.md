# MCMS PostgreSQL Migrations

MCMS production schema changes are versioned in this directory and must be
applied in order with Flyway or an equivalent controlled migration runner.

The existing production database was originally created by the application and
there is no authoritative historical V1 DDL in this repository. Do not invent a
destructive `V1__Initial_Schema.sql` against that database. Baseline an existing
database at its current schema with Flyway, then apply `V2__...` onward:

```bash
flyway -url=jdbc:postgresql://127.0.0.1:5432/mcms \
  -user=mcms -password="$MCMS_DB_PASSWORD" \
  -locations=filesystem:database/migrations baseline
flyway -url=jdbc:postgresql://127.0.0.1:5432/mcms \
  -user=mcms -password="$MCMS_DB_PASSWORD" \
  -locations=filesystem:database/migrations migrate
```

For a new environment, create the initial schema from a reviewed database
bootstrap dump, register it as the organization's immutable V1 baseline, and
then run the numbered migrations. Hibernate schema generation is disabled in
`persistence.xml`; the application never creates, alters, or drops production
tables at startup.
