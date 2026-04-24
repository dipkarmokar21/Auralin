package components;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import model.Song;
import config.Constants;

/**
 * MusicCard — a single song card shown in the Home view grid.
 *
 * CHANGES:
 *  - Fixed preferred width to 160px (normal size, not oversized)
 *  - Image is square and fills the card width dynamically via property binding
 *  - Rounded corners on image box (background-radius: 12)
 *  - Hover: smooth scale-up animation (1.0 → 1.05)
 *  - Padding inside card for clean spacing
 */
public class MusicCard extends VBox {

    // Fixed card width — FlowPane will fit as many per row as the screen allows
    private static final double CARD_WIDTH = 160;

    private final Song song;
    private final MusicCardListener listener;
    private StackPane playPauseOverlay;
    private SVGPath iconPath;

    public interface MusicCardListener {
        void onCardClick(Song song);
        boolean isCurrentSong(Song song);
        boolean isPlaying(Song song);
    }

    public MusicCard(Song song, MusicCardListener listener) {
        super(8);
        this.song = song;
        this.listener = listener;
        buildCard();
    }

    private void buildCard() {
        getStyleClass().add("music-card");
        setPadding(new Insets(10));
        setPrefWidth(CARD_WIDTH);

        // ── Album artwork ─────────────────────────────────────────────────────
        // Image fills the card width minus padding (10px each side = 20px total)
        ImageView artwork = new ImageView(song.getArtwork());
        artwork.setPreserveRatio(false);
        // Bind image size to card width so it scales with the card
        double imgSize = CARD_WIDTH - 20;
        artwork.setFitWidth(imgSize);
        artwork.setFitHeight(imgSize);

        StackPane imgBox = new StackPane(artwork);
        imgBox.setPrefSize(imgSize, imgSize);
        imgBox.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 12;");

        // Premium red circle play/pause icon at bottom-right corner
        if (listener.isCurrentSong(song)) {
            Circle playBg = new Circle(22, Color.web(Constants.COLOR_RED));
            playBg.setMouseTransparent(true);

            iconPath = new SVGPath();
            iconPath.setFill(Color.WHITE);
            iconPath.setMouseTransparent(true);
            
            // Set initial icon based on playing state (no animation on first load)
            boolean isPlaying = listener.isPlaying(song);
            if (isPlaying) {
                // Pause icon (two vertical bars)
                iconPath.setContent("M6 4h4v16H6V4zm8 0h4v16h-4V4z");
            } else {
                // Play icon (triangle)
                iconPath.setContent("M8 5v14l11-7z");
            }
            iconPath.setScaleX(0.8);
            iconPath.setScaleY(0.8);

            playPauseOverlay = new StackPane(playBg, iconPath);
            playPauseOverlay.setMouseTransparent(true);
            playPauseOverlay.setPrefSize(44, 44);
            playPauseOverlay.setMaxSize(44, 44);
            playPauseOverlay.setMinSize(44, 44);

            // Position at bottom-right corner of imgBox - using translateX/Y for precise positioning
            StackPane.setAlignment(playPauseOverlay, Pos.BOTTOM_RIGHT);
            playPauseOverlay.setTranslateX(8);  // Move 8px to the right (half of circle size)
            playPauseOverlay.setTranslateY(8);  // Move 8px down (half of circle size)
            
            imgBox.getChildren().add(playPauseOverlay);
        }

        // ── Song title ────────────────────────────────────────────────────────
        Label lblTitle = new Label(song.getTitle());
        lblTitle.setTextFill(
            listener.isCurrentSong(song) ? Color.web(Constants.COLOR_RED) : Color.WHITE
        );
        lblTitle.setFont(Font.font(null, FontWeight.BOLD, 13));
        lblTitle.setMaxWidth(CARD_WIDTH - 20);
        lblTitle.setTextOverrun(OverrunStyle.ELLIPSIS);

        // ── Artist name ───────────────────────────────────────────────────────
        Label lblArtist = new Label(song.getArtist());
        lblArtist.setTextFill(Color.web(Constants.TEXT_GRAY));
        lblArtist.setMaxWidth(CARD_WIDTH - 20);
        lblArtist.setTextOverrun(OverrunStyle.ELLIPSIS);
        lblArtist.setFont(Font.font(12));

        getChildren().addAll(imgBox, lblTitle, lblArtist);

        // ── Click to play ─────────────────────────────────────────────────────
        setOnMouseClicked(e -> listener.onCardClick(song));

        // ── Hover animation: scale up on enter, back on exit ─────────────────
        // CHANGED: smooth 150ms scale transition on hover
        ScaleTransition hoverIn  = new ScaleTransition(Duration.millis(150), this);
        hoverIn.setToX(1.05); hoverIn.setToY(1.05);

        ScaleTransition hoverOut = new ScaleTransition(Duration.millis(150), this);
        hoverOut.setToX(1.0); hoverOut.setToY(1.0);

        setOnMouseEntered(e -> hoverIn.playFromStart());
        setOnMouseExited(e -> hoverOut.playFromStart());
    }

}
