package utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import services.DatabaseService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileImportService {
    private DatabaseService db;
    
    public FileImportService(DatabaseService db) {
        this.db = db;
    }
    
    public void importFiles() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Audio Files");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.flac")
        );
        List<File> files = fc.showOpenMultipleDialog(null);
        if (files != null) {
            files.forEach(db::addFile);
        }
    }
    
    public void importFolder(ProgressBar progressBar, LoadingCallback callback) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Music Folder");
        File folder = dc.showDialog(null);
        if (folder == null) return;
        
        callback.onLoadingStart();
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<File> allFiles = new ArrayList<>();
                
                try (Stream<Path> paths = Files.walk(folder.toPath())) {
                    paths.filter(Files::isRegularFile)
                            .map(Path::toFile)
                            .filter(f -> {
                                String n = f.getName().toLowerCase();
                                return n.endsWith(".mp3") || n.endsWith(".wav") || 
                                       n.endsWith(".flac") || n.endsWith(".m4a");
                            })
                            .forEach(allFiles::add);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                for (int i = 0; i < allFiles.size(); i++) {
                    final int idx = i;
                    File currentFile = allFiles.get(idx);
                    Platform.runLater(() -> {
                        db.addFile(currentFile);
                        callback.onProgress(idx + 1, allFiles.size());
                    });
                    updateProgress(idx + 1, allFiles.size());
                    Thread.sleep(50);
                }
                return null;
            }
        };
        
        if (progressBar != null) {
            progressBar.progressProperty().bind(task.progressProperty());
        }
        
        task.setOnSucceeded(e -> callback.onLoadingComplete());
        task.setOnFailed(e -> callback.onLoadingComplete());
        
        new Thread(task).start();
    }
    
    public interface LoadingCallback {
        void onLoadingStart();
        void onProgress(int current, int total);
        void onLoadingComplete();
    }
}
