@echo off
echo Running Auralin Music Player...
echo.

if not exist "out" (
    echo Error: Compiled files not found!
    echo Please run compile.bat first.
    pause
    exit /b 1
)

java -cp "lib/*;out" Main

pause
