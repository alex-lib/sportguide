#!/usr/bin/env bash
# Called by GitHub Actions on the existing host; never starts another web server.
set -Eeuo pipefail
cd "$(dirname "$0")/../.."

site=/var/www/sportguide
config=/etc/nginx/sites-available/sportguide.online
backups=/var/backups/sportguide-static
exec 9>/run/lock/sportguide-static.lock
flock -n 9 || { echo 'Another static deployment is running' >&2; exit 1; }

restore() {
  # Keep newer hashed assets so already-open clients still work after rollback.
  tar -xzf "$1/site.tar.gz" -C "$site" --exclude=./index.html
  tar -xOzf "$1/site.tar.gz" ./index.html > "$site/.index.rollback.html"
  chmod 644 "$site/.index.rollback.html"
  mv -f "$site/.index.rollback.html" "$site/index.html"
  cp "$1/nginx.conf" "$config"
  nginx -t
  systemctl reload nginx
}

if [ "${1:-deploy}" = rollback ]; then
  backup_id=${2:?Pass the backup ID printed by the deployment}
  [[ "$backup_id" =~ ^[0-9]{8}T[0-9]{6}Z-[a-f0-9]{7,40}$ ]] || exit 2
  test -s "$backups/$backup_id/site.tar.gz"
  test -s "$backups/$backup_id/nginx.conf"
  restore "$backups/$backup_id"
  echo "Restored static site and Nginx from $backup_id (newer assets retained)."
  exit 0
fi
[ "${1:-deploy}" = deploy ] || exit 2

test -s frontend/dist/index.html
test -s frontend/dist/landing/index.html
test -s frontend/dist/landing/assets/js/app.js
test -f "$config"
test "$(readlink -f /etc/nginx/sites-enabled/sportguide.online)" = "$config"
nginx -t
# The inspected site is small; reserve room for backup and new assets.
available_kb=$(df -Pk "$site" | awk 'NR==2 {print $4}')
required_kb=$(( $(du -sk "$site" | cut -f1) + $(du -sk frontend/dist | cut -f1) + 102400 ))
[ "$available_kb" -gt "$required_kb" ] || { echo 'Insufficient disk reserve' >&2; exit 1; }

backup_id="$(date -u +%Y%m%dT%H%M%SZ)-$(git rev-parse --short HEAD)"
backup="$backups/$backup_id"
install -d -m 700 "$backup"
tar -czf "$backup/site.tar.gz" -C "$site" .
cp "$config" "$backup/nginx.conf"
echo "STATIC_BACKUP_ID=$backup_id"

trap 'status=$?; trap - ERR; echo "Deployment failed; restoring $backup_id" >&2; restore "$backup"; exit "$status"' ERR
# Publish dependencies first, entry point last. Retain old hashed app assets.
find frontend/dist -mindepth 1 -maxdepth 1 ! -name index.html -exec cp -R {} "$site/" \;
cp deploy/static/sportguide.online.conf "$config"
nginx -t
systemctl reload nginx
install -m 644 frontend/dist/index.html "$site/.index.next.html"
mv -f "$site/.index.next.html" "$site/index.html"

# The origin certificate is Cloudflare Origin CA, not a public browser certificate.
# -k is used ONLY for loopback origin checks; public HTTPS is checked with normal TLS.
origin=(--silent --show-error --connect-timeout 5 --max-time 15 --insecure --resolve sportguide.online:443:127.0.0.1)
[ "$(curl "${origin[@]}" -o /dev/null -w '%{http_code}' https://sportguide.online/landing)" = 308 ]
[ "$(curl "${origin[@]}" -o /dev/null -w '%{http_code}' https://sportguide.online/landing/)" = 200 ]
[ "$(curl "${origin[@]}" -o /dev/null -w '%{http_code}' https://sportguide.online/landing/missing.js)" = 404 ]
curl "${origin[@]}" -fI https://sportguide.online/landing/assets/js/app.js | grep -qi 'content-type: .*javascript'
public_html=$(curl --fail --silent --show-error --connect-timeout 5 --max-time 20 https://sportguide.online/landing/)
[[ "$public_html" == *'assets/js/app.js'* ]]
trap - ERR
echo "Static deployment succeeded. Rollback: bash deploy/static/publish.sh rollback $backup_id"
