package views;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.SongTable;
import model.Song;
import config.Constants;

public class LibraryView extends VBox {
    private LibraryViewListener listener;
    
    public interface LibraryViewListener {
        void onAddFiles();
        void onAddFolder();
    }
    
    public LibraryView(ObservableList<Song> songs, SongTable.SongTableListener tableListener, LibraryViewListener listener) {
        super(30);
        this.listener = listener;
        setupUI(songs, tableListener);
    }
    
    private void setupUI(ObservableList<Song> songs, SongTable.SongTableListener tableListener) {
        setPadding(new Insets(40));
        setMinWidth(800);
        setStyle("-fx-background-color: linear-gradient(to bottom, #222222, " + Constants.BG_BLACK + ");");
        
        Label title = new Label("Your Library");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        
        HBox actions = new HBox(15);
        Button addFiles = new Button("➕ Add Files");
        addFiles.getStyleClass().add("action-btn");
        addFiles.setOnAction(e -> listener.onAddFiles());
        
        Button addFolder = new Button("📁 Add Folder");
        addFolder.getStyleClass().add("action-btn");
        addFolder.setOnAction(e -> listener.onAddFolder());
        
        actions.getChildren().addAll(addFiles, addFolder);
        
        SongTable table = new SongTable(songs, tableListener);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        getChildren().addAll(title, actions, table);
    }
}
