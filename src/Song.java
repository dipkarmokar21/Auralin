import javafx.scene.image.Image;
import javafx.util.Duration;

public class Song {
    private String title;
    private String artist;
    private String filePath;
    private Image artwork;
    private Duration duration = Duration.ZERO;
    public int plays = 0;
    public long lastPlayed = 0;
    private boolean liked = false;

