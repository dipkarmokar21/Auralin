package components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import config.Constants;

public class Sidebar extends VBox {
    private VBox navButtons;
    private ProgressBar loadingBar = new ProgressBar(0);
    private Label lblLoadingStatus = new Label("");
    private String activeView = "Home";
    private SidebarListener listener;

    // Heart SVG path — same icon used in PlayerBar and SongTable
    private static final String HEART_PATH = loadHeartSvgPath();

    public interface SidebarListener {
        void onNavigate(String view);
    }

    public Sidebar(SidebarListener listener) {
        super(25);
        this.listener = listener;
        setupUI();
    }

    private void setupUI() {
        setPrefWidth(240);
        setPadding(new Insets(30, 20, 20, 20));
        setStyle("-fx-background-color: " + Constants.SIDEBAR_BLACK + ";");

        Label logo = new Label("Auralin");
        logo.setFont(Font.font("System", FontWeight.BOLD, 28));
        logo.setTextFill(Color.web("#FA2D48"));

        navButtons = new VBox(15);
        updateNavButtons();

        VBox loadingBox = new VBox(5, lblLoadingStatus, loadingBar);
        loadingBar.setMaxWidth(Double.MAX_VALUE);
        loadingBar.setVisible(false);
        lblLoadingStatus.setTextFill(Color.web(Constants.SPOTIFY_GREEN));
        lblLoadingStatus.setFont(Font.font(11));

        getChildren().addAll(logo, navButtons, new Separator(), loadingBox);
    }

    private void updateNavButtons() {
        navButtons.getChildren().clear();
        // ORDER: Home → Search → Your Library → Liked Songs (directly below Library)
        navButtons.getChildren().addAll(
            createNavBtn("Home",         "🏠", activeView.equals("Home"),    e -> navigate("Home")),
            createNavBtn("Search",       "🔍", activeView.equals("Search"),  e -> navigate("Search")),
            createNavBtn("Your Library", "📚", activeView.equals("Library"), e -> navigate("Library")),
            createNavBtnWithSvgHeart("Liked Songs",           activeView.equals("Liked"),  e -> navigate("Liked"))
        );
    }

    /** Standard nav button with an emoji icon
     * CHANGED: manual hover added for consistent behavior with SVG heart button
     */
    private Button createNavBtn(String text, String icon, boolean isActive,
                                javafx.event.EventHandler<javafx.event.ActionEvent> event) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-btn");
        btn.setFont(Font.font("System", FontWeight.BOLD, 16));

        if (isActive) {
            btn.setStyle("-fx-text-fill: " + Constants.SPOTIFY_GREEN + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-text-fill: " + Constants.COLOR_GRAY_TEXT + "; -fx-font-size: 16px; -fx-font-weight: bold;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"));
            btn.setOnMouseExited(e  -> btn.setStyle("-fx-text-fill: " + Constants.COLOR_GRAY_TEXT + "; -fx-font-size: 16px; -fx-font-weight: bold;"));
        }

        btn.setOnAction(event);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    /**
     * Liked Songs nav button — SVG heart icon instead of emoji.
     * CHANGED: hover effect added — heart and text both change color on mouse enter/exit
     */
    private Button createNavBtnWithSvgHeart(String text, boolean isActive,
                                            javafx.event.EventHandler<javafx.event.ActionEvent> event) {
        SVGPath heartIcon = new SVGPath();
        heartIcon.setContent(HEART_PATH);
        heartIcon.setMouseTransparent(true);

        Color activeColor  = Color.web("#FA2D48");
        Color normalColor  = Color.web(Constants.COLOR_GRAY_TEXT);
        Color hoverColor   = Color.WHITE;

        if (isActive) {
            heartIcon.setFill(activeColor);
            heartIcon.setStroke(Color.TRANSPARENT);
        } else {
            heartIcon.setFill(Color.TRANSPARENT);
            heartIcon.setStroke(normalColor);
            heartIcon.setStrokeWidth(1.5);
        }

        heartIcon.setScaleX(16.0 / 24.0);
        heartIcon.setScaleY(16.0 / 24.0);

        StackPane iconBox = new StackPane(heartIcon);
        iconBox.setPrefSize(20, 20);
        iconBox.setMinSize(20, 20);

        Label lblText = new Label(" " + text);
        lblText.setTextFill(isActive ? activeColor : normalColor);
        lblText.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblText.setMouseTransparent(true);

        HBox content = new HBox(2, iconBox, lblText);
        content.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.getStyleClass().add("nav-btn");
        btn.setOnAction(event);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        // CHANGED: manual hover — CSS hover can't reach Label inside graphic
        if (!isActive) {
            btn.setOnMouseEntered(e -> {
                lblText.setTextFill(hoverColor);
                heartIcon.setStroke(hoverColor);
            });
            btn.setOnMouseExited(e -> {
                lblText.setTextFill(normalColor);
                heartIcon.setStroke(normalColor);
            });
        }

        return btn;
    }

    private void navigate(String view) {
        activeView = view;
        updateNavButtons();
        listener.onNavigate(view);
    }

    public void setActiveView(String view) {
        activeView = view;
        updateNavButtons();
    }

    public void showLoading(boolean show, String status) {
        loadingBar.setVisible(show);
        lblLoadingStatus.setText(status);
    }

    public ProgressBar getLoadingBar() {
        return loadingBar;
    }

    /** Reads the SVG heart path from resources/heart.svg */
    private static String loadHeartSvgPath() {
        try {
            java.io.InputStream is = Sidebar.class.getResourceAsStream("/resources/heart.svg");
            if (is == null) return "";
            String xml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("d=\"([^\"]+)\"").matcher(xml);
            return m.find() ? m.group(1) : "";
        } catch (Exception e) {
            return "";
        }
    }
}
