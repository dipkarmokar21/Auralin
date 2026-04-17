package components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Song;
import config.Constants;

public class MusicCard extends VBox {
    private Song song;
    private MusicCardListener listener;
    
    public interface MusicCardListener {
        void onCardClick(Song song);
        boolean isCurrentSong(Song song);
    }
    
    public MusicCard(Song song, MusicCardListener listener) {
        super(12);
        this.song = song;
        this.listener = listener;
        setupUI();
    }
    
    private void setupUI() {
        getStyleClass().add("music-card");
        setPrefWidth(180);
        
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(150, 150);
        imgBox.setStyle("-fx-background-color: #333; -fx-background-radius: 12;");
        ImageView iv = new ImageView(song.getArtwork());
        iv.setFitWidth(150);
        iv.setFitHeight(150);
        imgBox.getChildren().add(iv);
        
        Label title = new Label(song.getTitle());
        title.setTextFill(listener.isCurrentSong(song) ? Color.web(Constants.SPOTIFY_GREEN) : Color.WHITE);
        title.setFont(Font.font(null, FontWeight.BOLD, 15));
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        
        Label artist = new Label(song.getArtist());
        artist.setTextFill(Color.web(Constants.TEXT_GRAY));
        artist.setTextOverrun(OverrunStyle.ELLIPSIS);
        
        getChildren().addAll(imgBox, title, artist);
        setOnMouseClicked(e -> listener.onCardClick(song));
    }
}
