package views;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import model.Song;
import config.Constants;

/**
 * NowPlayingView — true full-screen Now Playing overlay.
 *
 * CHANGES:
 *  - Covers the ENTIRE window (sidebar + playerBar hidden behind this)
 *  - Background: fully black (#000000), no blurred image
 *  - Large centered album art with drop shadow
 *  - Song title + artist centered below the art
 *  - Heart button: toggles like/unlike WITHOUT closing the screen
 *  - ✕ close button top-right, Escape key also closes
 *
 * Wired in Main.java: overlaid on the root StackPane (not contentArea)
 * so it covers everything including sidebar and playerBar.
 */
public class NowPlayingView extends StackPane {

    public interface NowPlayingListener {
        void onClose();  // ✕ or Escape — returns to previous screen
        void onLove();   // heart clicked — toggles like, does NOT close
    }

    private static final String HEART_PATH = loadHeartSvgPath();

    // ── UI nodes ──────────────────────────────────────────────────────────────
    private final ImageView mainArt      = new ImageView(); // large centered artwork
    private final Label     lblTitle     = new Label("No song playing");
    private final Label     lblArtist    = new Label("-");
    private final SVGPath   heartOutline = new SVGPath();
    private final SVGPath   heartFilled  = new SVGPath();
    private final StackPane btnHeart     = new StackPane(heartOutline, heartFilled);

    private final NowPlayingListener listener;

    public NowPlayingView(NowPlayingListener listener) {
        this.listener = listener;
        buildUI();
    }

    private void buildUI() {
        // Full black background
        setStyle("-fx-background-color: #000000;");

        // ── "Now Playing" heading (top-center) ────────────────────────────────
        // CHANGED: larger, bolder, fully opaque so it's clearly visible
        Label lblHeading = new Label("Now Playing");
        lblHeading.setTextFill(Color.WHITE);
        lblHeading.setFont(Font.font("System", FontWeight.BOLD, 20));
        StackPane.setAlignment(lblHeading, Pos.TOP_CENTER);
        StackPane.setMargin(lblHeading, new Insets(26, 0, 0, 0));

        // ── Close button (top-right corner) ───────────────────────────────────
        Label btnClose = new Label("✕");
        btnClose.setTextFill(Color.web(Constants.COLOR_GRAY_TEXT));
        btnClose.setFont(Font.font(24));
        btnClose.setStyle("-fx-cursor: hand;");
        btnClose.setOnMouseClicked(e -> listener.onClose());
        btnClose.setOnMouseEntered(e -> btnClose.setTextFill(Color.WHITE));
        btnClose.setOnMouseExited(e -> btnClose.setTextFill(Color.web(Constants.COLOR_GRAY_TEXT)));
        StackPane.setAlignment(btnClose, Pos.TOP_RIGHT);
        StackPane.setMargin(btnClose, new Insets(24));

        // ── Large centered album art ──────────────────────────────────────────
        mainArt.setPreserveRatio(true);
        mainArt.setFitWidth(340);
        mainArt.setFitHeight(340);
        mainArt.setEffect(new DropShadow(40, Color.BLACK));

        // ── Song title — centered below the artwork ───────────────────────────
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        lblTitle.setMaxWidth(380);
        lblTitle.setWrapText(false);
        lblTitle.setAlignment(Pos.CENTER);  // CHANGED: text centered

        // ── Artist name — centered ────────────────────────────────────────────
        lblArtist.setTextFill(Color.web(Constants.COLOR_GRAY_TEXT));
        lblArtist.setFont(Font.font(16));
        lblArtist.setAlignment(Pos.CENTER); // CHANGED: text centered

        // ── Heart button ──────────────────────────────────────────────────────
        // CHANGED: clicking heart toggles like/unlike but does NOT close the screen
        buildHeartButton();

        // ── Center layout: art → title → artist → heart ───────────────────────
        VBox center = new VBox(18, mainArt, lblTitle, lblArtist, btnHeart);
        center.setAlignment(Pos.CENTER);
        center.setMaxWidth(420);

        // Stack: black bg, centered content, "Now Playing" heading top-center, close button top-right
        getChildren().addAll(center, lblHeading, btnClose);
        setAlignment(Pos.CENTER);

        // Escape key closes without affecting like state
        setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) listener.onClose();
        });
        setFocusTraversable(true);
    }

    /** Two-layer SVG heart: white outline (unliked) / red filled (liked) */
    private void buildHeartButton() {
        heartOutline.setContent(HEART_PATH);
        heartOutline.setFill(Color.TRANSPARENT);
        heartOutline.setStroke(Color.web(Constants.COLOR_GRAY_TEXT));
        heartOutline.setStrokeWidth(1.5);
        heartOutline.setMouseTransparent(true);

        heartFilled.setContent(HEART_PATH);
        heartFilled.setFill(Color.web(Constants.COLOR_RED));
        heartFilled.setStroke(Color.TRANSPARENT);
        heartFilled.setOpacity(0); // hidden until liked
        heartFilled.setMouseTransparent(true);

        // Scale 24-unit viewBox → 30px
        double scale = 30.0 / 24.0;
        heartOutline.setScaleX(scale); heartOutline.setScaleY(scale);
        heartFilled.setScaleX(scale);  heartFilled.setScaleY(scale);

        btnHeart.setAlignment(Pos.CENTER);
        btnHeart.setPrefSize(40, 40);
        btnHeart.setStyle("-fx-cursor: hand;");
        // CHANGED: onLove() toggles like — Main.java does NOT close the view after this
        btnHeart.setOnMouseClicked(e -> listener.onLove());
    }

    // ── Public update methods (called from Main.java) ─────────────────────────

    /** Refreshes all content when a new song starts or the view is opened */
    public void updateSong(Song song) {
        if (song == null) return;
        mainArt.setImage(song.getArtwork());
        lblTitle.setText(song.getTitle());
        lblArtist.setText(song.getArtist());
        updateHeartState(song.isLiked());
    }

    /**
     * Syncs the heart icon with the current liked state.
     * CHANGED: called after onLove() so the icon updates live without closing.
     */
    public void updateHeartState(boolean isLiked) {
        // Pop animation on the heart button
        ScaleTransition pop = new ScaleTransition(Duration.millis(120), btnHeart);
        pop.setFromX(1.0); pop.setFromY(1.0);
        pop.setToX(1.3);   pop.setToY(1.3);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.play();

        if (isLiked) {
            heartFilled.setOpacity(1);
            heartOutline.setStroke(Color.TRANSPARENT);
            heartFilled.setEffect(new DropShadow(10, Color.web(Constants.COLOR_RED)));
        } else {
            heartFilled.setOpacity(0);
            heartOutline.setStroke(Color.web(Constants.COLOR_GRAY_TEXT));
            heartFilled.setEffect(null);
        }
    }

    private static String loadHeartSvgPath() {
        try {
            java.io.InputStream is =
                NowPlayingView.class.getResourceAsStream("/resources/heart.svg");
            if (is == null) return "";
            String xml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("d=\"([^\"]+)\"").matcher(xml);
            return m.find() ? m.group(1) : "";
        } catch (Exception e) {
            return "";
        }
    }
}
