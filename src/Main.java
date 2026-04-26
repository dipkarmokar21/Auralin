import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import components.PlayerBar;
import components.Sidebar;
import controllers.PlayerController;
import managers.ViewManager;
import model.Song;
import services.DatabaseService;
import utils.FileImportService;
import config.Constants;
import views.NowPlayingView;
import javafx.util.Duration;

public class Main extends Application {

    private final DatabaseService db          = new DatabaseService();
    private final StackPane       contentArea = new StackPane();
    private final String          userName    = "User";

    private Sidebar           sidebar;
    private PlayerBar         playerBar;
    private PlayerController  playerController;
    private ViewManager       viewManager;
    private FileImportService fileImportService;

    private final StackPane upperStack = new StackPane();
    private NowPlayingView nowPlayingView = null;

    // Resize state
    private double resizeStartX, resizeStartY, resizeStartW, resizeStartH, resizeStartSX, resizeStartSY;
    private static final int RESIZE_MARGIN = 6;

    private static String[] appArgs;

    public static void main(String[] args) {
        appArgs = args;
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        fileImportService = new FileImportService(db);

        playerBar = new PlayerBar(createPlayerBarListener());
        playerController = new PlayerController(db, playerBar);

        playerController.setOnSongChange(() -> {
            String active = viewManager.getActiveView();
            if (active.equals("Library") || active.equals("Search") || active.equals("Liked")) {
                viewManager.refreshTableOnly();
            } else {
                viewManager.refreshCurrentView();
            }
            // CHANGED: always refresh Home cards so icon updates live from any view
            viewManager.refreshHomeCards();
            if (isNowPlayingOpen()) nowPlayingView.updateSong(db.getCurrent());
        });

        playerController.setOnLikeChange(() -> {
            String activeView = viewManager.getActiveView();
            if (activeView.equals("Liked")) {
                viewManager.refreshCurrentView();
            } else if (activeView.equals("Search") || activeView.equals("Library")) {
                viewManager.refreshTableOnly();
            }
            if (isNowPlayingOpen()) {
                Song cur = db.getCurrent();
                if (cur != null) nowPlayingView.updateHeartState(cur.isLiked());
            }
        });

        playerController.setOnPlayStateChange(() -> {
            // CHANGED: always refresh Home cards on play/pause toggle
            viewManager.refreshHomeCards();
        });

        viewManager = new ViewManager(db, contentArea, userName, playerController);
        viewManager.setFileImportCallback(new ViewManager.FileImportCallback() {
            @Override public void onAddFiles() { fileImportService.importFiles(); }
            @Override public void onAddFolder() {
                fileImportService.importFolder(sidebar.getLoadingBar(),
                    new FileImportService.LoadingCallback() {
                        @Override public void onLoadingStart()            { sidebar.showLoading(true,  "Loading..."); }
                        @Override public void onProgress(int c, int t)   { sidebar.showLoading(true,  "Loading: " + c + "/" + t); }
                        @Override public void onLoadingComplete()         { sidebar.showLoading(false, ""); }
                    });
            }
        });

        sidebar = new Sidebar(view -> viewManager.showView(view));
        viewManager.showView("Home");

        // Auto-scan disabled

        // "Open with" support — if launched with a file argument, load and play it
        if (appArgs != null && appArgs.length > 0) {
            java.io.File argFile = new java.io.File(appArgs[0]);
            if (argFile.exists() && argFile.getName().toLowerCase().endsWith(".mp3")) {
                javafx.application.Platform.runLater(() -> {
                    db.addFile(argFile);
                    viewManager.showView("Library");
                    // small delay to let MediaPlayer load duration
                    javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
                    delay.setOnFinished(e -> {
                        db.getAllSongs().stream()
                            .filter(s -> s.getFilePath().equals(argFile.getAbsolutePath()))
                            .findFirst()
                            .ifPresent(playerController::play);
                    });
                    delay.play();
                });
            }
        }

        BorderPane titleBar = createTitleBar(stage);

        BorderPane innerLayout = new BorderPane();
        innerLayout.setStyle("-fx-background-color: " + Constants.BG_BLACK + ";");
        innerLayout.setLeft(sidebar);
        innerLayout.setCenter(contentArea);

        upperStack.getChildren().add(innerLayout);
        upperStack.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        BorderPane outerLayout = new BorderPane();
        outerLayout.setTop(titleBar);
        outerLayout.setCenter(upperStack);
        outerLayout.setBottom(playerBar);
        outerLayout.setStyle("-fx-background-color: #000000;");
        outerLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Wrap in StackPane with rounded corners + border
        StackPane root = new StackPane(outerLayout);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setStyle(
            "-fx-background-color: #000000;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #2a2a2a;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;"
        );

        // Screen-aware sizing
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double winW = Math.min(1250, screenBounds.getWidth() - 20);
        double winH = Math.min(850, screenBounds.getHeight() - 20);

        Scene scene = new Scene(root, winW, winH);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        applyGlobalStyles(scene);

        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        stage.setTitle("Auralin Music Player");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setWidth(winW);
        stage.setHeight(winH);
        stage.setX((screenBounds.getWidth() - winW) / 2);
        stage.setY((screenBounds.getHeight() - winH) / 2);

        // ── Resize: 8 edge/corner panes laid over root ────────────────────────
        setupResize(root, scene, stage);

        // Load taskbar icon
        try {
            java.io.InputStream icon = getClass().getResourceAsStream("/resources/appicon.png");
            if (icon == null) icon = getClass().getResourceAsStream("/resources/9973171-Photoroom.png");
            if (icon != null) stage.getIcons().add(new javafx.scene.image.Image(icon));
        } catch (Exception ignored) {}

        stage.show();
    }

