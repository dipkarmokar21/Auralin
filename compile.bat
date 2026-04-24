@echo off
echo Compiling Auralin Music Player...
echo.

REM Create output directory
if not exist "out" mkdir out

REM Compile all Java files
javac -cp "lib/*;src" -d out ^
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

if %errorlevel% equ 0 (
    echo.
    echo ✓ Compilation successful!
    REM Copy resources to out folder
    if not exist "out\resources" mkdir out\resources
    xcopy /Y /Q "src\resources\*" "out\resources\" >nul
    echo.
    echo To run the application, use:
    echo java -cp "lib/*;out" Main
) else (
    echo.
    echo ✗ Compilation failed! Check errors above.
)

pause
