package managers;

import javafx.collections.FXCollections;
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
            case "Queue":
                ObservableList<Song> queueList = FXCollections.observableArrayList(db.getAllSongs());
                Song current = db.getCurrent();
                if (current != null) {
                    queueList.remove(current);
                    queueList.add(0, current);
                }
                viewContent = new QueueView(queueList, tableListener);
                break;
        }
        
        if (viewContent != null) {
            ScrollPane scroll = new ScrollPane(viewContent);
            scroll.setFitToWidth(true);
            scroll.setFitToHeight(true);
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
            contentArea.getChildren().setAll(scroll);
        }
    }
    
    public void refreshCurrentView() {
        showView(activeView);
    }
    
    public String getActiveView() {
        return activeView;
    }
    
    public void setActiveView(String view) {
        this.activeView = view;
    }
}
