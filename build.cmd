@echo off
setlocal
cd /d "%~dp0"

where node >nul 2>nul || (echo Node.js 18 or newer is required: https://nodejs.org & exit /b 1)
for %%t in (java javac jar keytool) do (
  where %%t >nul 2>nul || (echo A JDK 17 or newer is required ^(missing %%t^): https://adoptium.net & exit /b 1)
)

node build.mjs %*
if errorlevel 1 exit /b 1
echo.
echo APK ready: %~dp0bin\glyphcast.apk
