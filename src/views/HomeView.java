package views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import components.MusicCard;
import model.Song;
import config.Constants;
import java.util.List;

public class HomeView extends VBox {
    private String userName;
    private MusicCard.MusicCardListener cardListener;
    
    public HomeView(String userName, MusicCard.MusicCardListener cardListener) {
        super(30);
        this.userName = userName;
        this.cardListener = cardListener;
        setupUI();
    }
    
    private void setupUI() {
        setPadding(new Insets(40));
        setMinWidth(800);
        setStyle("-fx-background-color: linear-gradient(to bottom, #222222, " + Constants.BG_BLACK + ");");
        
        Label title = new Label("Hello " + userName);
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        getChildren().add(title);
    }
    
    public void setContent(List<Song> recentlyPlayed, List<Song> recommendations) {
        getChildren().clear();
        
        Label title = new Label("Hello " + userName);
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        
        VBox recent = new VBox(15, createSectionLabel("Recently Played"), createGrid(recentlyPlayed));
        VBox recs = new VBox(15, createSectionLabel("Made For You"), createGrid(recommendations));
        
        VBox.setVgrow(recent, Priority.ALWAYS);
        VBox.setVgrow(recs, Priority.ALWAYS);
        
        getChildren().addAll(title, recent, recs);
    }
    
    private Label createSectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 22));
        l.setTextFill(Color.WHITE);
        return l;
    }
    
    private FlowPane createGrid(List<Song> songs) {
        FlowPane p = new FlowPane(20, 20);
        p.setPadding(new Insets(0, 20, 0, 0));
        HBox.setHgrow(p, Priority.ALWAYS);
        for (Song s : songs) {
            p.getChildren().add(new MusicCard(s, cardListener));
        }
        return p;
    }
}
