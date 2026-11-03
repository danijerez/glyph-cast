#!/usr/bin/env sh
set -e
cd "$(dirname "$0")"

command -v node >/dev/null || { echo "Node.js 18 or newer is required: https://nodejs.org"; exit 1; }
for tool in java javac jar keytool; do
  command -v "$tool" >/dev/null || { echo "A JDK 17 or newer is required (missing $tool): https://adoptium.net"; exit 1; }
done
if [ "$(uname)" = "Linux" ]; then
  command -v unzip >/dev/null || { echo "unzip is required: install it with your package manager"; exit 1; }
fi

node build.mjs "$@"
echo
echo "APK ready: $(pwd)/bin/glyphcast.apk"
