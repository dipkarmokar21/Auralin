package controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import model.Song;
import services.DatabaseService;
import components.PlayerBar;

import java.io.File;
import java.util.Random;

public class PlayerController {
    private final DatabaseService db;
    private MediaPlayer player;
    private final PlayerBar playerBar;
    private boolean isShuffle = false;
    private int repeatMode = 0;
    private Runnable onSongChange;
    
    public PlayerController(DatabaseService db, PlayerBar playerBar) {
        this.db = db;
        this.playerBar = playerBar;
    }
    
    public void setOnSongChange(Runnable callback) {
        this.onSongChange = callback;
    }
    
    public void play(Song s) {
        if (s == null) return;
        
        if (player != null) {
            player.stop();
            player.dispose();
        }
        
        db.setCurrent(s);
        
        try {
            Media m = new Media(new File(s.getFilePath()).toURI().toString());
            player = new MediaPlayer(m);
            player.setVolume(playerBar.getVolume());
            
            player.setOnReady(() -> {
                playerBar.updateTime("0:00", formatTime(m.getDuration()));
                playerBar.updateSongInfo(s);
                playerBar.updateLoveButton(s.isLiked());
            });
            
            player.currentTimeProperty().addListener((obs, ot, nt) -> {
                double totalMillis = m.getDuration().toMillis();
                if (totalMillis <= 0 || Double.isNaN(totalMillis)) return;
                double pct = nt.toMillis() / totalMillis;
                String cur = formatTime(nt);
                String tot = formatTime(m.getDuration());
                Platform.runLater(() -> {
                    playerBar.updateProgress(pct);
                    playerBar.updateTime(cur, tot);
                });
            });
            
            player.setOnEndOfMedia(() -> {
                if (repeatMode == 1) {
                    player.seek(Duration.ZERO);
                    player.play();
                } else {
                    playNext();
                }
            });
            
            player.play();
            playerBar.updatePlayButton(true);
            db.recordPlay(s);
            
            if (onSongChange != null) {
                Platform.runLater(onSongChange);
            }
        } catch (Exception e) {
            System.err.println("Error playing media: " + e.getMessage());
            playNext();
        }
    }
    
    public void togglePlay() {
        if (player == null) return;
        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
            playerBar.updatePlayButton(false);
        } else {
            player.play();
            playerBar.updatePlayButton(true);
        }
    }
    
    public void playNext() {
        ObservableList<Song> list = db.getAllSongs();
        if (list.isEmpty()) return;
        
        int idx;
        if (isShuffle) {
            idx = new Random().nextInt(list.size());
        } else {
            idx = list.indexOf(db.getCurrent()) + 1;
            if (idx >= list.size()) {
                idx = (repeatMode == 2) ? 0 : list.size() - 1;
            }
        }
        play(list.get(idx));
    }
    
    public void playPrevious() {
        ObservableList<Song> list = db.getAllSongs();
        if (list.isEmpty()) return;
        
        int idx = list.indexOf(db.getCurrent());
        if (idx == -1) idx = 1;
        idx -= 1;
        if (idx < 0) {
            idx = (repeatMode == 2) ? list.size() - 1 : 0;
        }
        play(list.get(idx));
    }
    
    public void seek(double position) {
        if (player != null && player.getTotalDuration() != null) {
            player.seek(player.getTotalDuration().multiply(position));
        }
    }
    
    public void setVolume(double volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }
    
    public void setShuffle(boolean shuffle) {
        this.isShuffle = shuffle;
    }
    
    public void setRepeatMode(int mode) {
        this.repeatMode = mode;
    }
    
    public void toggleLike() {
        Song s = db.getCurrent();
        if (s != null) {
            s.setLiked(!s.isLiked());
            playerBar.updateLoveButton(s.isLiked());
        }
    }
    
    private String formatTime(Duration d) {
        if (d == null || d.isUnknown()) return "0:00";
        int m = (int) d.toMinutes();
        int s = (int) d.toSeconds() % 60;
        return String.format("%d:%02d", m, s);
    }
    
    public void dispose() {
        if (player != null) {
            player.stop();
            player.dispose();
        }
    }
}
