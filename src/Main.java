import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import components.PlayerBar;
import components.Sidebar;
import controllers.PlayerController;
import managers.ViewManager;
import services.DatabaseService;
import utils.FileImportService;
import config.Constants;
import views.LibraryView;

public class Main extends Application {
    private DatabaseService db = new DatabaseService();
    private StackPane contentArea = new StackPane();
    private String userName = "User";
    
    private Sidebar sidebar;
    private PlayerBar playerBar;
    private PlayerController playerController;
    private ViewManager viewManager;
    private FileImportService fileImportService;

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        // Initialize services
        fileImportService = new FileImportService(db);
        
        // Create UI
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + Constants.BG_BLACK + ";");
        
        // Create player bar
        playerBar = new PlayerBar(createPlayerBarListener());
        
        // Create player controller
        playerController = new PlayerController(db, playerBar);
        playerController.setOnSongChange(() -> viewManager.refreshCurrentView());
        
        // Create view manager
        viewManager = new ViewManager(db, contentArea, userName, playerController);
        viewManager.setFileImportCallback(new ViewManager.FileImportCallback() {
            @Override
            public void onAddFiles() {
                fileImportService.importFiles();
            }
            
            @Override
            public void onAddFolder() {
                fileImportService.importFolder(sidebar.getLoadingBar(), new FileImportService.LoadingCallback() {
                    @Override
                    public void onLoadingStart() {
                        sidebar.showLoading(true, "Loading...");
                    }
                    
                    @Override
                    public void onProgress(int current, int total) {
                        sidebar.showLoading(true, "Loading: " + current + "/" + total);
                    }
                    
                    @Override
                    public void onLoadingComplete() {
                        sidebar.showLoading(false, "");
                    }
                });
            }
        });
        
        // Create sidebar
        sidebar = new Sidebar(view -> viewManager.showView(view));
        
        // Show initial view
        viewManager.showView("Home");
        
        // Setup layout
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        root.setBottom(playerBar);
        
        // Create scene
        Scene scene = new Scene(root, 1250, 850);
        applyGlobalStyles(scene);
        
        stage.setTitle("Auralin - Premium Offline Music Player");
        stage.setScene(scene);
        stage.show();
    }
    
    private PlayerBar.PlayerBarListener createPlayerBarListener() {
        return new PlayerBar.PlayerBarListener() {
            @Override
            public void onPlayPause() {
                playerController.togglePlay();
            }
            
            @Override
            public void onNext() {
                playerController.playNext();
            }
            
            @Override
            public void onPrevious() {
                playerController.playPrevious();
            }
            
            @Override
            public void onShuffle(boolean enabled) {
                playerController.setShuffle(enabled);
            }
            
            @Override
            public void onRepeat(int mode) {
                playerController.setRepeatMode(mode);
            }
            
            @Override
            public void onLove() {
                playerController.toggleLike();
            }
            
            @Override
            public void onSeek(double position) {
                playerController.seek(position);
            }
            
            @Override
            public void onVolumeChange(double volume) {
                playerController.setVolume(volume);
            }
        };
    }
    
    private void applyGlobalStyles(Scene scene) {
        String dataUri = "data:text/css," + Constants.GLOBAL_CSS
            .replace(" ", "%20")
            .replace("#", "%23")
            .replace("(", "%28")
            .replace(")", "%29");
        scene.getStylesheets().add(dataUri);
    }
    
    @Override
    public void stop() {
        playerController.dispose();
    }
}
