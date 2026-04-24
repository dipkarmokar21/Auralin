package components;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.OverrunStyle;
import javafx.util.Duration;
import model.Song;
import config.Constants;

/**
 * PlayerBar — bottom bar: now-playing info, playback controls, progress + volume sliders.
 */
public class PlayerBar extends HBox {

    // ── Listener ─────────────────────────────────────────────────────────────
    public interface PlayerBarListener {
        void onPlayPause();
        void onNext();
        void onPrevious();
        void onShuffle(boolean enabled);
        void onRepeat(int mode);         // 0 = off, 1 = on
        void onLove();
        void onSeek(double position);    // 0.0–1.0
        void onVolumeChange(double volume);
        void onArtworkClick();           // opens / closes NowPlayingView
    }

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final String COLOR_ACTIVE = "#FA2D48";
    private static final String COLOR_TRACK  = "#4D4D4D";
    private static final String COLOR_GRAY   = "#B3B3B3";

    private static final String HEART_PATH = loadHeartSvgPath();

    // ── UI fields ─────────────────────────────────────────────────────────────
    private final Label     lblTitle       = new Label("Select a song");
    private final Label     lblArtist      = new Label("-");
    private final Label     lblTimeCurrent = new Label("0:00");
    private final Label     lblTimeTotal   = new Label("0:00");
    private final ImageView imgArtwork     = new ImageView(new Image(Constants.DEFAULT_ART));
    private final Button    btnPlay        = new Button();   // SVG play/pause
    private final Slider    progressSlider = new Slider(0, 100, 0);
    private final Slider    volSlider      = new Slider(0, 1, 0.7);
    private final SVGPath   heartOutline   = new SVGPath();
    private final SVGPath   heartFilled    = new SVGPath();
    private final StackPane btnLove        = new StackPane(heartOutline, heartFilled);

    private final PlayerBarListener listener;
    private boolean isDragging     = false;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled  = false;

    // CHANGED: mutual exclusion — only one of shuffle/repeat can be active at a time
    private Button btnShuffleRef; // kept so repeat can turn it off
    private Button btnRepeatRef;  // kept so shuffle can turn it off

    private javafx.scene.Node progressTrackNode = null;
    private javafx.scene.Node volTrackNode      = null;

    // ── SVG paths for premium play / pause icons ──────────────────────────────
    // Play triangle (pointing right)
    private static final String PLAY_PATH  =
        "M8 5v14l11-7z";
    // Pause (two rectangles)
    private static final String PAUSE_PATH =
        "M6 19h4V5H6v14zm8-14v14h4V5h-4z";

    public PlayerBar(PlayerBarListener listener) {
        super(40);
        this.listener = listener;
        buildUI();
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    private void buildUI() {
        setPadding(new Insets(15, 30, 15, 30));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #000000; -fx-border-color: #282828; -fx-border-width: 1 0 0 0;");
        setPrefHeight(120);
        getChildren().addAll(buildInfoSection(), buildCenterSection(), buildVolumeSection());
    }

    /** Left: artwork (clickable) + title + artist + heart */
    private HBox buildInfoSection() {
        imgArtwork.setFitWidth(60);
        imgArtwork.setFitHeight(60);
        imgArtwork.setStyle("-fx-cursor: hand;");
        // CHANGED: artwork click toggles NowPlayingView open/close (handled in Main)
        imgArtwork.setOnMouseClicked(e -> listener.onArtworkClick());

        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font(null, FontWeight.BOLD, 14));
        lblTitle.setMaxWidth(180);
        lblTitle.setTextOverrun(OverrunStyle.ELLIPSIS);

        lblArtist.setTextFill(Color.web(COLOR_GRAY));
        lblArtist.setMaxWidth(180);
        lblArtist.setTextOverrun(OverrunStyle.ELLIPSIS);

        VBox songText = new VBox(2, lblTitle, lblArtist);
        songText.setAlignment(Pos.CENTER_LEFT);

        buildHeartButton();

        HBox info = new HBox(15, imgArtwork, songText, btnLove);
        // CHANGED: Pos.CENTER keeps all items vertically centered in the PlayerBar
        info.setAlignment(Pos.CENTER);
        info.setMinWidth(320);
        info.setMaxWidth(320);
        return info;
    }

    private void buildHeartButton() {
        heartOutline.setContent(HEART_PATH);
        heartOutline.setFill(Color.TRANSPARENT);
        heartOutline.setStroke(Color.web(COLOR_GRAY));
        heartOutline.setStrokeWidth(1.5);
        heartOutline.setMouseTransparent(true);

        heartFilled.setContent(HEART_PATH);
        heartFilled.setFill(Color.web(COLOR_ACTIVE));
        heartFilled.setStroke(Color.TRANSPARENT);
        heartFilled.setOpacity(0);
        heartFilled.setMouseTransparent(true);

        btnLove.setAlignment(Pos.CENTER);
        btnLove.setPrefSize(28, 28);
        btnLove.setStyle("-fx-cursor: hand;");
        btnLove.setOnMouseClicked(e -> listener.onLove());
    }

