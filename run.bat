@echo off
REM Compile and run Emergency SOS (Stage 1). Needs JDK 11+ (javac must work).
REM cd /d "%~dp0" = go to the folder of this file (works even if the path has spaces)
cd /d "%~dp0"
if not exist out mkdir out
javac -d out -sourcepath src src\com\sos\Main.java
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
echo Starting server... open http://localhost:8080
java -cp out com.sos.Main
pause
