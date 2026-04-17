package components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.OverrunStyle;
import javafx.util.Duration;
import model.Song;
import config.Constants;

public class PlayerBar extends HBox {
    private Label lblTitle = new Label("Select a song");
    private Label lblArtist = new Label("-");
    private Label lblTimeCurrent = new Label("0:00");
    private Label lblTimeTotal = new Label("0:00");
    private ImageView imgNowPlaying = new ImageView(new Image(Constants.DEFAULT_ART));
    private Slider progressSlider = new Slider(0, 100, 0);
    private Slider volSlider = new Slider(0, 1, 0.7);
    private Button btnPlay = new Button("▶");
    private Button btnLove = new Button("\u2661");
    private Button btnQueue = new Button("≡");
    
    private PlayerBarListener listener;
    
    public interface PlayerBarListener {
        void onPlayPause();
        void onNext();
        void onPrevious();
        void onShuffle(boolean enabled);
        void onRepeat(int mode);
        void onLove();
        void onQueue();
        void onSeek(double position);
        void onVolumeChange(double volume);
    }
    
    public PlayerBar(PlayerBarListener listener) {
        super(40);
        this.listener = listener;
        setupUI();
    }

    private void setupUI() {
        setPadding(new Insets(15, 30, 15, 30));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #000000; -fx-border-color: #282828; -fx-border-width: 1 0 0 0;");
        setPrefHeight(120);
        
        HBox info = createInfoSection();
        VBox center = createCenterSection();
        HBox extra = createExtraSection();
        
        getChildren().addAll(info, center, extra);
    }
    
    private HBox createInfoSection() {
        HBox info = new HBox(15);
        info.setAlignment(Pos.CENTER_LEFT);
        info.setMinWidth(320);
        info.setMaxWidth(320);
        imgNowPlaying.setFitHeight(60);
        imgNowPlaying.setFitWidth(60);
        
        VBox text = new VBox(2, lblTitle, lblArtist);
        text.setAlignment(Pos.CENTER_LEFT);
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font(null, FontWeight.BOLD, 14));
        lblTitle.setMaxWidth(180);
        lblTitle.setTextOverrun(OverrunStyle.ELLIPSIS);
        lblArtist.setTextFill(Color.web(Constants.TEXT_GRAY));
        lblArtist.setMaxWidth(180);
        lblArtist.setTextOverrun(OverrunStyle.ELLIPSIS);
        
