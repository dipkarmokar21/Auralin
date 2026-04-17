import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class Main extends Application {
    private String userName = "User";
    private DatabaseService db = new DatabaseService();
    private MediaPlayer player;
    private StackPane contentArea = new StackPane();

    // Player UI Components
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
    private VBox sidebarNav;
    private ProgressBar loadingBar = new ProgressBar(0);
    private Label lblLoadingStatus = new Label("");

    // State variables
    private String activeView = "Home";
    private boolean isShuffle = false;
    private int repeatMode = 0;
    private boolean isUserSeeking = false;
    private boolean isQueueVisible = false;
    private String lastActiveView = "Home";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        promptForUserName();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + Constants.BG_BLACK + ";");

        root.setLeft(createSidebar(stage));
        showHomeView();
        root.setCenter(contentArea);
        root.setBottom(createPlayerBar());

        Scene scene = new Scene(root, 1250, 850);
        applyGlobalStyles(scene);
        stage.setTitle("Melodix - Premium Offline Music Player");
        stage.setScene(scene);
        stage.show();
    }

    private void promptForUserName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Welcome");
        dialog.setHeaderText("Welcome to Auralin Music Player!");
        dialog.setContentText("Please enter your name:");
        dialog.setGraphic(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add("data:text/css," + Constants.DIALOG_CSS.replace(" ", "%20").replace("#", "%23"));

        TextField inputField = dialog.getEditor();
        javafx.scene.Node okButton = dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty());
        });

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            userName = result.get().trim();
        }
    }

    private VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(25);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(30, 20, 20, 20));
        sidebar.setStyle("-fx-background-color: " + Constants.SIDEBAR_BLACK + ";");

        Label logo = new Label("Auralin");
        logo.setFont(Font.font("System", FontWeight.BOLD, 28));
        logo.setTextFill(Color.web("#FA2D48"));

        sidebarNav = new VBox(15);
        updateNavHighlight();

        VBox loadingBox = new VBox(5, lblLoadingStatus, loadingBar);
        loadingBar.setMaxWidth(Double.MAX_VALUE);
        loadingBar.setVisible(false);
        lblLoadingStatus.setTextFill(Color.web(Constants.SPOTIFY_GREEN));
        lblLoadingStatus.setFont(Font.font(11));

        sidebar.getChildren().addAll(logo, sidebarNav, new Separator(), loadingBox);
        return sidebar;
    }

    private void updateNavHighlight() {
        sidebarNav.getChildren().clear();
        sidebarNav.getChildren().addAll(
                createNavBtn("Home", "🏠", activeView.equals("Home"), e -> { activeView = "Home"; updateNavHighlight(); showHomeView(); }),
                createNavBtn("Search", "🔍", activeView.equals("Search"), e -> { activeView = "Search"; updateNavHighlight(); showSearchView(); }),
                createNavBtn("Your Library", "📚", activeView.equals("Library"), e -> { activeView = "Library"; updateNavHighlight(); showLibraryView(); }),
                createNavBtn("Liked Songs", "❤", activeView.equals("Liked"), e -> { activeView = "Liked"; updateNavHighlight(); showLikedView(); })
        );
    }

    private Button createNavBtn(String text, String icon, boolean isActive, javafx.event.EventHandler<javafx.event.ActionEvent> event) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-btn");
        if (isActive) btn.setStyle("-fx-text-fill: " + Constants.SPOTIFY_GREEN + ";");
        btn.setOnAction(event);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void showHomeView() {
        VBox v = createViewContainer("Hello " + userName);
        VBox recent = new VBox(15, createSectionLabel("Recently Played"), createGrid(db.getRecentlyPlayed()));
        VBox recs = new VBox(15, createSectionLabel("Made For You"), createGrid(db.getRecommendations()));

        VBox.setVgrow(recent, Priority.ALWAYS);
        VBox.setVgrow(recs, Priority.ALWAYS);

        v.getChildren().addAll(recent, recs);
        setMainView(v);
    }

    private void showSearchView() {
        VBox v = createViewContainer("Search");
        TextField search = new TextField();
        search.setPromptText("What do you want to listen to?");
        search.getStyleClass().add("search-field");

        TableView<Song> results = createPremiumTable(FXCollections.observableArrayList());
        results.setVisible(false);
        results.managedProperty().bind(results.visibleProperty());

        search.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                results.setVisible(false);
            } else {
                results.setVisible(true);
                results.setItems(db.getAllSongs().filtered(s ->
                        s.getTitle().toLowerCase().contains(newVal.toLowerCase()) ||
                                s.getArtist().toLowerCase().contains(newVal.toLowerCase())));
            }
        });

        v.getChildren().addAll(search, results);
        setMainView(v);
    }

    private void showLibraryView() {
        VBox v = createViewContainer("Your Library");
        HBox actions = new HBox(15);
        Button addFiles = new Button("➕ Add Files");
        addFiles.getStyleClass().add("action-btn");
        addFiles.setOnAction(e -> addFiles());

        Button addFolder = new Button("📁 Add Folder");
        addFolder.getStyleClass().add("action-btn");
        addFolder.setOnAction(e -> addFolder());

        actions.getChildren().addAll(addFiles, addFolder);
        TableView<Song> table = createPremiumTable(db.getAllSongs());
        VBox.setVgrow(table, Priority.ALWAYS);
        v.getChildren().addAll(actions, table);
        setMainView(v);
    }

    private void showLikedView() {
        VBox v = createViewContainer("Liked Songs");
        TableView<Song> table = createPremiumTable(db.getLikedSongs());
        VBox.setVgrow(table, Priority.ALWAYS);
        v.getChildren().add(table);
        setMainView(v);
    }

    private void showQueueView() {
        VBox v = createViewContainer("Play Queue");
        ObservableList<Song> queueList = FXCollections.observableArrayList(db.getAllSongs());
        Song current = db.getCurrent();
        if(current != null) {
            queueList.remove(current);
            queueList.add(0, current);
        }
        TableView<Song> table = createPremiumTable(queueList);
        VBox.setVgrow(table, Priority.ALWAYS);
        v.getChildren().add(table);
        setMainView(v);
    }

    private void setMainView(VBox v) {
        ScrollPane scroll = new ScrollPane(v);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        contentArea.getChildren().setAll(scroll);
    }

    private VBox createViewContainer(String title) {
        VBox v = new VBox(30);
        v.setPadding(new Insets(40));
        v.setMinWidth(800);
        Stop[] stops = {new Stop(0, Color.web("#222222")), new Stop(1, Color.web(Constants.BG_BLACK))};
        v.setBackground(new Background(new BackgroundFill(new LinearGradient(0,0,0,1, true, CycleMethod.NO_CYCLE, stops), null, null)));
        Label lbl = new Label(title);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 32));
        lbl.setTextFill(Color.WHITE);
        v.getChildren().add(lbl);
        return v;
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
        for(Song s : songs) {
            VBox card = new VBox(12);
            card.getStyleClass().add("music-card");
            card.setPrefWidth(180);
            StackPane imgBox = new StackPane();
            imgBox.setPrefSize(150, 150);
            imgBox.setStyle("-fx-background-color: #333; -fx-background-radius: 12;");
            ImageView iv = new ImageView(s.getArtwork());
            iv.setFitWidth(150); iv.setFitHeight(150);
            imgBox.getChildren().add(iv);

            Label t = new Label(s.getTitle());
            t.setTextFill(s == db.getCurrent() ? Color.web(Constants.SPOTIFY_GREEN) : Color.WHITE);
            t.setFont(Font.font(null, FontWeight.BOLD, 15));
            t.setTextOverrun(OverrunStyle.ELLIPSIS);

            Label a = new Label(s.getArtist());
            a.setTextFill(Color.web(Constants.TEXT_GRAY));
            a.setTextOverrun(OverrunStyle.ELLIPSIS);

            card.getChildren().addAll(imgBox, t, a);
            card.setOnMouseClicked(e -> play(s));
            p.getChildren().add(card);
        }
        return p;
    }

    private TableView<Song> createPremiumTable(ObservableList<Song> items) {
        TableView<Song> table = new TableView<>(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("spotify-table");

        TableColumn<Song, Song> titleCol = new TableColumn<>("TITLE");
        titleCol.setCellValueFactory(f -> new SimpleObjectProperty<>(f.getValue()));
        titleCol.setCellFactory(c -> new TableCell<>() {
            private HBox hBox = new HBox(12);
            private ImageView imageView = new ImageView();
            private Label titleLbl = new Label();
            private Label artistLbl = new Label();
            private VBox vBox = new VBox(2, titleLbl, artistLbl);
            {
                hBox.setAlignment(Pos.CENTER_LEFT);
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                hBox.getChildren().addAll(imageView, vBox);
            }
            @Override protected void updateItem(Song s, boolean empty) {
                super.updateItem(s, empty);
                if(empty || s == null) { setGraphic(null); }
                else {
                    imageView.setImage(s.getArtwork());
                    titleLbl.setText(s.getTitle());
                    artistLbl.setText(s.getArtist());
                    if(s == db.getCurrent()) {
                        titleLbl.setTextFill(Color.web(Constants.SPOTIFY_GREEN));
                        animatePulse(titleLbl);
                    } else {
                        titleLbl.setTextFill(Color.WHITE);
                        stopPulse(titleLbl);
                    }
                    artistLbl.setTextFill(Color.web(Constants.TEXT_GRAY));
                    setGraphic(hBox);
                }
            }
        });

        TableColumn<Song, String> artCol = new TableColumn<>("ARTIST");
        artCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getArtist()));
        artCol.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else { setText(item); setTextFill(Color.WHITE); }
            }
        });

        TableColumn<Song, String> durCol = new TableColumn<>("DURATION");
        durCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getDurationStr()));
        durCol.setMaxWidth(100);
        durCol.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else { setText(item); setTextFill(Color.WHITE); setAlignment(Pos.CENTER_RIGHT); }
            }
        });

        table.getColumns().addAll(titleCol, artCol, durCol);
        table.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null)
                play(table.getSelectionModel().getSelectedItem());
        });
        return table;
    }

    private void animatePulse(Label l) {
        Timeline pulse = (Timeline) l.getProperties().get("pulse");
        if (pulse == null) {
            pulse = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(l.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(l.opacityProperty(), 0.6)),
                    new KeyFrame(Duration.seconds(2), new KeyValue(l.opacityProperty(), 1.0))
            );
            pulse.setCycleCount(Timeline.INDEFINITE);
            l.getProperties().put("pulse", pulse);
        }
        pulse.play();
    }

    private void stopPulse(Label l) {
        Timeline pulse = (Timeline) l.getProperties().get("pulse");
        if (pulse != null) { pulse.stop(); }
        l.setOpacity(1.0);
    }

    private HBox createPlayerBar() {
        HBox bar = new HBox(40);
        bar.setPadding(new Insets(15, 30, 15, 30));
        bar.setAlignment(Pos.CENTER);
        bar.setStyle("-fx-background-color: #000000; -fx-border-color: #282828; -fx-border-width: 1 0 0 0;");
        bar.setPrefHeight(120);

        HBox info = new HBox(15);
        info.setAlignment(Pos.CENTER_LEFT);
        info.setMinWidth(320);
        info.setMaxWidth(320);
        imgNowPlaying.setFitHeight(60); imgNowPlaying.setFitWidth(60);

        VBox text = new VBox(2, lblTitle, lblArtist);
        text.setAlignment(Pos.CENTER_LEFT);
        lblTitle.setTextFill(Color.WHITE); lblTitle.setFont(Font.font(null, FontWeight.BOLD, 14));
        lblTitle.setMaxWidth(180); lblTitle.setTextOverrun(OverrunStyle.ELLIPSIS);
        lblArtist.setTextFill(Color.web(Constants.TEXT_GRAY));
        lblArtist.setMaxWidth(180); lblArtist.setTextOverrun(OverrunStyle.ELLIPSIS);

        btnLove.getStyleClass().add("love-btn");
        btnLove.setOnAction(e -> { Song s = db.getCurrent(); if(s != null) { s.setLiked(!s.isLiked()); updateLoveUI(); } });
        info.getChildren().addAll(imgNowPlaying, text, btnLove);

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

        btnPlay.setOnAction(e -> togglePlay());
        bP.setOnAction(e -> playPrev());
        bN.setOnAction(e -> playNext());
        bS.setOnAction(e -> { isShuffle = !isShuffle; bS.setStyle("-fx-text-fill: " + (isShuffle ? Constants.SPOTIFY_GREEN : Constants.TEXT_GRAY) + ";"); });
        bR.setOnAction(e -> { repeatMode = (repeatMode + 1) % 3; bR.setText(repeatMode == 1 ? "🔂" : "🔁"); bR.setStyle("-fx-text-fill: " + (repeatMode > 0 ? Constants.SPOTIFY_GREEN : Constants.TEXT_GRAY) + ";"); });

        btns.getChildren().addAll(bS, bP, btnPlay, bN, bR);

        StackPane sliderStack = new StackPane();
        HBox.setHgrow(sliderStack, Priority.ALWAYS);
        sliderStack.setMaxWidth(500);
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        progressSlider.setPrefHeight(4);
        progressSlider.setPrefWidth(1);
        progressSlider.getStyleClass().add("spotify-slider");
        progressSlider.setOnMousePressed(e -> isUserSeeking = true);
        progressSlider.setOnMouseReleased(e -> {
            if(player != null && player.getTotalDuration() != null) {
                double pct = progressSlider.getValue() / 100.0;
                player.seek(player.getTotalDuration().multiply(pct));
            }
            isUserSeeking = false;
        });
        progressSlider.setOnMouseClicked(e -> {
            if(player != null && player.getTotalDuration() != null) {
                double pct = e.getX() / progressSlider.getWidth();
                progressSlider.setValue(pct * 100);
                player.seek(player.getTotalDuration().multiply(pct));
            }
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
        lblTimeCurrent.setTextFill(Color.web(Constants.TEXT_GRAY)); lblTimeTotal.setTextFill(Color.web(Constants.TEXT_GRAY));
        HBox.setHgrow(timeBar, Priority.ALWAYS);

        center.getChildren().addAll(btns, timeBar);

        HBox extra = new HBox(15);
        extra.setAlignment(Pos.CENTER_RIGHT);
        extra.setMinWidth(250);

        btnQueue.getStyleClass().add("control-icon");
        btnQueue.setStyle("-fx-font-size: 22px;");
        btnQueue.setOnAction(e -> {
            if (!isQueueVisible) {
                lastActiveView = activeView;
                activeView = "Queue";
                showQueueView();
                updateNavHighlight();
                isQueueVisible = true;
            } else {
                isQueueVisible = false;
                activeView = lastActiveView;
                refreshCurrentView();
                updateNavHighlight();
            }
        });

        volSlider.setPrefWidth(100);
        volSlider.valueProperty().addListener((o, old, nv) -> { if(player != null) player.setVolume(nv.doubleValue()); });

        Label lblVol = new Label("🔊");
        lblVol.setTextFill(Color.WHITE);
        lblVol.setStyle("-fx-font-size: 22px;");
        extra.getChildren().addAll(btnQueue, lblVol, volSlider);

        bar.getChildren().addAll(info, center, extra);
        return bar;
    }

    private Button createControlBtn(String icon, int size) {
        Button b = new Button(icon);
        b.getStyleClass().add("control-icon");
        b.setStyle("-fx-font-size: " + size + "px;");
        return b;
    }

    private void play(Song s) {
        if(player != null) {
            player.stop();
            player.dispose();
        }
        db.setCurrent(s);
        try {
            Media m = new Media(new File(s.getFilePath()).toURI().toString());
            player = new MediaPlayer(m);
            player.setVolume(volSlider.getValue());

            player.setOnReady(() -> {
                lblTimeTotal.setText(formatTime(m.getDuration()));
                lblTimeCurrent.setText("0:00");
                updatePlayingUI(s);
            });

            player.currentTimeProperty().addListener((obs, ot, nt) -> {
                if(!isUserSeeking) {
                    double totalMillis = m.getDuration().toMillis();
                    if (totalMillis > 0 && !Double.isNaN(totalMillis)) {
                        double pct = nt.toMillis() / totalMillis;
                        progressSlider.setValue(pct * 100);
                    }
                    lblTimeCurrent.setText(formatTime(nt));
                }
            });

            player.setOnEndOfMedia(() -> { if (repeatMode == 1) { player.seek(Duration.ZERO); player.play(); } else playNext(); });
            player.play();
            btnPlay.setText("⏸");
            db.recordPlay(s);
            Platform.runLater(this::refreshCurrentView);
        } catch(Exception e) {
            System.err.println("Error playing media: " + e.getMessage());
            playNext();
        }
    }

    private void refreshCurrentView() {
        if (activeView.equals("Home")) showHomeView();
        else if (activeView.equals("Library")) showLibraryView();
        else if (activeView.equals("Search")) showSearchView();
        else if (activeView.equals("Liked")) showLikedView();
        else if (activeView.equals("Queue")) showQueueView();
    }

    private void updatePlayingUI(@org.jetbrains.annotations.NotNull Song s) {
        lblTitle.setText(s.getTitle());
        lblArtist.setText(s.getArtist());
        imgNowPlaying.setImage(s.getArtwork());
        updateLoveUI();
    }

    private void updateLoveUI() {
        Song s = db.getCurrent();
        if(s != null && s.isLiked()) {
            btnLove.setText("❤");
            btnLove.setStyle("-fx-text-fill: " + Constants.SPOTIFY_GREEN + ";");
        } else {
            btnLove.setText("\u2661");
            btnLove.setStyle("-fx-text-fill: white;");
        }
    }

    private void togglePlay() {
        if(player == null) return;
        if(player.getStatus() == MediaPlayer.Status.PLAYING) { player.pause(); btnPlay.setText("▶"); }
        else { player.play(); btnPlay.setText("⏸"); }
    }

    private void playNext() {
        ObservableList<Song> list = db.getAllSongs();
        if (list.isEmpty()) return;
        int idx = isShuffle ? new Random().nextInt(list.size()) : (list.indexOf(db.getCurrent()) + 1);
        if (idx >= list.size()) idx = (repeatMode == 2) ? 0 : list.size() - 1;
        play(list.get(idx));
    }

    private void playPrev() {
        ObservableList<Song> list = db.getAllSongs();
        if (list.isEmpty()) return;
        int idx = list.indexOf(db.getCurrent());
        if (idx == -1) idx = 1;
        idx -= 1;
        if (idx < 0) idx = (repeatMode == 2) ? list.size() - 1 : 0;
        play(list.get(idx));
    }

    private void addFiles() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav", "*.m4a", "*.flac"));
        List<File> files = fc.showOpenMultipleDialog(null);
        if(files != null) files.forEach(db::addFile);
    }

    private void addFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        File folder = dc.showDialog(null);
        if(folder == null) return;

        loadingBar.setVisible(true);
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                List<File> allFiles = new ArrayList<>();
                try (Stream<Path> paths = Files.walk(folder.toPath())) {
                    paths.filter(Files::isRegularFile)
                            .map(Path::toFile)
                            .filter(f -> {
                                String n = f.getName().toLowerCase();
                                return n.endsWith(".mp3") || n.endsWith(".wav") || n.endsWith(".flac") || n.endsWith(".m4a");
                            })
                            .forEach(allFiles::add);
                } catch (Exception e) { e.printStackTrace(); }

                for(int i=0; i<allFiles.size(); i++) {
                    final int idx = i;
                    File currentFile = allFiles.get(idx);
                    Platform.runLater(() -> {
                        db.addFile(currentFile);
                        lblLoadingStatus.setText("Loading: " + (idx+1) + "/" + allFiles.size());
                    });
                    updateProgress(idx + 1, allFiles.size());
                    Thread.sleep(50);
                }
                return null;
            }
        };
        loadingBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> { loadingBar.setVisible(false); lblLoadingStatus.setText(" "); });
        new Thread(task).start();
    }

    private String formatTime(Duration d) {
        if (d == null || d.isUnknown()) return "0:00";
        int m = (int)d.toMinutes();
        int s = (int)d.toSeconds() % 60;
        return String.format("%d:%02d", m, s);
    }

    private void applyGlobalStyles(Scene scene) {
        String dataUri = "data:text/css," + Constants.GLOBAL_CSS.replace(" ", "%20").replace("#", "%23").replace("(", "%28").replace(")", "%29");
        scene.getStylesheets().add(dataUri);
    }
}