package services;

import config.Constants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import model.Song;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DatabaseService {
    private ObservableList<Song> all = FXCollections.observableArrayList();
    private Song current;

    // Thread pool for background metadata loading
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    private static final File SAVE_FILE = new File(
        System.getenv("APPDATA") != null
            ? System.getenv("APPDATA") + File.separator + "AuralinPlayer" + File.separator + "library.dat"
            : System.getProperty("user.home") + File.separator + ".auralin" + File.separator + "library.dat"
    );

    public DatabaseService() {
        loadFromDisk();
    }

    public void addFile(File f) {
        if (all.stream().anyMatch(s -> s.getFilePath().equals(f.getAbsolutePath()))) return;
        Song s = new Song(f.getName().replaceAll("\\.[a-zA-Z0-9]+$", ""), f.getAbsolutePath());
        all.add(s);

        executor.submit(() -> {
            // Load all metadata in background thread via mp3agic
            try {
                Mp3File mp3file = new Mp3File(f.getAbsolutePath());
                // Duration from mp3agic (no MediaPlayer needed)
                s.setDurationMs((long)(mp3file.getLengthInMilliseconds()));

                if (mp3file.hasId3v2Tag()) {
                    ID3v2 tag = mp3file.getId3v2Tag();
                    if (tag.getArtist() != null && !tag.getArtist().isEmpty())
                        s.setArtist(tag.getArtist());
                    byte[] img = tag.getAlbumImage();
                    if (img != null) s.setArtwork(new Image(new ByteArrayInputStream(img)));
                } else if (mp3file.hasId3v1Tag()) {
                    String artist = mp3file.getId3v1Tag().getArtist();
                    if (artist != null && !artist.isEmpty()) s.setArtist(artist);
                }
            } catch (Exception e) {
                System.err.println("mp3agic failed: " + e.getMessage());
            }

            Platform.runLater(() -> {
                int index = all.indexOf(s);
                if (index >= 0) all.set(index, s);
                saveToDisk();
            });
        });
    }

    // format: filePath|liked|plays|lastPlayed|artist|durationMs
    public void saveToDisk() {
        List<Song> snapshot = new ArrayList<>(all);
        executor.submit(() -> {
            try {
                SAVE_FILE.getParentFile().mkdirs();
                try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
                    for (Song s : snapshot) {
                        pw.println(s.getFilePath() + "|"
                            + s.isLiked() + "|"
                            + s.plays + "|"
                            + s.lastPlayed + "|"
                            + s.getArtist() + "|"
                            + (long) s.getDurationMs());
                    }
                }
            } catch (Exception e) {
                System.err.println("Save failed: " + e.getMessage());
            }
        });
    }

    private void loadFromDisk() {
        if (!SAVE_FILE.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", 6);
                if (p.length < 1) continue;
                File f = new File(p[0]);
                if (!f.exists()) continue;

                Song s = new Song(f.getName().replaceAll("\\.[a-zA-Z0-9]+$", ""), f.getAbsolutePath());
                if (p.length > 1) s.setLiked(Boolean.parseBoolean(p[1]));
                if (p.length > 2) s.plays = Integer.parseInt(p[2]);
                if (p.length > 3) s.lastPlayed = Long.parseLong(p[3]);
                if (p.length > 4 && !p[4].equals("Unknown Artist")) s.setArtist(p[4]);
                if (p.length > 5) s.setDurationMs(Long.parseLong(p[5]));
                all.add(s);

                // Load artwork only in background — no MediaPlayer needed
                executor.submit(() -> loadArtwork(s, f));
            }
        } catch (Exception e) {
            System.err.println("Load failed: " + e.getMessage());
        }
    }

    private void loadArtwork(Song s, File f) {
        try {
            Mp3File mp3file = new Mp3File(f.getAbsolutePath());
            if (mp3file.hasId3v2Tag()) {
                byte[] img = mp3file.getId3v2Tag().getAlbumImage();
                if (img != null) {
                    Image artwork = new Image(new ByteArrayInputStream(img));
                    Platform.runLater(() -> {
                        s.setArtwork(artwork);
                        int index = all.indexOf(s);
                        if (index >= 0) all.set(index, s);
                    });
                }
            }
        } catch (Exception ignored) {}
    }

    public ObservableList<Song> getAllSongs() { return all; }
    public ObservableList<Song> getLikedSongs() { return all.filtered(Song::isLiked); }

    public List<Song> getRecentlyPlayed() {
        return all.stream()
                .filter(s -> s.lastPlayed > 0)
                .sorted(Comparator.comparingLong((Song s) -> s.lastPlayed).reversed())
                .limit(9)
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
    public void recordPlay(Song s) { s.record(); saveToDisk(); }

    public void shutdown() { executor.shutdown(); }
}