        btnLove.getStyleClass().add("love-btn");
        btnLove.setOnAction(e -> listener.onLove());
        info.getChildren().addAll(imgNowPlaying, text, btnLove);
        return info;
    }
    
    private VBox createCenterSection() {
        VBox center = new VBox(8);
        center.setAlignment(Pos.CENTER);
        HBox.setHgrow(center, Priority.ALWAYS);
        
        HBox btns = new HBox(20);
        btns.setAlignment(Pos.CENTER);
        Button bS = createControlBtn("🔀", 18);
        Button bP = createControlBtn("⏮", 20);
        btnPlay.getStyleClass().add("play-circle");
        Button bN = createControlBtn("⏭", 20);
        Button bR = createControlBtn("🔁", 18);
        
        btnPlay.setOnAction(e -> listener.onPlayPause());
        bP.setOnAction(e -> listener.onPrevious());
        bN.setOnAction(e -> listener.onNext());
        bS.setOnAction(e -> {
            boolean shuffle = !bS.getStyle().contains(Constants.SPOTIFY_GREEN);
            listener.onShuffle(shuffle);
            bS.setStyle("-fx-text-fill: " + (shuffle ? Constants.SPOTIFY_GREEN : Constants.TEXT_GRAY) + ";");
        });
        bR.setOnAction(e -> {
            String currentText = bR.getText();
            int mode = currentText.equals("🔁") ? 1 : (currentText.equals("🔂") ? 2 : 0);
            listener.onRepeat(mode);
            bR.setText(mode == 1 ? "🔂" : "🔁");
            bR.setStyle("-fx-text-fill: " + (mode > 0 ? Constants.SPOTIFY_GREEN : Constants.TEXT_GRAY) + ";");
        });
        
        btns.getChildren().addAll(bS, bP, btnPlay, bN, bR);
        
        StackPane sliderStack = new StackPane();
        HBox.setHgrow(sliderStack, Priority.ALWAYS);
        sliderStack.setMaxWidth(500);
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        progressSlider.setPrefHeight(4);
        progressSlider.setPrefWidth(1);
        progressSlider.getStyleClass().add("spotify-slider");
        
        progressSlider.setOnMousePressed(e -> {});
        progressSlider.setOnMouseReleased(e -> listener.onSeek(progressSlider.getValue() / 100.0));
        progressSlider.setOnMouseClicked(e -> {
            double pct = e.getX() / progressSlider.getWidth();
            progressSlider.setValue(pct * 100);
            listener.onSeek(pct);
        });
        
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            javafx.scene.Node track = progressSlider.lookup(".track");
            if (track != null) {
                double percent = (newVal.doubleValue() / progressSlider.getMax()) * 100;
                track.setStyle(String.format("-fx-background-color: linear-gradient(to right, #FA2D48 %f%%, #4D4D4D %f%%);", percent, percent));
            }
        });
        
        sliderStack.getChildren().add(progressSlider);
        HBox timeBar = new HBox(10, lblTimeCurrent, sliderStack, lblTimeTotal);
        timeBar.setAlignment(Pos.CENTER);
        lblTimeCurrent.setTextFill(Color.web(Constants.TEXT_GRAY));
        lblTimeTotal.setTextFill(Color.web(Constants.TEXT_GRAY));
        HBox.setHgrow(timeBar, Priority.ALWAYS);
        
        center.getChildren().addAll(btns, timeBar);
        return center;
    }
    
    private HBox createExtraSection() {
        HBox extra = new HBox(15);
        extra.setAlignment(Pos.CENTER_RIGHT);
        extra.setMinWidth(250);
        
        btnQueue.getStyleClass().add("control-icon");
        btnQueue.setStyle("-fx-font-size: 22px;");
        btnQueue.setOnAction(e -> listener.onQueue());
        
        volSlider.setPrefWidth(100);
        volSlider.valueProperty().addListener((o, old, nv) -> listener.onVolumeChange(nv.doubleValue()));
        
        Label lblVol = new Label("🔊");
        lblVol.setTextFill(Color.WHITE);
        lblVol.setStyle("-fx-font-size: 22px;");
        extra.getChildren().addAll(btnQueue, lblVol, volSlider);
        return extra;
    }
    
    private Button createControlBtn(String icon, int size) {
        Button b = new Button(icon);
        b.getStyleClass().add("control-icon");
        b.setStyle("-fx-font-size: " + size + "px;");
        return b;
    }
    
    public void updateSongInfo(Song song) {
        lblTitle.setText(song.getTitle());
        lblArtist.setText(song.getArtist());
        imgNowPlaying.setImage(song.getArtwork());
    }
    
    public void updateLoveButton(boolean isLiked) {
        if (isLiked) {
            btnLove.setText("❤");
            btnLove.setStyle("-fx-text-fill: " + Constants.SPOTIFY_GREEN + ";");
        } else {
            btnLove.setText("\u2661");
            btnLove.setStyle("-fx-text-fill: white;");
        }
    }
    
    public void updatePlayButton(boolean isPlaying) {
        btnPlay.setText(isPlaying ? "⏸" : "▶");
    }
    
    public void updateProgress(double percent) {
        progressSlider.setValue(percent * 100);
    }
    
    public void updateTime(String current, String total) {
        lblTimeCurrent.setText(current);
        lblTimeTotal.setText(total);
    }
    
    public double getVolume() {
        return volSlider.getValue();
    }
}
