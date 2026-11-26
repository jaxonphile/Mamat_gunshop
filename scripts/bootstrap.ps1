param(
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]]$AppArgs
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
$Cache = Join-Path $env:USERPROFILE '.mamatgunshop\runtime'
$Jar = Join-Path $Root 'build\libs\mamat-gunshop-1.0.0-all.jar'
$Wrapper = Join-Path $Root 'gradlew.bat'

function Write-Step {
  param([string]$Message)
  Write-Host "[mamat] $Message" -ForegroundColor Cyan
}

function Get-JavaExe {
  $cmd = Get-Command java -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }

  $candidates = @()
  if ($env:JAVA_HOME) {
    $candidates += Join-Path $env:JAVA_HOME 'bin\java.exe'
  }
  if ($env:ProgramFiles) {
    $candidates += Join-Path $env:ProgramFiles 'Eclipse Adoptium\jdk-25\bin\java.exe'
  }
  if (${env:ProgramFiles(x86)}) {
    $candidates += Join-Path ${env:ProgramFiles(x86)} 'Eclipse Adoptium\jdk-25\bin\java.exe'
  }
  foreach ($candidate in $candidates) {
    if ($candidate -and (Test-Path $candidate)) { return $candidate }
  }
  return $null
}

function Get-JavaMajor {
  param([string]$JavaExe)
  if (-not $JavaExe -or -not (Test-Path $JavaExe)) { return 0 }

  $line = & $JavaExe -XshowSettings:properties -version 2>&1 |
    Select-String 'java.specification.version' |
    Select-Object -First 1
  if (-not $line) { return 0 }

  $text = $line.ToString()
  $value = ($text -split '=', 2)[1].Trim()
  if ($value -like '1.*') {
    $value = ($value -split '\.')[1]
  } else {
    $value = ($value -split '\.')[0]
  }
  return [int]$value
}

function Install-Jdk {
  Write-Step 'JDK 25 belum tersedia. Mengunduh Temurin 25 (~140 MB)...'
  New-Item -ItemType Directory -Force -Path $Cache | Out-Null

  $arch = if ($env:PROCESSOR_ARCHITECTURE -like 'ARM64') { 'aarch64' } else { 'x64' }
  $api = "https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=$arch&image_type=jdk&os=windows&vendor=eclipse"
  $asset = Invoke-RestMethod -Uri $api -TimeoutSec 60 | Select-Object -First 1
  if (-not $asset) { throw 'Gagal membaca metadata rilis JDK dari Adoptium.' }

  $pkg = $asset.binary.package
  $link = $pkg.link
  $checksum = $pkg.checksum
  $name = $pkg.name
  if (-not $link -or -not $checksum -or -not $name) {
    throw 'Metadata JDK tidak lengkap.'
  }

  $archive = Join-Path $Cache $name
  if (-not (Test-Path $archive)) {
    $part = "$archive.part"
    Write-Step 'Mengunduh JDK... ini hanya terjadi sekali.'
    Invoke-WebRequest -Uri $link -OutFile $part -TimeoutSec 900
    Move-Item -Path $part -Destination $archive -Force
  }

  $actual = (Get-FileHash -Algorithm SHA256 -Path $archive).Hash.ToLowerInvariant()
  if ($actual -ne $checksum.ToLowerInvariant()) {
    Remove-Item $archive -Force
    throw 'Checksum JDK tidak cocok. Unduhan dibuang; silakan coba lagi.'
  }

  Write-Step 'Checksum OK. Mengekstrak JDK...'
  $extract = Join-Path $Cache 'jdk-extract'
  if (Test-Path $extract) { Remove-Item $extract -Recurse -Force }
  New-Item -ItemType Directory -Force -Path $extract | Out-Null
  Expand-Archive -Path $archive -DestinationPath $extract -Force

  $jdkDir = Get-ChildItem -Path $extract -Directory -Filter 'jdk-25*' | Select-Object -First 1
  if (-not $jdkDir) { throw 'Folder JDK tidak ditemukan setelah ekstraksi.' }

  $target = Join-Path $Cache 'jdk-25'
  if (Test-Path $target) { Remove-Item $target -Recurse -Force }
  Move-Item -Path $jdkDir.FullName -Destination $target
  Remove-Item $extract -Recurse -Force

  return (Join-Path $target 'bin\java.exe')
}

function Test-NeedsBuild {
  if (-not (Test-Path $Jar)) { return $true }
  $jarTime = (Get-Item $Jar).LastWriteTime
  $paths = @(
    (Join-Path $Root 'src'),
    (Join-Path $Root 'build.gradle'),
    (Join-Path $Root 'settings.gradle'),
    (Join-Path $Root 'gradle')
  )
  foreach ($path in $paths) {
    if (-not (Test-Path $path)) { continue }
    $newer = Get-ChildItem -Path $path -Recurse -File -ErrorAction SilentlyContinue |
      Where-Object { $_.LastWriteTime -gt $jarTime } |
      Select-Object -First 1
    if ($newer) { return $true }
  }
  return $false
}

$javaExe = Get-JavaExe
$major = Get-JavaMajor $javaExe
if ($major -lt 25) {
  $javaExe = Install-Jdk
  $major = Get-JavaMajor $javaExe
}
if ($major -lt 25) {
  throw 'JDK hasil unduhan tidak bisa dipakai.'
}

$env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $javaExe)
Write-Step "JDK terdeteksi: $major ($env:JAVA_HOME)"

if ($env:MAMAT_BOOTSTRAP_CHECK -eq '1') {
  $state = if (Test-Path $Jar) { 'ada' } else { 'akan dibangun' }
  Write-Step "BOOTSTRAP OK — JDK=$major, JAR=$state"
  exit 0
}

if (Test-NeedsBuild) {
  Write-Step 'Menyiapkan Gradle + dependency (unduhan pertama bisa memakan waktu)...'
  & $Wrapper -p $Root fatJar --console=plain
  if ($LASTEXITCODE -ne 0) { throw 'Gagal membangun aplikasi.' }
}

Write-Step 'Menjalankan Mamat Gunshop...'
& $javaExe -jar $Jar @AppArgs
if ($LASTEXITCODE -ne 0) {
  throw "Aplikasi keluar dengan kode $LASTEXITCODE"
}
