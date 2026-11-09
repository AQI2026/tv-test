#!/bin/sh
# 简易启动脚本：没有 gradle-wrapper.jar 时也能用 ./gradlew
GV=8.9
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
BASE="${HOME}/.gradle-bootstrap"
DIR="${BASE}/gradle-${GV}"
if [ ! -x "${DIR}/bin/gradle" ]; then
  mkdir -p "${BASE}"
  curl -fsSL -o "/tmp/gradle-${GV}.zip" "https://services.gradle.org/distributions/gradle-${GV}-bin.zip"
  unzip -q "/tmp/gradle-${GV}.zip" -d "${BASE}"
fi
exec "${DIR}/bin/gradle" "$@"
