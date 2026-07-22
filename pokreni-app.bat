@echo off
title EasyShop
cd /d "%~dp0"

for %%f in (target\shop-*.jar) do set JAR=%%f

if not defined JAR (
    echo Nije pronadjen .jar fajl u target\ folderu.
    echo Prvo build-uj aplikaciju sa: mvnw clean package -Pbundle-frontend
    pause
    exit /b 1
)

echo Pokrecem EasyShop (%JAR%)...
echo Aplikacija ce se otvoriti u browseru za par sekundi. Ne zatvaraj ovaj prozor dok koristis app.
echo.

java -jar "%JAR%"

echo.
echo Aplikacija je zaustavljena.
pause
