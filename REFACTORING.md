# Code Refactoring - Component Structure

## নতুন Structure

```
src/
├── components/          # Reusable UI components
│   ├── PlayerBar.java   # Bottom player controls
│   ├── Sidebar.java     # Left navigation sidebar
│   └── MusicCard.java   # Song card for grid view
│
├── views/              # Full page views
│   ├── HomeView.java   # Home page with recently played
│   ├── SearchView.java # Search functionality
│   ├── LibraryView.java # Library with add files/folder
│   ├── LikedView.java  # Liked songs page
│   └── QueueView.java  # Play queue
│
├── ui/                 # Complex UI widgets
│   └── SongTable.java  # Table view for songs
│
├── model/              # Data models
│   └── Song.java       # Song entity
│
├── services/           # Business logic
│   └── DatabaseService.java # Data management
│
├── config/             # Configuration
│   └── Constants.java  # App constants & CSS
│
└── Main.java           # New modular main class
```

## Component Breakdown

### Components (components/)
Reusable UI components যেগুলো listener pattern ব্যবহার করে:

- **PlayerBar**: Play/pause, next/prev, shuffle, repeat, volume, progress slider
- **Sidebar**: Navigation buttons, loading indicator
- **MusicCard**: Grid view-তে song card display

### Views (views/)
Full page views যেগুলো specific screen represent করে:

- **HomeView**: Recently played + recommendations
- **SearchView**: Search field + filtered results
- **LibraryView**: All songs + add files/folder buttons
- **LikedView**: Liked songs only
- **QueueView**: Current play queue

### UI Widgets (ui/)
Complex reusable widgets:

- **SongTable**: TableView with custom cells, animations

## কিভাবে Run করবেন

### Option 1: Batch scripts use করুন (Windows)
```bash
# Compile করুন
compile.bat

# Run করুন
run.bat
```

### Option 2: Manual commands
```bash
# Compile
javac -cp "lib/*;src" -d out src/Main.java src/components/*.java src/views/*.java src/ui/*.java src/model/*.java src/services/*.java src/config/*.java

# Run
java -cp "lib/*;out" Main
```

## Benefits

1. **Modularity**: প্রতিটা component আলাদা file-এ
2. **Reusability**: Components অন্য project-এ reuse করা যাবে
3. **Maintainability**: Bug fix করা সহজ, specific component-এ focus করা যায়
4. **Testability**: Individual components test করা সহজ
5. **Readability**: Main.java এখন 300 lines (আগে 600+)
6. **Clean**: পুরানো monolithic code remove করা হয়েছে

## Listener Pattern

সব components listener interface ব্যবহার করে parent-এর সাথে communicate করে:
- `PlayerBar.PlayerBarListener`
- `Sidebar.SidebarListener`
- `MusicCard.MusicCardListener`
- `SongTable.SongTableListener`
- `LibraryView.LibraryViewListener`

এটা loose coupling ensure করে।
