# Auralin Music Player - Project Structure

## 📁 Final Organized Structure

```
src/
├── 📂 components/           # Reusable UI Components (3 files)
│   ├── PlayerBar.java       # Bottom player with controls
│   ├── Sidebar.java         # Left navigation menu
│   └── MusicCard.java       # Song card for grid display
│
├── 📂 views/                # Full Page Views (5 files)
│   ├── HomeView.java        # Home page (recently played + recommendations)
│   ├── SearchView.java      # Search with live filtering
│   ├── LibraryView.java     # All songs + add files/folder
│   ├── LikedView.java       # Liked songs only
│   └── QueueView.java       # Current play queue
│
├── 📂 ui/                   # Complex UI Widgets (1 file)
│   └── SongTable.java       # Custom table with animations
│
├── 📂 model/                # Data Models (1 file)
│   └── Song.java            # Song entity with properties
│
├── 📂 services/             # Business Logic (1 file)
│   └── DatabaseService.java # Data management & filtering
│
├── 📂 config/               # Configuration (1 file)
│   └── Constants.java       # App constants, colors, CSS
│
└── 📄 Main.java             # Application entry point
```

## 🎯 Package Organization

### 1. **config/** - Configuration Layer
- **Constants.java**: 
  - Color themes (Spotify green, backgrounds)
  - CSS styles (buttons, tables, cards)
  - Default values (album art URL)

### 2. **model/** - Data Layer
- **Song.java**:
  - Song properties (title, artist, file path)
  - Artwork, duration, play count
  - Liked status management

### 3. **services/** - Business Logic Layer
- **DatabaseService.java**:
  - Song collection management
  - Metadata extraction (mp3agic + JavaFX Media)
  - Filtering (liked, recent, recommendations)

### 4. **components/** - Reusable UI Components
- **PlayerBar.java**: Play controls, progress, volume
- **Sidebar.java**: Navigation buttons, loading indicator
- **MusicCard.java**: Grid view song cards

### 5. **views/** - Page Views
- **HomeView.java**: Recently played + recommendations
- **SearchView.java**: Search functionality
- **LibraryView.java**: All songs library
- **LikedView.java**: Liked songs
- **QueueView.java**: Play queue

### 6. **ui/** - Complex Widgets
- **SongTable.java**: Custom table with pulse animations

## 🔗 Dependencies Between Packages

```
Main.java
  ├─→ config.Constants
  ├─→ model.Song
  ├─→ services.DatabaseService
  ├─→ components.*
  ├─→ views.*
  └─→ ui.*

components/* & views/*
  ├─→ model.Song
  └─→ config.Constants

services.DatabaseService
  ├─→ model.Song
  └─→ config.Constants

model.Song
  └─→ config.Constants
```

## 🚀 How to Build & Run

### Windows:
```bash
# Compile
compile.bat

# Run
run.bat
```

### Manual (Windows):
```bash
mkdir out
javac -cp "lib/*;src" -d out src/Main.java src/components/*.java src/views/*.java src/ui/*.java src/model/*.java src/services/*.java src/config/*.java
java -cp "lib/*;out" Main
```

### Manual (Linux/Mac):
```bash
mkdir out
javac -cp "lib/*:src" -d out src/Main.java src/components/*.java src/views/*.java src/ui/*.java src/model/*.java src/services/*.java src/config/*.java
java -cp "lib/*:out" Main
```

## ✨ Benefits of This Structure

1. **Clear Separation of Concerns**
   - UI (components, views, ui)
   - Data (model)
   - Logic (services)
   - Config (config)

2. **Easy to Navigate**
   - প্রতিটা layer আলাদা folder-এ
   - File খুঁজে পাওয়া সহজ

3. **Scalable**
   - নতুন component/view যোগ করা সহজ
   - Package structure maintain করা easy

4. **Maintainable**
   - Bug fix করতে কোথায় যেতে হবে clear
   - Code duplication কম

5. **Testable**
   - Individual packages test করা যায়
   - Mock করা সহজ

## 📊 File Count Summary

- **Total Files**: 13
  - Components: 3
  - Views: 5
  - UI Widgets: 1
  - Models: 1
  - Services: 1
  - Config: 1
  - Main: 1

- **Total Lines**: ~1500 (আগে 600+ একটা file-এ ছিল)

## 🎨 Design Patterns Used

1. **Listener Pattern**: Components communicate via interfaces
2. **Observer Pattern**: ObservableList for reactive updates
3. **MVC-like**: Model (Song), View (views/*), Controller (Main)
4. **Service Layer**: DatabaseService handles business logic
5. **Configuration**: Constants centralize all config

## 📝 Notes

- সব files-এ proper package declarations আছে
- Import statements সঠিকভাবে organized
- No compilation errors
- Ready to run!
