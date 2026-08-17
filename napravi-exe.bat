@echo off
title EasyShop - pravljenje exe-a
cd /d "%~dp0"

echo === 1/2: Build backend + frontend (mvn package) ===
call mvnw.cmd clean package -Pbundle-frontend -DskipTests
if errorlevel 1 (
    echo Build nije uspeo.
    pause
    exit /b 1
)

echo.
echo === 2/2: Pravljenje samostalnog EasyShop.exe (sa upakovanom Javom) ===
rmdir /s /q jpackage-input 2>nul
mkdir jpackage-input
copy /y target\shop-*.jar jpackage-input\shop.jar >nul

rmdir /s /q dist-app 2>nul
jpackage ^
  --type app-image ^
  --input jpackage-input ^
  --main-jar shop.jar ^
  --name EasyShop ^
  --dest dist-app ^
  --icon branding\EasyShop.ico ^
  --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (
    echo jpackage nije uspeo. Proveri da li je JAVA_HOME podesen na pravi JDK (17+) sa jpackage alatom.
    pause
    exit /b 1
)

rmdir /s /q jpackage-input

echo.
echo GOTOVO! Ceo folder "dist-app\EasyShop" mozes da prebacis na drugi racunar
echo (USB / mrezni disk) i pokrenes EasyShop.exe direktno - Java ne mora biti instalirana.
echo.
pause
