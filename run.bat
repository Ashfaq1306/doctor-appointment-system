@echo off
cd /d "%~dp0"

rem use java from PATH, if it is not found use the one that comes with BlueJ
where javac >nul 2>nul
if errorlevel 1 set "PATH=%ProgramFiles%\BlueJ\jdk\bin;%PATH%"

if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java
if errorlevel 1 (
    echo Could not compile. Please install JDK 11 or newer.
    pause
    exit /b 1
)
java -cp out Main
