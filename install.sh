#!/bin/bash

set -e

APP_DIR="/var/www/mevastyle.it"
DOMAIN="mevastyle.it"
PORT=3050

echo "==== AGGIORNAMENTO SISTEMA E INSTALLAZIONE DIPENDENZE ===="
apt update && apt upgrade -y
apt install -y git curl build-essential nodejs npm apache2 libxml2-dev


echo "==== INSTALLAZIONE YARN GLOBAL ===="
npm install -g yarn

echo "==== CLONAZIONE REPO SE NON ESISTE ===="
if [ ! -d "$APP_DIR" ]; then
  git clone https://github.com/pasqualelembo78/mevastyle.git "$APP_DIR"
fi

cd "$APP_DIR"

echo "==== INSTALLAZIONE DIPENDENZE ===="
yarn install
yarn install:app

echo "==== COMPILAZIONE TYPESCRIPT ===="
yarn build

echo "==== CONFIGURAZIONE .env ===="
cp -n .env.example .env

# Imposta variabili ambiente se non presenti
grep -qxF "PAYLOAD_CONFIG_PATH=dist/payload.config.js" .env || echo "PAYLOAD_CONFIG_PATH=dist/payload.config.js" >> .env

echo "==== TERMINO EVENTUALI PROCESSI SULLA PORTA $PORT ===="
PID=$(lsof -t -i:$PORT || true)
[ ! -z "$PID" ] && kill -9 "$PID"

echo "==== AVVIO CON PM2 ===="
pm2 delete print-designer || true
pm2 start dist/server.js --name print-designer
pm2 save
pm2 startup systemd -u root --hp /root

echo "==== CONFIGURAZIONE APACHE ===="
cat > /etc/apache2/sites-available/${DOMAIN}.conf <<EOF
<VirtualHost *:80>
    ServerName $DOMAIN

    ProxyPreserveHost On
    ProxyRequests Off

    ProxyPass / http://localhost:${PORT}/
    ProxyPassReverse / http://localhost:${PORT}/

    ErrorLog \${APACHE_LOG_DIR}/${DOMAIN}_error.log
    CustomLog \${APACHE_LOG_DIR}/${DOMAIN}_access.log combined
</VirtualHost>

<IfModule mod_ssl.c>
<VirtualHost *:443>
    ServerName $DOMAIN
    ServerAlias www.$DOMAIN

    ProxyPreserveHost On
    ProxyRequests Off

    ProxyPass / http://localhost:${PORT}/
    ProxyPassReverse / http://localhost:${PORT}/

    ErrorLog \${APACHE_LOG_DIR}/${DOMAIN}_ssl_error.log
    CustomLog \${APACHE_LOG_DIR}/${DOMAIN}_ssl_access.log combined

    Include /etc/letsencrypt/options-ssl-apache.conf
    SSLCertificateFile /etc/letsencrypt/live/${DOMAIN}/fullchain.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/${DOMAIN}/privkey.pem
</VirtualHost>
</IfModule>
EOF

echo "==== ABILITAZIONE MODULI APACHE ===="
a2enmod proxy proxy_http ssl
a2ensite ${DOMAIN}.conf
systemctl restart apache2

echo "==== INSTALLAZIONE COMPLETATA CON SUCCESSO ===="
echo "Visita https://${DOMAIN}/admin per accedere al pannello."
