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
    // CHANGED: separate callback for like toggles so views refresh live
    private Runnable onLikeChange;
    // Callback for play/pause state changes
    private Runnable onPlayStateChange;
    
    public PlayerController(DatabaseService db, PlayerBar playerBar) {
        this.db = db;
        this.playerBar = playerBar;
    }
    
    public void setOnSongChange(Runnable callback) {
        this.onSongChange = callback;
    }

    /** Called when the like state changes — used to refresh views live */
    public void setOnLikeChange(Runnable callback) {
        this.onLikeChange = callback;
    }
    
    /** Called when play/pause state changes — used to update play/pause icons */
    public void setOnPlayStateChange(Runnable callback) {
        this.onPlayStateChange = callback;
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
                    // Repeat ON — restart the same song
                    player.seek(Duration.ZERO);
                    player.play();
                } else {
                    // Repeat OFF — advance to next song
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
        
        // Notify views to update play/pause icons
        if (onPlayStateChange != null) {
            Platform.runLater(onPlayStateChange);
        }
    }
    
    public void playNext() {
        ObservableList<Song> allSongs = db.getAllSongs();
        if (allSongs.isEmpty()) return;

        int nextIndex;
        if (isShuffle) {
            // Pick a random song
            nextIndex = new Random().nextInt(allSongs.size());
        } else {
            nextIndex = allSongs.indexOf(db.getCurrent()) + 1;
            // Wrap around to first song if at the end
            if (nextIndex >= allSongs.size()) nextIndex = 0;
        }
        play(allSongs.get(nextIndex));
    }

    public void playPrevious() {
        ObservableList<Song> allSongs = db.getAllSongs();
        if (allSongs.isEmpty()) return;

        int prevIndex = allSongs.indexOf(db.getCurrent()) - 1;
        // Wrap around to last song if at the beginning
        if (prevIndex < 0) prevIndex = allSongs.size() - 1;
        play(allSongs.get(prevIndex));
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
            db.saveToDisk();
            if (onLikeChange != null) Platform.runLater(onLikeChange);
        }
    }

    /**
     * Called when a like is toggled from the SongTable (not the PlayerBar heart).
     * Syncs the PlayerBar heart icon to match the song's current liked state.
     */
    public void syncLikeState(Song song) {
        playerBar.updateLoveButton(song.isLiked());
    }
    
    /**
     * Returns true if a song is currently playing (not paused)
     */
    public boolean isPlaying() {
        return player != null && player.getStatus() == MediaPlayer.Status.PLAYING;
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
