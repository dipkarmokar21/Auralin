# Auralin Music Player

A modern desktop music player built with JavaFX 25, inspired by Spotify's dark UI.

![Java](https://img.shields.io/badge/Java-25-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-25.0.2-blue) ![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey) ![License](https://img.shields.io/badge/License-Proprietary-red)

---

## Features

- **Library Management** — Add individual MP3 files or entire folders
- **Music Playback** — Play, pause, next, previous, seek, volume control
- **Shuffle & Repeat** — Shuffle mode and single-song repeat
- **Liked Songs** — Heart toggle with persistent storage
- **Home View** — Recently Played and Made For You grids
- **Now Playing** — Full-screen artwork overlay
- **Real-time Search** — Filter songs instantly
- **Auto-Save** — Library, liked songs, and play history persist across sessions
- **Open With** — Register as default MP3 player
- **Custom Window** — Frameless Win11-style window with resize support

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| UI Framework | JavaFX 25.0.2 |
| Audio Metadata | mp3agic 0.9.1 |
| Build Tool | javac + jar (manual) |
| Installer | Inno Setup 6 + launch4j |
| Runtime | jlink bundled JRE |

---

## Project Structure

```
src/
├── Main.java                    # Application entry point
├── components/
│   ├── MusicCard.java           # Song card for Home view grid
│   ├── PlayerBar.java           # Bottom playback controls bar
│   └── Sidebar.java             # Left navigation panel
├── config/
│   └── Constants.java           # Colors, CSS, global constants
├── controllers/
│   └── PlayerController.java    # Playback logic (play/pause/next/prev)
├── managers/
│   └── ViewManager.java         # View switching and scroll management
├── model/
│   └── Song.java                # Song data model
├── services/
│   └── DatabaseService.java     # Library persistence and metadata loading
├── ui/
│   └── SongTable.java           # Custom TableView for Library/Search/Liked
├── utils/
│   └── FileImportService.java   # File/folder import with progress
└── views/
    ├── HomeView.java            # Home screen with grids
    ├── LibraryView.java         # Full library table
    ├── LikedView.java           # Liked songs table
    ├── NowPlayingView.java      # Full-screen now playing overlay
    └── SearchView.java          # Search with real-time filter
```

---

## Architecture

Auralin follows the **MVC pattern** with additional design patterns:

- **Observer** — `onSongChange`, `onLikeChange`, `onPlayStateChange` callbacks
- **Facade** — `DatabaseService` hides mp3agic + MediaPlayer complexity
- **Strategy** — Shuffle vs Sequential playback
- **Singleton** — `DatabaseService` as single source of truth

---

## Building from Source

### Prerequisites
- JDK 25 (Amazon Corretto 25 recommended)
- JavaFX SDK 25.0.2 at `D:\javafx\javafx-sdk-25.0.2`
- mp3agic 0.9.1 jar at `C:\Users\<user>\Downloads\mp3agic-0.9.1.jar`

### Compile & Run
```bat
compile.bat
run.bat
```

### Build Full Installer
```bat
build-all.bat
```
This will:
1. Compile all Java sources
2. Build a fat JAR with mp3agic bundled
3. Create a custom JRE with jlink (~80MB)
4. Wrap into `.exe` with launch4j
5. Build `AuralinPlayerSetup.exe` with Inno Setup

---

## Data Persistence

Library data is saved at:
```
%APPDATA%\AuralinPlayer\library.dat
```

Format (pipe-separated):
```
filePath|liked|plays|lastPlayed|artist|durationMs
```

---

## Running the JAR directly

```bat
java --module-path "D:\javafx\javafx-sdk-25.0.2\lib" --add-modules javafx.controls,javafx.media,javafx.swing -jar AuralinPlayer.jar
```

---

## License

Copyright (c) 2026 Dip's Auralin Tech Inc. All rights reserved.
See [LICENSE.txt](LICENSE.txt) for details.

---

## Author

**Dip Karmokar** — OOP Project 2026
