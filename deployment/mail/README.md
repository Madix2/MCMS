# MCMS Send-Only Mail Host

This is an operational template for Ubuntu 22.04. Do not commit private DKIM
keys, SMTP credentials, or certificates.

## Postfix

```bash
sudo apt update
sudo apt install postfix mailutils opendkim opendkim-tools
sudo postconf -e 'myhostname = mail.example.com'
sudo postconf -e 'myorigin = $myhostname'
sudo postconf -e 'inet_interfaces = loopback-only'
sudo postconf -e 'inet_protocols = ipv4'
sudo postconf -e 'mydestination = $myhostname, localhost.$mydomain, localhost'
sudo postconf -e 'mynetworks = 127.0.0.0/8'
sudo postconf -e 'smtpd_relay_restrictions = permit_mynetworks, reject_unauth_destination'
sudo postconf -e 'smtpd_recipient_restrictions = permit_mynetworks, reject'
sudo postconf -e 'relay_domains ='
sudo postconf -e 'relayhost ='
sudo postconf -e 'smtp_tls_security_level = encrypt'
sudo systemctl enable --now postfix
```

Postfix must listen only on `127.0.0.1:25`. Verify with:

```bash
sudo ss -ltnp | grep ':25'
```

OCI security lists and UFW should not expose port 25 inbound.

## OpenDKIM

```bash
sudo mkdir -p /etc/opendkim/keys/example.com
sudo opendkim-genkey -b 2048 -d example.com \
  -D /etc/opendkim/keys/example.com -s mcms
sudo chown -R opendkim:opendkim /etc/opendkim/keys
sudo chmod 700 /etc/opendkim/keys
sudo chmod 600 /etc/opendkim/keys/example.com/mcms.private
```

Use `deployment/mail/opendkim.conf` as the configuration template. The public
key in `mcms.txt` must be published as the `mcms._domainkey` TXT record.

Postfix integration:

```bash
sudo postconf -e 'milter_protocol = 6'
sudo postconf -e 'milter_default_action = accept'
sudo postconf -e 'smtpd_milters = inet:127.0.0.1:8891'
sudo postconf -e 'non_smtpd_milters = inet:127.0.0.1:8891'
sudo systemctl restart opendkim postfix
```

Configure WildFly to submit locally instead of using Brevo:

```bash
/opt/wildfly/wildfly/bin/jboss-cli.sh --connect \
  --file=/home/ubuntu/MCMS/deployment/wildfly/mcms-direct-postfix.cli
```

Set `MCMS_MAIL_FROM` to an address in the authenticated sending domain, not a
free Gmail address. Test the complete path only after PTR, SPF, DKIM, and DMARC
are published and port 25 outbound is permitted by the cloud provider.

## DNS

Replace the placeholders with the real owned domain and public IP:

```text
example.com. TXT "v=spf1 ip4:PUBLIC_IP -all"
mcms._domainkey.example.com. TXT "v=DKIM1; k=rsa; p=PUBLIC_KEY"
_dmarc.example.com. TXT "v=DMARC1; p=none; rua=mailto:dmarc@example.com; adkim=s; aspf=s"
```

Set PTR/rDNS at the cloud provider to `mail.example.com`, and ensure the A
record points back to the same address. DuckDNS pools generally cannot provide
the dedicated PTR, reputation, or domain authentication required by major
mailbox providers. Brevo relay remains the safer production option.
