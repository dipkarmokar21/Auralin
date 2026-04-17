package views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.SongTable;
import model.Song;
import config.Constants;

public class SearchView extends VBox {
    private TextField searchField;
    private SongTable resultsTable;
    private ObservableList<Song> allSongs;
    
    public SearchView(ObservableList<Song> allSongs, SongTable.SongTableListener tableListener) {
        super(30);
        this.allSongs = allSongs;
        setupUI(tableListener);
    }
    
    private void setupUI(SongTable.SongTableListener tableListener) {
        setPadding(new Insets(40));
        setMinWidth(800);
        setStyle("-fx-background-color: linear-gradient(to bottom, #222222, " + Constants.BG_BLACK + ");");
        
        Label title = new Label("Search");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        
        searchField = new TextField();
        searchField.setPromptText("What do you want to listen to?");
        searchField.getStyleClass().add("search-field");
        
        resultsTable = new SongTable(FXCollections.observableArrayList(), tableListener);
        resultsTable.setVisible(false);
        resultsTable.managedProperty().bind(resultsTable.visibleProperty());
        
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                resultsTable.setVisible(false);
            } else {
                resultsTable.setVisible(true);
                resultsTable.setItems(allSongs.filtered(s ->
                        s.getTitle().toLowerCase().contains(newVal.toLowerCase()) ||
                                s.getArtist().toLowerCase().contains(newVal.toLowerCase())));
            }
        });
        
        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        getChildren().addAll(title, searchField, resultsTable);
    }
}
