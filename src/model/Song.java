package model;

import javafx.scene.image.Image;
import javafx.util.Duration;
import config.Constants;

public class Song {
    private String title;
    private String artist;
    private String filePath;
    private Image artwork;
    private Duration duration = Duration.ZERO;
    public int plays = 0;
    public long lastPlayed = 0;
    private boolean liked = false;

    public Song(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;
        this.artist = "Unknown Artist";
        this.artwork = new Image(Constants.DEFAULT_ART);
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getFilePath() { return filePath; }
    public Image getArtwork() { return artwork; }
    public void setArtwork(Image artwork) { this.artwork = artwork; }

    public String getDurationStr() {
        if (duration == null || duration.isUnknown()) return "0:00";
        return (int)duration.toMinutes() + ":" + String.format("%02d", (int)duration.toSeconds() % 60);
    }

    public void setDuration(Duration duration) { this.duration = duration; }
    public double getDurationMs() { return duration == null ? 0 : duration.toMillis(); }
    public void setDurationMs(long ms) { this.duration = Duration.millis(ms); }
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public void record() {
        plays++;
        lastPlayed = System.currentTimeMillis();
    }
}