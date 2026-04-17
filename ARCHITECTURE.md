# 🏗️ Architecture Guide - Auralin Music Player

## 📐 Architecture Pattern

This project follows a **layered architecture** with clear separation of concerns, inspired by MVC but adapted for JavaFX.

---

## 🎯 Layer Breakdown

### 1. **Presentation Layer** (UI)
Components that users see and interact with.

```
components/     - Reusable UI widgets
views/          - Full page views
ui/             - Complex custom widgets
```

**Responsibility:** Display data and capture user input

---

### 2. **Manager Layer** (Coordination)
Coordinates between different parts of the application.

```
managers/       - UI coordination and orchestration
```

**Example:** `ViewManager`
- Switches between views
- Creates view instances
- Sets up listeners
- Manages layout

**NOT a Controller because:**
- ❌ Doesn't handle business logic
- ❌ Doesn't update models directly
- ✅ Only coordinates UI components

---

### 3. **Controller Layer** (Business Logic)
Handles user actions and updates models.

```
controllers/    - Business logic and state management
```

**Example:** `PlayerController`
- Handles play/pause/next/prev
- Manages MediaPlayer lifecycle
- Updates song state
- Controls playback logic

**IS a Controller because:**
- ✅ Handles business logic
- ✅ Updates model (Song, DatabaseService)
- ✅ Responds to user actions
- ✅ Manages application state

---

### 4. **Service Layer** (Data & Operations)
Provides data access and business operations.

```
services/       - Data management and business operations
```

**Example:** `DatabaseService`
- Manages song collection
- Filters and sorts data
- Metadata extraction
- Data persistence logic

---

### 5. **Model Layer** (Data)
Pure data structures.

```
model/          - Data entities
```

**Example:** `Song`
- Properties (title, artist, etc.)
- Simple getters/setters
- No business logic

---

### 6. **Utility Layer** (Helpers)
Reusable helper functions.

```
utils/          - Helper classes and utilities
```

**Examples:**
- `DialogHelper` - Dialog creation
- `FileImportService` - File operations

---

### 7. **Configuration Layer**
Application-wide constants and settings.

```
config/         - Constants and configuration
```

**Example:** `Constants`
- Colors, CSS
- Default values
- App settings

---

## 🔄 Data Flow

```
User Interaction
      ↓
┌─────────────────┐
│  Presentation   │  (components, views, ui)
│   (UI Layer)    │
└────────┬────────┘
         ↓
┌─────────────────┐
│    Managers     │  (managers)
│  (Coordination) │
└────────┬────────┘
         ↓
┌─────────────────┐
│  Controllers    │  (controllers)
│ (Business Logic)│
└────────┬────────┘
         ↓
┌─────────────────┐
│    Services     │  (services)
│  (Data Access)  │
└────────┬────────┘
         ↓
┌─────────────────┐
│     Models      │  (model)
│     (Data)      │
└─────────────────┘
```

---

## 📦 Package Responsibilities

### **components/** - Reusable UI Components
```java
// Example: PlayerBar.java
- Displays player controls
- Fires events via listeners
- NO business logic
- Reusable across views
```

### **views/** - Full Page Views
```java
// Example: HomeView.java
- Represents a complete screen
- Composes multiple components
- Handles layout
- NO business logic
```

### **ui/** - Complex Custom Widgets
```java
// Example: SongTable.java
- Custom TableView with animations
- Complex rendering logic
- Reusable widget
- NO business logic
```

### **managers/** - Coordination & Orchestration
```java
// Example: ViewManager.java
- Switches between views
- Creates view instances
- Wires up listeners
- Manages UI state
- NO business logic (just coordination)
```

### **controllers/** - Business Logic & State
```java
// Example: PlayerController.java
- Handles user actions
- Updates models
- Manages MediaPlayer
- Contains business rules
- Application state management
```

### **services/** - Data & Operations
```java
// Example: DatabaseService.java
- Data access
- CRUD operations
- Filtering/sorting
- Business operations
```

### **model/** - Data Entities
```java
// Example: Song.java
- Pure data structure
- Properties + getters/setters
- NO business logic
- NO UI dependencies
```

### **utils/** - Helper Utilities
```java
// Example: DialogHelper.java
- Reusable helper functions
- Stateless utilities
- Can be used anywhere
```

### **config/** - Configuration
```java
// Example: Constants.java
- App-wide constants
- Configuration values
- CSS styles
```

---

## 🎨 Design Patterns Used

