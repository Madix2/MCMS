# WildFly Messaging

The current MCMS deployment uses the transactional PostgreSQL email outbox and
scheduled EJB worker because the cloud WildFly instance does not currently have
an Artemis `messaging-activemq` subsystem configured. Do not deploy an MDB until
the queue exists.

After provisioning Artemis and authenticating to the WildFly CLI, run:

```bash
/opt/wildfly/wildfly/bin/jboss-cli.sh --connect \
  --file=/home/ubuntu/MCMS/deployment/wildfly/mcms-jms.cli
```

Then add and test an MDB in a separate deployment change. The queue must have a
dead-letter address, expiry address, durable delivery, and a transaction-aware
connection factory before replacing the current outbox worker.
