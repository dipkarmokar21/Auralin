package ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import model.Song;
import config.Constants;

public class SongTable extends TableView<Song> {
    private SongTableListener listener;
    
    public interface SongTableListener {
        void onSongDoubleClick(Song song);
        boolean isCurrentSong(Song song);
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
        
        getColumns().addAll(titleCol, artCol, durCol);
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
