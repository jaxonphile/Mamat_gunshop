@echo off
chcp 65001 >nul
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\bootstrap.ps1" %*
if errorlevel 1 (
  echo.
  echo [mamat] Proses gagal. Pastikan terhubung internet, lalu coba lagi.
  pause
)
