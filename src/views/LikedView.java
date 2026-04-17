package views;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.SongTable;
import model.Song;
import config.Constants;

public class LikedView extends VBox {
    public LikedView(ObservableList<Song> likedSongs, SongTable.SongTableListener tableListener) {
        super(30);
        setupUI(likedSongs, tableListener);
    }
    
    private void setupUI(ObservableList<Song> likedSongs, SongTable.SongTableListener tableListener) {
        setPadding(new Insets(40));
        setMinWidth(800);
        setStyle("-fx-background-color: linear-gradient(to bottom, #222222, " + Constants.BG_BLACK + ");");
        
        Label title = new Label("Liked Songs");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        
        SongTable table = new SongTable(likedSongs, tableListener);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        getChildren().addAll(title, table);
    }
}
