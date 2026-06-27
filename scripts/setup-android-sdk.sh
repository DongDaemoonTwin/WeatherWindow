#!/usr/bin/env bash
set -euo pipefail

# Installs the Android command line SDK into Codespaces or a lightweight Linux dev box.
# The project does not commit local.properties, so this script also writes it locally.

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-${HOME}/android-sdk}"
CMDLINE_TOOLS_URL="${CMDLINE_TOOLS_URL:-https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip}"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools"

if [ ! -x "${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "Installing Android command line tools to ${ANDROID_SDK_ROOT}..."
  TMP_DIR="$(mktemp -d)"
  curl -fsSL "${CMDLINE_TOOLS_URL}" -o "${TMP_DIR}/commandlinetools.zip"
  unzip -q "${TMP_DIR}/commandlinetools.zip" -d "${TMP_DIR}"
  rm -rf "${ANDROID_SDK_ROOT}/cmdline-tools/latest"
  mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools/latest"
  mv "${TMP_DIR}/cmdline-tools"/* "${ANDROID_SDK_ROOT}/cmdline-tools/latest/"
  rm -rf "${TMP_DIR}"
fi

export ANDROID_HOME="${ANDROID_SDK_ROOT}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT}"
export PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"

SDKMANAGER="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager"

echo "Accepting Android SDK licenses..."
yes | "${SDKMANAGER}" --licenses >/dev/null || true

echo "Installing Android SDK packages..."
"${SDKMANAGER}" \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;35.0.0"

cat > "${PROJECT_ROOT}/local.properties" <<EOF
sdk.dir=${ANDROID_SDK_ROOT}
EOF

BASHRC="${HOME}/.bashrc"
if ! grep -q "WeatherWindow Android SDK" "${BASHRC}" 2>/dev/null; then
  cat >> "${BASHRC}" <<EOF

# WeatherWindow Android SDK
export ANDROID_HOME=${ANDROID_SDK_ROOT}
export ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT}
export PATH=\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$ANDROID_SDK_ROOT/platform-tools:\$PATH
EOF
fi

echo "Android SDK ready. local.properties written to ${PROJECT_ROOT}/local.properties"
