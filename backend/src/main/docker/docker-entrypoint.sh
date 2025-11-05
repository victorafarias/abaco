#!/bin/bash
set -euo pipefail

sleep_time="${JHIPSTER_SLEEP:-0}"
echo "The application will start in ${sleep_time}s..."
sleep "${sleep_time}"

exec java ${JAVA_OPTS:-} -Djava.security.egd=file:/dev/./urandom -jar /app.war