    /** Center: shuffle / prev / play-pause / next / repeat + progress bar */
    private VBox buildCenterSection() {
        HBox controls = buildPlaybackControls();

        progressSlider.getStyleClass().add("spotify-slider");
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        progressSlider.setPrefWidth(1);
        progressSlider.setOnMousePressed(e -> {
            isDragging = true;
            progressSlider.setValue(Math.max(0, Math.min(1, e.getX() / progressSlider.getWidth())) * 100);
        });
        progressSlider.setOnMouseDragged(e ->
            progressSlider.setValue(Math.max(0, Math.min(1, e.getX() / progressSlider.getWidth())) * 100)
        );
        progressSlider.setOnMouseReleased(e -> {
            isDragging = false;
            listener.onSeek(progressSlider.getValue() / 100.0);
        });
        progressSlider.valueProperty().addListener((obs, old, val) ->
            updateSliderColor(progressSlider, val.doubleValue() / progressSlider.getMax(),
                progressTrackNode, node -> progressTrackNode = node)
        );

        lblTimeCurrent.setTextFill(Color.web(COLOR_GRAY));
        lblTimeTotal.setTextFill(Color.web(COLOR_GRAY));
        lblTimeCurrent.setMinWidth(38);
        lblTimeTotal.setMinWidth(38);
        lblTimeTotal.setAlignment(Pos.CENTER_RIGHT);

        StackPane sliderWrapper = new StackPane(progressSlider);
        HBox.setHgrow(sliderWrapper, Priority.ALWAYS);
        sliderWrapper.setMaxWidth(500);

        HBox timeRow = new HBox(10, lblTimeCurrent, sliderWrapper, lblTimeTotal);
        timeRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(timeRow, Priority.ALWAYS);

        VBox center = new VBox(8, controls, timeRow);
        center.setAlignment(Pos.CENTER);
        HBox.setHgrow(center, Priority.ALWAYS);
        return center;
    }

    /** Builds shuffle / prev / play-pause / next / repeat row */
    private HBox buildPlaybackControls() {
        btnShuffleRef = createIconBtn("🔀", 18);
        Button btnPrev = createIconBtn("⏮", 20);
        Button btnNext = createIconBtn("⏭", 20);
        btnRepeatRef  = createIconBtn("🔁", 18);

        // ── CHANGED: premium SVG play/pause button ────────────────────────────
        // Uses an SVG path inside a white circle instead of a plain text button
        SVGPath playIcon = new SVGPath();
        playIcon.setContent(PLAY_PATH);
        playIcon.setFill(Color.BLACK);
        // Scale the 24-unit Material icon viewBox to ~20px
        playIcon.setScaleX(20.0 / 24.0);
        playIcon.setScaleY(20.0 / 24.0);
        playIcon.setMouseTransparent(true);

        Circle playCircleBg = new Circle(22, Color.WHITE);
        playCircleBg.setMouseTransparent(true);

        StackPane playBtn = new StackPane(playCircleBg, playIcon);
        playBtn.setPrefSize(44, 44);
        playBtn.setStyle("-fx-cursor: hand;");
        playBtn.setOnMouseClicked(e -> listener.onPlayPause());
        // Subtle scale on hover
        playBtn.setOnMouseEntered(e -> playBtn.setScaleX(1.08));
        playBtn.setOnMouseEntered(e -> { playBtn.setScaleX(1.08); playBtn.setScaleY(1.08); });
        playBtn.setOnMouseExited(e ->  { playBtn.setScaleX(1.0);  playBtn.setScaleY(1.0);  });

        // Store playIcon so updatePlayButton() can swap the path
        playBtn.setUserData(playIcon);

        // Keep a reference so updatePlayButton can find the icon
        btnPlay.setUserData(playBtn); // btnPlay is now just a holder for the StackPane ref

        btnPrev.setOnAction(e -> listener.onPrevious());
        btnNext.setOnAction(e -> listener.onNext());

        // CHANGED: shuffle turns OFF repeat if it was on (mutual exclusion)
        btnShuffleRef.setOnAction(e -> {
            shuffleEnabled = !shuffleEnabled;
            if (shuffleEnabled && repeatEnabled) {
                // Turn off repeat
                repeatEnabled = false;
                btnRepeatRef.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
                listener.onRepeat(0);
            }
            btnShuffleRef.setStyle("-fx-text-fill: " +
                (shuffleEnabled ? Constants.COLOR_RED : "white") + "; -fx-font-size: 18px;");
            listener.onShuffle(shuffleEnabled);
        });

        // CHANGED: repeat turns OFF shuffle if it was on (mutual exclusion)
        btnRepeatRef.setOnAction(e -> {
            repeatEnabled = !repeatEnabled;
            if (repeatEnabled && shuffleEnabled) {
                // Turn off shuffle
                shuffleEnabled = false;
                btnShuffleRef.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
                listener.onShuffle(false);
            }
            btnRepeatRef.setStyle("-fx-text-fill: " +
                (repeatEnabled ? Constants.COLOR_RED : "white") + "; -fx-font-size: 18px;");
            listener.onRepeat(repeatEnabled ? 1 : 0);
        });

        HBox controls = new HBox(20, btnShuffleRef, btnPrev, playBtn, btnNext, btnRepeatRef);
        controls.setAlignment(Pos.CENTER);
        return controls;
    }

