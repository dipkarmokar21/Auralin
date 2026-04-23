package ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import model.Song;
import config.Constants;

public class SongTable extends TableView<Song> {
    private SongTableListener listener;

    // heart.svg path loader
    private static final String HEART_PATH = loadHeartPath();

    private static String loadHeartPath() {
        try {
            java.io.InputStream is = SongTable.class.getResourceAsStream("/resources/heart.svg");
            if (is == null) return "";
            String xml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("d=\"([^\"]+)\"").matcher(xml);
            return m.find() ? m.group(1) : "";
        } catch (Exception e) { return ""; }
    }
    
    public interface SongTableListener {
        void onSongDoubleClick(Song song);
        boolean isCurrentSong(Song song);
        void onLikeToggle(Song song);
    }
    
    public SongTable(ObservableList<Song> items, SongTableListener listener) {
        super(items);
        this.listener = listener;
        setupTable();
    }
    
    private void setupTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        getStyleClass().add("spotify-table");
        
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
            @Override
            protected void updateItem(Song s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(s.getArtwork());
                    titleLbl.setText(s.getTitle());
                    artistLbl.setText(s.getArtist());
                    if (listener.isCurrentSong(s)) {
                        titleLbl.setTextFill(Color.web("#4a64e9ff"));
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
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else {
                    setText(item);
                    setTextFill(Color.WHITE);
                }
            }
        });
        
        TableColumn<Song, String> durCol = new TableColumn<>("DURATION");
        durCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getDurationStr()));
        durCol.setMaxWidth(100);
        durCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });

        TableColumn<Song, Song> likeCol = new TableColumn<>("LIKED");
        likeCol.setCellValueFactory(f -> new SimpleObjectProperty<>(f.getValue()));
        likeCol.setMaxWidth(80);
        likeCol.setCellFactory(c -> new TableCell<>() {
            private SVGPath heartOutline = new SVGPath();
            private SVGPath heartFilled = new SVGPath();
            private StackPane heartBtn = new StackPane(heartOutline, heartFilled);
            {
                double scale = 0.75; // 24 viewBox → 18px
                heartOutline.setContent(HEART_PATH);
                heartOutline.setFill(Color.TRANSPARENT);
                heartOutline.setStroke(Color.web("#B3B3B3"));
                heartOutline.setStrokeWidth(1.5);
                heartOutline.getTransforms().add(new Scale(scale, scale));
                heartOutline.setMouseTransparent(true);

                heartFilled.setContent(HEART_PATH);
                heartFilled.setFill(Color.web("#FA2D48"));
                heartFilled.setStroke(Color.TRANSPARENT);
                heartFilled.getTransforms().add(new Scale(scale, scale));
                heartFilled.setOpacity(0);
                heartFilled.setMouseTransparent(true);

                heartBtn.setAlignment(Pos.CENTER);
                heartBtn.setPrefSize(20, 20);
                heartBtn.setStyle("-fx-cursor: hand;");
                heartBtn.setOnMouseClicked(e -> {
                    Song song = getTableRow().getItem();
                    if (song != null) {
                        song.setLiked(!song.isLiked());
                        updateHeartState(song.isLiked());
                        listener.onLikeToggle(song);
                        e.consume();
                    }
                });
            }

            private void updateHeartState(boolean isLiked) {
                ScaleTransition pop = new ScaleTransition(Duration.millis(120), heartBtn);
                pop.setFromX(1.0); pop.setFromY(1.0);
                pop.setToX(1.35);  pop.setToY(1.35);
                pop.setAutoReverse(true);
                pop.setCycleCount(2);
                pop.play();

                if (isLiked) {
                    heartFilled.setOpacity(1);
                    heartOutline.setStroke(Color.TRANSPARENT);
                    DropShadow glow = new DropShadow(6, Color.web("#FA2D48"));
                    heartFilled.setEffect(glow);
                } else {
                    heartFilled.setOpacity(0);
                    heartOutline.setStroke(Color.web("#B3B3B3"));
                    heartFilled.setEffect(null);
                }
            }

            @Override
            protected void updateItem(Song s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setGraphic(null);
                } else {
                    updateHeartState(s.isLiked());
                    setGraphic(heartBtn);
                }
            }
        });
        
        getColumns().addAll(titleCol, artCol, durCol, likeCol);
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && getSelectionModel().getSelectedItem() != null)
                listener.onSongDoubleClick(getSelectionModel().getSelectedItem());
        });
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
        if (pulse != null) {
            pulse.stop();
        }
        l.setOpacity(1.0);
    }
}