    // ── Resize logic ──────────────────────────────────────────────────────────

    private void setupResize(StackPane root, Scene scene, Stage stage) {
        final int M = RESIZE_MARGIN;

        // Create 8 edge/corner hit areas using AnchorPane
        javafx.scene.layout.AnchorPane resizeOverlay = new javafx.scene.layout.AnchorPane();
        resizeOverlay.setPickOnBounds(false);
        resizeOverlay.setMouseTransparent(false);

        // Each edge pane: transparent, sits on the border
        javafx.scene.layout.Pane n  = edgePane(Cursor.N_RESIZE,  stage, scene);
        javafx.scene.layout.Pane s  = edgePane(Cursor.S_RESIZE,  stage, scene);
        javafx.scene.layout.Pane e2 = edgePane(Cursor.E_RESIZE,  stage, scene);
        javafx.scene.layout.Pane w  = edgePane(Cursor.W_RESIZE,  stage, scene);
        javafx.scene.layout.Pane nw = edgePane(Cursor.NW_RESIZE, stage, scene);
        javafx.scene.layout.Pane ne = edgePane(Cursor.NE_RESIZE, stage, scene);
        javafx.scene.layout.Pane sw = edgePane(Cursor.SW_RESIZE, stage, scene);
        javafx.scene.layout.Pane se = edgePane(Cursor.SE_RESIZE, stage, scene);

        // Bind sizes to root so they always cover the edges
        // Top edge
        AnchorPane.setTopAnchor(n, 0.0); AnchorPane.setLeftAnchor(n, (double)M); AnchorPane.setRightAnchor(n, (double)M);
        n.setPrefHeight(M);
        // Bottom edge
        AnchorPane.setBottomAnchor(s, 0.0); AnchorPane.setLeftAnchor(s, (double)M); AnchorPane.setRightAnchor(s, (double)M);
        s.setPrefHeight(M);
        // Right edge
        AnchorPane.setRightAnchor(e2, 0.0); AnchorPane.setTopAnchor(e2, (double)M); AnchorPane.setBottomAnchor(e2, (double)M);
        e2.setPrefWidth(M);
        // Left edge
        AnchorPane.setLeftAnchor(w, 0.0); AnchorPane.setTopAnchor(w, (double)M); AnchorPane.setBottomAnchor(w, (double)M);
        w.setPrefWidth(M);
        // Corners
        AnchorPane.setTopAnchor(nw, 0.0);    AnchorPane.setLeftAnchor(nw, 0.0);
        nw.setPrefWidth(M*2); nw.setPrefHeight(M*2);
        AnchorPane.setTopAnchor(ne, 0.0);    AnchorPane.setRightAnchor(ne, 0.0);
        ne.setPrefWidth(M*2); ne.setPrefHeight(M*2);
        AnchorPane.setBottomAnchor(sw, 0.0); AnchorPane.setLeftAnchor(sw, 0.0);
        sw.setPrefWidth(M*2); sw.setPrefHeight(M*2);
        AnchorPane.setBottomAnchor(se, 0.0); AnchorPane.setRightAnchor(se, 0.0);
        se.setPrefWidth(M*2); se.setPrefHeight(M*2);

        resizeOverlay.getChildren().addAll(n, s, e2, w, nw, ne, sw, se);

        // Bind overlay to root size
        resizeOverlay.prefWidthProperty().bind(root.widthProperty());
        resizeOverlay.prefHeightProperty().bind(root.heightProperty());

        root.getChildren().add(resizeOverlay);
    }