### 1. **MVC-inspired (Modified for JavaFX)**
- **Model:** Song, DatabaseService
- **View:** components/, views/, ui/
- **Controller:** PlayerController
- **Manager:** ViewManager (coordination layer)

### 2. **Observer Pattern**
- ObservableList for reactive updates
- Listeners for event handling

### 3. **Listener Pattern**
- PlayerBarListener
- SongTableListener
- MusicCardListener
- FileImportCallback

### 4. **Service Layer Pattern**
- DatabaseService
- FileImportService

### 5. **Facade Pattern**
- Main.java as application facade

### 6. **Strategy Pattern**
- Different view strategies (Home, Search, Library, etc.)

---

## 🔍 Key Differences

### Manager vs Controller

| Aspect | Manager | Controller |
|--------|---------|------------|
| **Purpose** | Coordinate UI | Handle business logic |
| **Example** | ViewManager | PlayerController |
| **Updates Model?** | ❌ No | ✅ Yes |
| **Business Logic?** | ❌ No | ✅ Yes |
| **Creates Views?** | ✅ Yes | ❌ No |
| **Wires Listeners?** | ✅ Yes | ❌ No |
| **Manages State?** | UI state only | Application state |

### Service vs Controller

| Aspect | Service | Controller |
|--------|---------|------------|
| **Purpose** | Data operations | Business logic |
| **Example** | DatabaseService | PlayerController |
| **Has UI deps?** | ❌ No | ✅ Can have |
| **Reusable?** | ✅ Highly | ✅ Moderately |
| **Stateful?** | ✅ Yes (data) | ✅ Yes (state) |

---

## 📊 Dependency Graph

```
Main.java
  ├─→ managers.ViewManager
  │     ├─→ views.*
  │     ├─→ components.*
  │     ├─→ ui.*
  │     └─→ controllers.PlayerController
  │
  ├─→ controllers.PlayerController
  │     ├─→ services.DatabaseService
  │     ├─→ model.Song
  │     └─→ components.PlayerBar
  │
  ├─→ services.DatabaseService
  │     ├─→ model.Song
  │     └─→ config.Constants
  │
  └─→ utils.*
        ├─→ DialogHelper
        └─→ FileImportService
```

---

## ✅ Architecture Benefits

### 1. **Clear Separation**
- Each layer has specific responsibility
- Easy to understand where code belongs

### 2. **Testability**
- Controllers can be unit tested
- Services can be tested independently
- Models are pure data (easy to test)

### 3. **Maintainability**
- Bug in playback? → Check PlayerController
- Bug in view switching? → Check ViewManager
- Bug in data? → Check DatabaseService

### 4. **Scalability**
- Easy to add new views
- Easy to add new controllers
- Easy to add new services

### 5. **Reusability**
- Components reusable across views
- Services reusable across controllers
- Utils reusable everywhere

---

## 🎯 When to Use Each Layer

### Use **components/** when:
- Creating reusable UI widget
- Need same UI in multiple places
- Pure presentation logic

### Use **views/** when:
- Creating a full screen/page
- Composing multiple components
- Specific to one screen

### Use **managers/** when:
- Coordinating multiple components
- Managing UI state/navigation
- Wiring up listeners
- NO business logic

### Use **controllers/** when:
- Handling user actions
- Implementing business rules
- Managing application state
- Updating models

### Use **services/** when:
- Data access/persistence
- Business operations
- External API calls
- Reusable operations

### Use **model/** when:
- Defining data structure
- Pure data entity
- No logic needed

### Use **utils/** when:
- Helper/utility function
- Stateless operation
- Reusable across layers

---

## 🏆 Best Practices

1. ✅ **Keep layers independent**
   - Don't skip layers
   - Follow dependency direction

2. ✅ **Single Responsibility**
   - Each class has one job
   - Easy to name and understand

3. ✅ **Dependency Injection**
   - Pass dependencies via constructor
   - Easy to test and mock

4. ✅ **Interface-based Design**
   - Use listeners/callbacks
   - Loose coupling

5. ✅ **No circular dependencies**
   - Dependencies flow downward
   - Clean architecture

---

## 📚 Summary

```
Presentation (UI)
    ↓
Managers (Coordination)
    ↓
Controllers (Business Logic)
    ↓
Services (Data Operations)
    ↓
Models (Data)

+ Utils (Helpers)
+ Config (Settings)
```

**This is a professional, scalable, maintainable architecture!** 🎉
