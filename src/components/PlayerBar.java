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
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.animation.ScaleTransition;
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
    private javafx.scene.Node cachedTrack = null;
    private boolean isDragging = false;
    private Slider volSlider = new Slider(0, 1, 0.7);
    private Button btnPlay = new Button("▶");

    // heart.svg — loaded from src/resources/heart.svg (viewBox 0 0 512 512)
    private static final String HEART_PATH = loadHeartPath();

    private static String loadHeartPath() {
        try {
            java.io.InputStream is = PlayerBar.class.getResourceAsStream("/resources/heart.svg");
            if (is == null) return "";
            String xml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("d=\"([^\"]+)\"").matcher(xml);
            return m.find() ? m.group(1) : "";
        } catch (Exception e) { return ""; }
    }

    private SVGPath heartOutlinePath = new SVGPath();
    private SVGPath heartFilledPath  = new SVGPath();
    private StackPane btnLove = new StackPane(heartOutlinePath, heartFilledPath);
    private boolean shuffleEnabled = false;
    private int repeatMode = 0; // 0=off, 1=one, 2=all
    
    private PlayerBarListener listener;
    
    public interface PlayerBarListener {
        void onPlayPause();
        void onNext();
        void onPrevious();
        void onShuffle(boolean enabled);
        void onRepeat(int mode);
        void onLove();
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
        
        // Outline heart — always visible, gray stroke
        heartOutlinePath.setContent(HEART_PATH);
        heartOutlinePath.setFill(Color.TRANSPARENT);
        heartOutlinePath.setStroke(Color.web("#B3B3B3"));
        heartOutlinePath.setStrokeWidth(1.5);
        double scale = 1.0; // 24 viewBox → 24px
        heartOutlinePath.getTransforms().add(new Scale(scale, scale));
        heartOutlinePath.setMouseTransparent(true);

        // Filled heart — same path, red fill, hidden by default
        heartFilledPath.setContent(HEART_PATH);
        heartFilledPath.setFill(Color.web("#FA2D48"));
        heartFilledPath.setStroke(Color.TRANSPARENT);
        heartFilledPath.getTransforms().add(new Scale(scale, scale));
        heartFilledPath.setOpacity(0);
        heartFilledPath.setMouseTransparent(true);

        btnLove.setAlignment(Pos.CENTER);
        btnLove.setPrefSize(28, 28);
        btnLove.setStyle("-fx-cursor: hand;");
        btnLove.setOnMouseClicked(e -> listener.onLove());
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
            shuffleEnabled = !shuffleEnabled;
            bS.setStyle("-fx-text-fill: " + (shuffleEnabled ? Constants.SPOTIFY_GREEN : "white") + "; -fx-font-size: 18px;");
            listener.onShuffle(shuffleEnabled);
        });
        bR.setOnAction(e -> {
            repeatMode = (repeatMode + 1) % 3;
            switch (repeatMode) {
                case 0 -> { bR.setText("🔁"); bR.setStyle("-fx-text-fill: white; -fx-font-size: 18px;"); }
                case 1 -> { bR.setText("🔂"); bR.setStyle("-fx-text-fill: " + Constants.SPOTIFY_GREEN + "; -fx-font-size: 18px;"); }
                case 2 -> { bR.setText("🔁"); bR.setStyle("-fx-text-fill: " + Constants.SPOTIFY_GREEN + "; -fx-font-size: 18px;"); }
            }
            listener.onRepeat(repeatMode);
        });
        
        btns.getChildren().addAll(bS, bP, btnPlay, bN, bR);
        
        StackPane sliderStack = new StackPane();
        HBox.setHgrow(sliderStack, Priority.ALWAYS);
        sliderStack.setMaxWidth(500);
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        progressSlider.setPrefHeight(4);
        progressSlider.setPrefWidth(1);
        progressSlider.getStyleClass().add("spotify-slider");
        
        progressSlider.setOnMousePressed(e -> isDragging = true);
        progressSlider.setOnMouseReleased(e -> {
            isDragging = false;
            listener.onSeek(progressSlider.getValue() / 100.0);
        });
        progressSlider.setOnMouseClicked(e -> {
            double pct = e.getX() / progressSlider.getWidth();
            progressSlider.setValue(pct * 100);
            listener.onSeek(pct);
        });
        
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (cachedTrack == null) cachedTrack = progressSlider.lookup(".track");
            if (cachedTrack != null) {
                double percent = (newVal.doubleValue() / progressSlider.getMax()) * 100;
                cachedTrack.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to right, #FA2D48 %.4f%%, #4D4D4D %.4f%%);",
                    percent, percent
                ));
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
        
        volSlider.setPrefWidth(100);
        volSlider.valueProperty().addListener((o, old, nv) -> listener.onVolumeChange(nv.doubleValue()));
        
        Label lblVol = new Label("🔊");
        lblVol.setTextFill(Color.WHITE);
        lblVol.setStyle("-fx-font-size: 22px;");
        extra.getChildren().addAll(lblVol, volSlider);
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
        ScaleTransition pop = new ScaleTransition(Duration.millis(120), btnLove);
        pop.setFromX(1.0); pop.setFromY(1.0);
        pop.setToX(1.35);  pop.setToY(1.35);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.play();

        if (isLiked) {
            heartFilledPath.setOpacity(1);
            heartOutlinePath.setStroke(Color.TRANSPARENT);
            DropShadow glow = new DropShadow(8, Color.web("#FA2D48"));
            heartFilledPath.setEffect(glow);
        } else {
            heartFilledPath.setOpacity(0);
            heartOutlinePath.setStroke(Color.web("#B3B3B3"));
            heartFilledPath.setEffect(null);
        }
    }
    
    public void updatePlayButton(boolean isPlaying) {
        btnPlay.setText(isPlaying ? "⏸" : "▶");
    }
    
    public void updateProgress(double percent) {
        if (!isDragging) {
            progressSlider.setValue(percent * 100);
        }
    }
    
    public void updateTime(String current, String total) {
        if (!isDragging) {
            lblTimeCurrent.setText(current);
        }
        lblTimeTotal.setText(total);
    }
    
    public double getVolume() {
        return volSlider.getValue();
    }
}