    /** Right: volume icon + slider */
    private HBox buildVolumeSection() {
        Label lblVolIcon = new Label("🔊");
        lblVolIcon.setTextFill(Color.WHITE);
        lblVolIcon.setStyle("-fx-font-size: 22px;");

        volSlider.setPrefWidth(100);
        volSlider.getStyleClass().add("spotify-slider");
        volSlider.setOnMousePressed(e ->
            volSlider.setValue(Math.max(0, Math.min(1, e.getX() / volSlider.getWidth())))
        );
        volSlider.setOnMouseDragged(e ->
            volSlider.setValue(Math.max(0, Math.min(1, e.getX() / volSlider.getWidth())))
        );
        volSlider.valueProperty().addListener((obs, old, val) -> {
            updateSliderColor(volSlider, val.doubleValue() / volSlider.getMax(),
                volTrackNode, node -> volTrackNode = node);
            listener.onVolumeChange(val.doubleValue());
        });
        volSlider.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) javafx.application.Platform.runLater(() ->
                updateSliderColor(volSlider, volSlider.getValue() / volSlider.getMax(),
                    volTrackNode, node -> volTrackNode = node)
            );
        });

        HBox vol = new HBox(15, lblVolIcon, volSlider);
        vol.setAlignment(Pos.CENTER_RIGHT);
        vol.setMinWidth(250);
        return vol;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateSliderColor(Slider slider, double fraction,
                                   javafx.scene.Node cached,
                                   java.util.function.Consumer<javafx.scene.Node> cache) {
        if (cached == null) {
            cached = slider.lookup(".track");
            if (cached != null) cache.accept(cached);
        }
        if (cached != null) {
            double pct = fraction * 100;
            cached.setStyle(String.format(
                "-fx-background-color: linear-gradient(to right, %s %.4f%%, %s %.4f%%);",
                COLOR_ACTIVE, pct, COLOR_TRACK, pct));
        }
    }

    private Button createIconBtn(String icon, int fontSize) {
        Button btn = new Button(icon);
        btn.getStyleClass().add("control-icon");
        btn.setStyle("-fx-font-size: " + fontSize + "px;");
        return btn;
    }

    private static String loadHeartSvgPath() {
        try {
            java.io.InputStream is = PlayerBar.class.getResourceAsStream("/resources/heart.svg");
            if (is == null) return "";
            String xml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("d=\"([^\"]+)\"").matcher(xml);
            return m.find() ? m.group(1) : "";
        } catch (Exception e) { return ""; }
    }

    // ── Public update methods ─────────────────────────────────────────────────

    public void updateSongInfo(Song song) {
        lblTitle.setText(song.getTitle());
        lblArtist.setText(song.getArtist());
        imgArtwork.setImage(song.getArtwork());
    }

    public void updateLoveButton(boolean isLiked) {
        ScaleTransition pop = new ScaleTransition(Duration.millis(120), btnLove);
        pop.setFromX(1.0); pop.setFromY(1.0);
        pop.setToX(1.35);  pop.setToY(1.35);
        pop.setAutoReverse(true); pop.setCycleCount(2); pop.play();
        if (isLiked) {
            heartFilled.setOpacity(1);
            heartOutline.setStroke(Color.TRANSPARENT);
            heartFilled.setEffect(new DropShadow(8, Color.web(COLOR_ACTIVE)));
        } else {
            heartFilled.setOpacity(0);
            heartOutline.setStroke(Color.web(COLOR_GRAY));
            heartFilled.setEffect(null);
        }
    }

    /**
     * CHANGED: swaps the SVG path inside the play button circle.
     * Play = triangle, Pause = two bars — both look premium on the white circle.
     */
    public void updatePlayButton(boolean isPlaying) {
        // Find the StackPane stored in btnPlay.userData, then find the SVGPath inside it
        if (btnPlay.getUserData() instanceof StackPane playBtn) {
            playBtn.getChildren().stream()
                .filter(n -> n instanceof SVGPath)
                .map(n -> (SVGPath) n)
                .findFirst()
                .ifPresent(icon -> icon.setContent(isPlaying ? PAUSE_PATH : PLAY_PATH));
        }
    }

    public void updateProgress(double fraction) {
        if (!isDragging) progressSlider.setValue(fraction * 100);
    }

    public void updateTime(String current, String total) {
        if (!isDragging) lblTimeCurrent.setText(current);
        lblTimeTotal.setText(total);
    }

    public double getVolume() { return volSlider.getValue(); }
}
