import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseService {
    private ObservableList<Song> all = FXCollections.observableArrayList();
    private Song current;

    public void addFile(File f) {
        if(all.stream().anyMatch(s -> s.getFilePath().equals(f.getAbsolutePath()))) return;
        Song s = new Song(f.getName().replaceAll("\\.[a-zA-Z0-9]+$", ""), f.getAbsolutePath());
        all.add(s);

        try {
            // mp3agic দিয়ে মেটাডেটা এবং ছবি বের করার চেষ্টা
            try {
                Mp3File mp3file = new Mp3File(f.getAbsolutePath());
                if (mp3file.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                    if (id3v2Tag.getArtist() != null) {
                        s.setArtist(id3v2Tag.getArtist());
                    }
                    byte[] imageData = id3v2Tag.getAlbumImage();
                    if (imageData != null) {
                        Image img = new Image(new ByteArrayInputStream(imageData));
                        s.setArtwork(img);
                    }
                }
            } catch (Exception ex) {
                System.err.println("mp3agic failed for " + f.getName() + " - " + ex.getMessage());
            }

            // JavaFX Media (Duration এবং অন্যান্য তথ্যের জন্য)
            Media m = new Media(f.toURI().toString());
            MediaPlayer mp = new MediaPlayer(m);

            mp.setOnReady(() -> {
                s.setDuration(m.getDuration());
                if (s.getArtist().equals("Unknown Artist")) {
                    Object art = m.getMetadata().get("artist");
                    if(art != null) s.setArtist(art.toString());
                }
                if (s.getArtwork().getUrl() != null && s.getArtwork().getUrl().equals(Constants.DEFAULT_ART)) {
                    Object img = m.getMetadata().get("image");
                    if(img instanceof Image) s.setArtwork((Image)img);
                }

                Platform.runLater(() -> {
                    int index = all.indexOf(s);
                    if(index >= 0) all.set(index, s);
                });
                mp.dispose();
            });
        } catch (Exception e) {
            System.err.println("General error loading: " + f.getName());
        }
    }

    public ObservableList<Song> getAllSongs() { return all; }
    public ObservableList<Song> getLikedSongs() { return all.filtered(Song::isLiked); }

    public List<Song> getRecentlyPlayed() {
        return all.stream()
                .filter(s -> s.lastPlayed > 0)
                .sorted(Comparator.comparingLong((Song s) -> s.lastPlayed).reversed())
                .limit(6)
                .collect(Collectors.toList());
    }

    public List<Song> getRecommendations() {
        return all.stream()
                .sorted(Comparator.comparingInt((Song s) -> s.plays).reversed())
                .limit(12)
                .collect(Collectors.toList());
    }

    public Song getCurrent() { return current; }
    public void setCurrent(Song s) { this.current = s; }
    public void recordPlay(Song s) { s.record(); }
}