    private javafx.scene.layout.Pane edgePane(Cursor cursor, Stage stage, Scene scene) {
        javafx.scene.layout.Pane pane = new javafx.scene.layout.Pane();
        pane.setStyle("-fx-background-color: transparent;");
        pane.setCursor(cursor);

        pane.setOnMousePressed(e -> {
            if (stage.isMaximized()) return;
            resizeStartX  = e.getSceneX();  resizeStartY  = e.getSceneY();
            resizeStartW  = stage.getWidth(); resizeStartH  = stage.getHeight();
            resizeStartSX = stage.getX();    resizeStartSY = stage.getY();
            e.consume();
        });

        pane.setOnMouseDragged(e -> {
            if (stage.isMaximized()) return;
            double dx = e.getScreenX() - (resizeStartSX + resizeStartX);
            double dy = e.getScreenY() - (resizeStartSY + resizeStartY);
            double newW = resizeStartW, newH = resizeStartH;
            double newX = resizeStartSX, newY = resizeStartSY;

            if (cursor == Cursor.E_RESIZE  || cursor == Cursor.SE_RESIZE || cursor == Cursor.NE_RESIZE)
                newW = Math.max(stage.getMinWidth(),  resizeStartW + dx);
            if (cursor == Cursor.S_RESIZE  || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE)
                newH = Math.max(stage.getMinHeight(), resizeStartH + dy);
            if (cursor == Cursor.W_RESIZE  || cursor == Cursor.SW_RESIZE || cursor == Cursor.NW_RESIZE) {
                newW = Math.max(stage.getMinWidth(),  resizeStartW - dx);
                newX = resizeStartSX + (resizeStartW - newW);
            }
            if (cursor == Cursor.N_RESIZE  || cursor == Cursor.NW_RESIZE || cursor == Cursor.NE_RESIZE) {
                newH = Math.max(stage.getMinHeight(), resizeStartH - dy);
                newY = resizeStartSY + (resizeStartH - newH);
            }
            stage.setX(newX); stage.setY(newY);
            stage.setWidth(newW); stage.setHeight(newH);
            e.consume();
        });

        return pane;
    }

    // ── Custom Title Bar ──────────────────────────────────────────────────────

    private BorderPane createTitleBar(Stage stage) {
        javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView();
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/resources/9973171-Photoroom.png");
            if (is != null) {
                icon.setImage(new javafx.scene.image.Image(is));
                icon.setFitWidth(16); icon.setFitHeight(16);
                icon.setPreserveRatio(true);
            }
        } catch (Exception ignored) {}

        javafx.scene.control.Label title = new javafx.scene.control.Label("Auralin");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        javafx.scene.control.Button btnMin   = makeWinBtn("─",  false, stage);
        javafx.scene.control.Button btnMax   = makeWinBtn("⬜", true,  stage);
        javafx.scene.control.Button btnClose = makeWinBtn("✕",  false, stage);

        btnMin.setOnAction(e -> {
            animateBtn(btnMin);
            stage.setIconified(true);
        });
        btnMax.setOnAction(e -> {
            animateBtn(btnMax);
            stage.setMaximized(!stage.isMaximized());
        });
        btnClose.setOnAction(e -> {
            animateBtn(btnClose);
            playerController.dispose();
            stage.close();
        });

        HBox left = new HBox(8, icon, title);
        left.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setMargin(icon, new Insets(0, 0, 0, 0));

        HBox right = new HBox(0, btnMin, btnMax, btnClose);
        right.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        BorderPane titleBar = new BorderPane();
        titleBar.setLeft(left);
        titleBar.setRight(right);
        titleBar.setStyle("-fx-background-color: #000000; -fx-padding: 0 0 0 10; -fx-background-radius: 10 10 0 0;");
        titleBar.setPrefHeight(32);
        titleBar.setMinHeight(32);
        titleBar.setMaxHeight(32);
        titleBar.setVisible(true);
        titleBar.setManaged(true);
        BorderPane.setAlignment(left, javafx.geometry.Pos.CENTER_LEFT);
        BorderPane.setAlignment(right, javafx.geometry.Pos.CENTER_RIGHT);

