#!/bin/bash
set -euo pipefail

# --- CONFIGURAZIONE ---
DOMAIN="mevastyle.it"
WWW_DOMAIN="www.mevastyle.it"
DOC_ROOT="/var/www/${DOMAIN}/public_html"
APACHE_CONF="/etc/apache2/sites-available/${DOMAIN}.conf"
EMAIL="admin@${DOMAIN}"
# ----------------------

echo "[0/10] Installazione sito ${DOMAIN}"

echo "[1/10] Aggiornamento sistema..."
sudo apt update -y
sudo apt upgrade -y

echo "[2/10] Installazione Apache + Certbot..."
sudo apt install -y apache2 certbot python3-certbot-apache

echo "[3/10] Creazione cartella sito..."
sudo mkdir -p "$DOC_ROOT"
sudo chown -R www-data:www-data "/var/www/${DOMAIN}"
sudo chmod -R 755 "/var/www/${DOMAIN}"

cat > /tmp/index_mevastyle.html <<HTML
<!doctype html>
<html lang="it">
<head>
  <meta charset="utf-8">
  <title>mevastyle.it</title>
</head>
<body>
  <h1>Benvenuto su MevaStyle</h1>
  <p>Sito installato correttamente.</p>
</body>
</html>
HTML

sudo mv /tmp/index_mevastyle.html "${DOC_ROOT}/index.html"
sudo chown www-data:www-data "${DOC_ROOT}/index.html"

echo "[4/10] Configurazione Apache..."
if [ -f "$APACHE_CONF" ]; then
  sudo cp "$APACHE_CONF" "${APACHE_CONF}.bak.$(date +%s)"
fi

sudo tee "$APACHE_CONF" > /dev/null <<EOL
<VirtualHost *:80>
    ServerAdmin ${EMAIL}
    ServerName ${DOMAIN}
    ServerAlias ${WWW_DOMAIN}

    DocumentRoot ${DOC_ROOT}

    <Directory ${DOC_ROOT}>
        Options Indexes FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>

    ErrorLog \${APACHE_LOG_DIR}/${DOMAIN}_error.log
    CustomLog \${APACHE_LOG_DIR}/${DOMAIN}_access.log combined
</VirtualHost>
EOL

echo "[5/10] Abilitazione sito..."
sudo a2dissite 000-default.conf || true
sudo a2ensite "${DOMAIN}.conf"
sudo a2enmod rewrite ssl headers || true

echo "[6/10] Riavvio Apache..."
sudo systemctl reload apache2

echo "[7/10] Firewall (UFW)..."
if command -v ufw >/dev/null 2>&1; then
  sudo ufw allow 'Apache Full' || true
fi

echo "[8/10] Certificato SSL..."
sudo certbot --apache -d "$DOMAIN" -d "$WWW_DOMAIN" \
  --non-interactive --agree-tos -m "$EMAIL" --redirect || {
    echo "ERRORE Certbot: controlla DNS e porta 80/443"
}

echo "[9/10] Test rinnovo SSL..."
sudo certbot renew --dry-run || true

echo "[10/10] COMPLETATO"
echo "Sito: https://${DOMAIN}"
echo "Root: ${DOC_ROOT}"
