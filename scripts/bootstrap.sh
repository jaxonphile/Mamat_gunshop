#!/usr/bin/env bash
# Bootstrap Mamat Gunshop: cari JDK 25, unduh JDK portabel bila perlu,
# bangun fat JAR lewat Gradle Wrapper, lalu jalankan aplikasi.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CACHE="${HOME}/.mamatgunshop/runtime"
JAR="${ROOT}/build/libs/mamat-gunshop-1.0.0-all.jar"

log() { printf '[mamat] %s\n' "$*" >&2; }

java_major() {
  local exe="${1:-}"
  [[ -n "$exe" && -x "$exe" ]] || { echo 0; return; }
  local spec
  spec="$("$exe" -XshowSettings:properties -version 2>&1 \
    | awk -F'= ' '/java.specification.version/{print $2; exit}')"
  [[ -n "$spec" ]] || { echo 0; return; }
  local major="${spec%%.*}"
  if [[ "$major" == "1" ]]; then
    major="${spec#*.}"
    major="${major%%.*}"
  fi
  echo "$major"
}

find_java() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    echo "${JAVA_HOME}/bin/java"
    return
  fi
  if command -v java >/dev/null 2>&1; then
    command -v java
    return
  fi
  echo ""
}

sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | awk '{print $1}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$1" | awk '{print $1}'
  else
    log "Butuh sha256sum atau shasum untuk verifikasi unduhan."
    exit 1
  fi
}

install_jdk() {
  log "JDK 25 belum tersedia. Mengunduh Temurin 25 (~140 MB)..."
  mkdir -p "$CACHE"

  local os arch
  case "$(uname -s)" in
    Linux) os=linux ;;
    Darwin) os=mac ;;
    *) log "Sistem operasi ini belum didukung launcher otomatis."; exit 1 ;;
  esac
  case "$(uname -m)" in
    x86_64|amd64) arch=x64 ;;
    aarch64|arm64) arch=aarch64 ;;
    *) log "Arsitektur CPU ini belum didukung launcher otomatis."; exit 1 ;;
  esac

  local api="https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=${arch}&image_type=jdk&os=${os}&vendor=eclipse"
  local json block link checksum name
  json="$(curl -fsSL --retry 3 "$api")"
  block="$(printf '%s' "$json" | sed -n '/"package": {/,/}/p')"
  link="$(printf '%s' "$block" | grep -o '"link": "[^"]*"' | head -n1 | cut -d'"' -f4)"
  checksum="$(printf '%s' "$block" | grep -o '"checksum": "[^"]*"' | head -n1 | cut -d'"' -f4)"
  name="$(printf '%s' "$block" | grep -o '"name": "[^"]*"' | head -n1 | cut -d'"' -f4)"

  [[ -n "$link" && -n "$checksum" && -n "$name" ]] || {
    log "Gagal membaca metadata rilis JDK dari Adoptium."
    exit 1
  }

  local archive="${CACHE}/${name}"
  if [[ ! -f "$archive" ]]; then
    curl -fL --retry 3 -o "${archive}.part" "$link"
    mv "${archive}.part" "$archive"
  fi

  local actual
  actual="$(sha256_of "$archive")"
  if [[ "$actual" != "$checksum" ]]; then
    rm -f "$archive"
    log "Checksum JDK tidak cocok. Unduhan dibuang; silakan coba lagi."
    exit 1
  fi

  log "Checksum OK. Mengekstrak JDK..."
  rm -rf "${CACHE}/jdk-extract"
  mkdir -p "${CACHE}/jdk-extract"
  tar -xzf "$archive" -C "${CACHE}/jdk-extract"

  local jdk_dir
  jdk_dir="$(find "${CACHE}/jdk-extract" -maxdepth 1 -type d -name 'jdk-25*' | head -n1)"
  [[ -n "$jdk_dir" ]] || { log "Folder JDK tidak ditemukan setelah ekstraksi."; exit 1; }

  rm -rf "${CACHE}/jdk-25"
  mv "$jdk_dir" "${CACHE}/jdk-25"
  rm -rf "${CACHE}/jdk-extract"

  echo "${CACHE}/jdk-25/bin/java"
}

needs_build() {
  [[ ! -f "$JAR" ]] && return 0
  find "$ROOT/src" "$ROOT/build.gradle" "$ROOT/settings.gradle" "$ROOT/gradle" \
    -newer "$JAR" -print -quit 2>/dev/null | grep -q .
}

JAVA="$(find_java)"
MAJOR="$(java_major "$JAVA")"

if (( MAJOR < 25 )); then
  JAVA="$(install_jdk)"
  MAJOR="$(java_major "$JAVA")"
fi
if (( MAJOR < 25 )); then
  log "JDK hasil unduhan tidak bisa dipakai."
  exit 1
fi

export JAVA_HOME="$(cd "$(dirname "$(dirname "$JAVA")")" && pwd)"
log "JDK terdeteksi: ${MAJOR} (${JAVA_HOME})"

if [[ "${MAMAT_BOOTSTRAP_CHECK:-0}" == "1" ]]; then
  log "BOOTSTRAP OK — JDK=${MAJOR}, JAR=$([[ -f "$JAR" ]] && echo ada || echo 'akan dibangun')"
  exit 0
fi

if needs_build; then
  log "Menyiapkan Gradle + dependency (unduhan pertama bisa memakan waktu)..."
  "$ROOT/gradlew" -p "$ROOT" fatJar --console=plain >&2
fi

# Workaround resmi Niri/Wayland + xwayland-satellite (non-reparenting WM).
export _JAVA_AWT_WM_NONREPARENTING=1

log "Menjalankan Mamat Gunshop..."
exec "$JAVA" -jar "$JAR" "$@"
