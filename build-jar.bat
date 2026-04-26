@echo off
echo Building Auralin Music Player JAR...
echo.

set JAVAFX=D:\javafx\javafx-sdk-25.0.2\lib
set MP3AGIC=C:\Users\Dip karmokar\Downloads\mp3agic-0.9.1.jar
set CLASSPATH=%JAVAFX%\*;%MP3AGIC%;lib\*;src

REM Step 1: Compile
if not exist "out" mkdir out
if not exist "out\resources" mkdir out\resources
xcopy /Y /Q "src\resources\*" "out\resources\" >nul

javac -cp "%CLASSPATH%" -d out ^
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
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Step 2: Extract mp3agic and annotations into out folder
echo Extracting dependencies...
cd out
jar xf "%MP3AGIC%"
jar xf "..\lib\annotations-26.0.2.jar"
cd ..

REM Step 3: Create MANIFEST.MF
if not exist "out\META-INF" mkdir out\META-INF
echo Main-Class: Main> out\META-INF\MANIFEST.MF

REM Step 4: Package into fat JAR (all classes merged)
jar cfm AuralinPlayer.jar out\META-INF\MANIFEST.MF -C out .

if %errorlevel% equ 0 (
    echo.
    echo JAR created: AuralinPlayer.jar
    echo.
    echo To run:
    echo java --module-path "%JAVAFX%" --add-modules javafx.controls,javafx.media,javafx.swing -jar AuralinPlayer.jar
) else (
    echo JAR creation failed!
)

pause