        // Drag to move
        final double[] drag = {0, 0};
        titleBar.setOnMousePressed(e -> { drag[0] = e.getSceneX(); drag[1] = e.getSceneY(); });
        titleBar.setOnMouseDragged(e -> {
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - drag[0]);
                stage.setY(e.getScreenY() - drag[1]);
            }
        });
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) stage.setMaximized(!stage.isMaximized());
        });

        return titleBar;
    }

    private javafx.scene.control.Button makeWinBtn(String symbol, boolean isMax, Stage stage) {
        javafx.scene.control.Button btn = new javafx.scene.control.Button(symbol);
        // Max button slightly larger font for Windows 11 standard feel
        String fontSize = isMax ? "15px" : "13px";
        String base = "-fx-background-color: transparent; -fx-text-fill: white; " +
                      "-fx-font-size: " + fontSize + "; -fx-padding: 0; -fx-cursor: hand; " +
                      "-fx-border-width: 0; -fx-min-width: 46; -fx-min-height: 32;";
        String hoverBg = (symbol.equals("✕")) ? "#C42B1C" : "#2A2A2A";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> {
            btn.setStyle(base + "-fx-background-color: " + hoverBg + ";");
            // Win11 fade-in hover
            FadeTransition ft = new FadeTransition(Duration.millis(100), btn);
            ft.setFromValue(0.7); ft.setToValue(1.0); ft.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(base);
            FadeTransition ft = new FadeTransition(Duration.millis(100), btn);
            ft.setFromValue(1.0); ft.setToValue(0.85); ft.play();
        });
        return btn;
    }

    /** Small press scale animation — Win11 style */
    private void animateBtn(javafx.scene.control.Button btn) {
        ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(0.88);  st.setToY(0.88);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    // ── PlayerBar event listener ──────────────────────────────────────────────

    private PlayerBar.PlayerBarListener createPlayerBarListener() {
        return new PlayerBar.PlayerBarListener() {
            @Override public void onPlayPause()           { playerController.togglePlay(); }
            @Override public void onNext()                { playerController.playNext(); }
            @Override public void onPrevious()            { playerController.playPrevious(); }
            @Override public void onShuffle(boolean on)   { playerController.setShuffle(on); }
            @Override public void onRepeat(int mode)      { playerController.setRepeatMode(mode); }
            @Override public void onLove()                { playerController.toggleLike(); }
            @Override public void onSeek(double pos)      { playerController.seek(pos); }
            @Override public void onVolumeChange(double v){ playerController.setVolume(v); }

            @Override
            public void onArtworkClick() {
                if (isNowPlayingOpen()) {
                    upperStack.getChildren().remove(nowPlayingView);
                } else {
                    openNowPlayingView();
                }
            }
        };
    }

    // ── NowPlayingView ────────────────────────────────────────────────────────

    private void openNowPlayingView() {
        if (db.getCurrent() == null) return;
        if (nowPlayingView == null) {
            nowPlayingView = new NowPlayingView(new NowPlayingView.NowPlayingListener() {
                @Override public void onClose() { upperStack.getChildren().remove(nowPlayingView); }
                @Override public void onLove()  { playerController.toggleLike(); }
            });
        }
        nowPlayingView.updateSong(db.getCurrent());
        if (!upperStack.getChildren().contains(nowPlayingView)) {
            nowPlayingView.prefWidthProperty().bind(upperStack.widthProperty());
            nowPlayingView.prefHeightProperty().bind(upperStack.heightProperty());
            upperStack.getChildren().add(nowPlayingView);
            nowPlayingView.requestFocus();
        }
    }

    private boolean isNowPlayingOpen() {
        return nowPlayingView != null && upperStack.getChildren().contains(nowPlayingView);
    }

    // ── Global CSS ────────────────────────────────────────────────────────────

    private void applyGlobalStyles(Scene scene) {
        // Write CSS to a temp file and load it — avoids data URI encoding issues
        try {
            java.io.File tmp = java.io.File.createTempFile("auralin", ".css");
            tmp.deleteOnExit();
            try (java.io.FileWriter fw = new java.io.FileWriter(tmp)) {
                fw.write(Constants.GLOBAL_CSS);
            }
            scene.getStylesheets().add(tmp.toURI().toURL().toExternalForm());
        } catch (Exception e) {
            // fallback: data URI with encoding
            String css = Constants.GLOBAL_CSS
                .replace("\\", "%5C").replace(" ", "%20")
                .replace("#", "%23").replace("(", "%28")
                .replace(")", "%29").replace("'", "%27");
            scene.getStylesheets().add("data:text/css," + css);
        }
    }
    @Override
    public void stop() { playerController.dispose(); db.saveToDisk(); db.shutdown(); }
}
