@echo off
echo ============================================
echo  Auralin Music Player - Full Build Script
echo ============================================
echo.

set JLINK="C:\Users\Dip karmokar\.jdks\corretto-25.0.2\bin\jlink.exe"
set JAVAC="C:\Users\Dip karmokar\.jdks\corretto-25.0.2\bin\javac.exe"
set JAR_TOOL="C:\Users\Dip karmokar\.jdks\corretto-25.0.2\bin\jar.exe"
set JAVAFX=D:\javafx\javafx-sdk-25.0.2\lib
set MP3AGIC=C:\Users\Dip karmokar\Downloads\mp3agic-0.9.1.jar
set LAUNCH4J="C:\Program Files (x86)\Launch4j\launch4jc.exe"
set INNO="C:\Program Files (x86)\Inno Setup 6\ISCC.exe"

REM ── Step 1: Compile ───────────────────────────────────────────────────────
echo [1/5] Compiling Java sources...
if not exist "out" mkdir out
if not exist "out\resources" mkdir out\resources
xcopy /Y /Q "src\resources\*" "out\resources\" >nul
copy /Y "AuralinPlayer.ico" "out\resources\" >nul

%JAVAC% -cp "%JAVAFX%\*;%MP3AGIC%;lib\*;src" -d out ^
    src/Main.java ^
    src/components/PlayerBar.java ^
    src/components/Sidebar.java ^
    src/components/MusicCard.java ^
    src/views/HomeView.java ^
    src/views/SearchView.java ^
    src/views/LibraryView.java ^
    src/views/LikedView.java ^
    src/views/NowPlayingView.java ^
    src/ui/SongTable.java ^
    src/model/Song.java ^
    src/services/DatabaseService.java ^
    src/config/Constants.java ^
    src/controllers/PlayerController.java ^
    src/managers/ViewManager.java ^
    src/utils/FileImportService.java

if %errorlevel% neq 0 (
    echo FAILED: Compilation error!
    pause & exit /b 1
)
echo     Done.

REM ── Step 2: Build fat JAR ─────────────────────────────────────────────────
echo [2/5] Building JAR...
cd out
%JAR_TOOL% xf "%MP3AGIC%"
%JAR_TOOL% xf "..\lib\annotations-26.0.2.jar"
cd ..
if not exist "out\META-INF" mkdir out\META-INF
echo Main-Class: Main> out\META-INF\MANIFEST.MF
%JAR_TOOL% cfm AuralinPlayer.jar out\META-INF\MANIFEST.MF -C out .
if %errorlevel% neq 0 (
    echo FAILED: JAR creation error!
    pause & exit /b 1
)
echo     Done.

REM ── Step 3: Build custom JRE with jlink ───────────────────────────────────
echo [3/5] Building bundled JRE (this may take a minute)...
if exist "runtime" rmdir /s /q runtime
%JLINK% ^
    --module-path "%JAVAFX%" ^
    --add-modules java.base,java.desktop,java.logging,java.xml,java.naming,java.sql,javafx.controls,javafx.media,javafx.swing,jdk.unsupported ^
    --output runtime ^
    --strip-debug ^
    --compress=2 ^
    --no-header-files ^
    --no-man-pages
if %errorlevel% neq 0 (
    echo FAILED: jlink error!
    pause & exit /b 1
)
REM Copy JavaFX native DLLs (required for rendering)
copy /Y "%JAVAFX:lib=bin%\*.dll" "runtime\bin\" >nul 2>&1
echo     Done.

REM ── Step 4: Build EXE with launch4j ──────────────────────────────────────
echo [4/5] Building EXE with launch4j...
if not exist %LAUNCH4J% (
    echo SKIPPED: launch4j not found at %LAUNCH4J%
    echo Please install launch4j from https://launch4j.sourceforge.net
) else (
    %LAUNCH4J% launch4j-config.xml
    echo     Done.
)

REM ── Step 5: Build installer with Inno Setup ───────────────────────────────
echo [5/5] Building installer...
if not exist %INNO% (
    echo SKIPPED: Inno Setup not found at %INNO%
    echo Please install Inno Setup from https://jrsoftware.org/isdl.php
) else (
    %INNO% installer.iss
    if %errorlevel% neq 0 (
        echo FAILED: Inno Setup error!
        pause & exit /b 1
    )
    echo     Done.
)

echo.
echo ============================================
echo  Build complete!
echo  Installer: installer-output\AuralinPlayerSetup.exe
echo ============================================
pause
