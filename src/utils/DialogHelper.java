package utils;

import javafx.scene.control.*;
import config.Constants;

public class DialogHelper {
    
    public static String showUserNameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Welcome");
        dialog.setHeaderText("Welcome to Auralin Music Player!");
        dialog.setContentText("Please enter your name:");
        dialog.setGraphic(null);
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
            "data:text/css," + Constants.DIALOG_CSS
                .replace(" ", "%20")
                .replace("#", "%23")
        );
        
        TextField inputField = dialog.getEditor();
        javafx.scene.Node okButton = dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        
        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty());
        });
        
        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            return result.get().trim();
        }
        return "User";
    }
}
