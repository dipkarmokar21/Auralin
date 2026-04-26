package managers;

import controllers.PlayerController;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import components.MusicCard;
import views.*;
import ui.SongTable;
import model.Song;
import services.DatabaseService;

public class ViewManager {
    private DatabaseService db;
    private StackPane contentArea;
    private String activeView = "Home";
    private String userName;
    private PlayerController playerController;
    private FileImportCallback fileImportCallback;



    public interface FileImportCallback {
        void onAddFiles();
        void onAddFolder();
    }
    
    public ViewManager(DatabaseService db, StackPane contentArea, String userName, PlayerController playerController) {
        this.db = db;
        this.contentArea = contentArea;
        this.userName = userName;
        this.playerController = playerController;
    }
    
    public void setFileImportCallback(FileImportCallback callback) {
        this.fileImportCallback = callback;
    }
    
    public void showView(String view) {
        this.activeView = view;
        VBox viewContent = null;
        
        SongTable.SongTableListener tableListener = new SongTable.SongTableListener() {
            @Override
            public void onSongDoubleClick(Song song) {
                playerController.play(song);
            }

            @Override
            public boolean isCurrentSong(Song song) {
                return song == db.getCurrent();
            }

            @Override
            public void onLikeToggle(Song song) {
                db.saveToDisk();
                // If the toggled song is currently playing, update the heart in PlayerBar too
                if (song == db.getCurrent()) {
                    playerController.syncLikeState(song);
                }
                // If we're on the Liked view, refresh it so unliked songs disappear instantly
                if (activeView.equals("Liked")) {
                    showView("Liked");
                }
            }
        };
        
        MusicCard.MusicCardListener cardListener = new MusicCard.MusicCardListener() {
            @Override
            public void onCardClick(Song song) {
                playerController.play(song);
            }
            
            @Override
            public boolean isCurrentSong(Song song) {
                return song == db.getCurrent();
            }
            
            @Override
            public boolean isPlaying(Song song) {
                return playerController.isPlaying();
            }
        };
        
        switch (view) {
            case "Home":
                HomeView homeView = new HomeView(userName, cardListener);
                homeView.setContent(db.getRecentlyPlayed(), db.getRecommendations());
                viewContent = homeView;
                break;
            case "Search":
                viewContent = new SearchView(db.getAllSongs(), tableListener);
                break;
            case "Library":
                viewContent = new LibraryView(db.getAllSongs(), tableListener, new LibraryView.LibraryViewListener() {
                    @Override
                    public void onAddFiles() {
                        if (fileImportCallback != null) fileImportCallback.onAddFiles();
                    }
                    
                    @Override
                    public void onAddFolder() {
                        if (fileImportCallback != null) fileImportCallback.onAddFolder();
                    }
                });
                break;
            case "Liked":
                viewContent = new LikedView(db.getLikedSongs(), tableListener);
                break;
        }
        
        if (viewContent != null) {
            ScrollPane scroll = new ScrollPane(viewContent);
            scroll.setFitToWidth(true);
            scroll.setFitToHeight(!view.equals("Home"));
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            contentArea.getChildren().setAll(scroll);
            // Style scrollbar nodes after layout pass
            javafx.application.Platform.runLater(() -> styleScrollBars(scroll));
        }
    }
    
    public void refreshCurrentView() {
        showView(activeView);
    }

    /**
     * CHANGED: Refreshes Home view cards in-place without switching views.
     * Called on song change and play/pause toggle so icons stay live
     * even when the user is on Library/Search/Liked view.
     */
    public void refreshHomeCards() {
        if (activeView.equals("Home")) {
            showView("Home");
        }
        // If not on Home, the next time user navigates to Home it will rebuild with correct state
    }

    private void styleScrollBars(ScrollPane scroll) {
        // Walk the entire scene graph under the ScrollPane and style every scrollbar part
        styleNode(scroll);
    }

    private void styleNode(javafx.scene.Parent parent) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            String cls = node.getStyleClass().toString();
            if (cls.contains("scroll-bar")) {
                node.setStyle("-fx-background-color: #000000; -fx-border-color: transparent;");
            }
            if (cls.contains("thumb")) {
                node.setStyle("-fx-background-color: #000000; -fx-background-radius: 3; -fx-background-insets: 1;");
            }
            if (cls.contains("track")) {
                node.setStyle("-fx-background-color: #000000;");
            }
            if (cls.contains("increment-button") || cls.contains("decrement-button")) {
                node.setStyle("-fx-background-color: #000000; -fx-padding: 0; -fx-pref-width: 0; -fx-pref-height: 0;");
            }
            if (cls.contains("increment-arrow") || cls.contains("decrement-arrow")) {
                node.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            }
            if (node instanceof javafx.scene.Parent) {
                styleNode((javafx.scene.Parent) node);
            }
        }
    }
    
    /**
     * Refreshes only the table data without recreating the entire view.
     * This prevents the header from flickering when like state changes.
     */
    public void refreshTableOnly() {
        // Simply trigger a table refresh by calling refresh() on the TableView
        // The table will re-render cells without recreating headers
        if (contentArea.getChildren().isEmpty()) return;
        
        javafx.scene.Node scrollPane = contentArea.getChildren().get(0);
        if (scrollPane instanceof ScrollPane) {
            javafx.scene.Node content = ((ScrollPane) scrollPane).getContent();
            if (content instanceof VBox) {
                VBox vbox = (VBox) content;
                // Find the TableView in the VBox and refresh it
                for (javafx.scene.Node node : vbox.getChildren()) {
                    if (node instanceof ui.SongTable) {
                        ((ui.SongTable) node).refresh();
                        return;
                    }
                }
            }
        }
    }
    
    public String getActiveView() {
        return activeView;
    }
    
    public void setActiveView(String view) {
        this.activeView = view;
    }
}
