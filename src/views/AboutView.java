package views;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import config.Constants;

public class AboutView extends VBox {
    private HostServices hostServices;

    public AboutView(HostServices hostServices) {
        super(30);
        this.hostServices = hostServices;
        setupUI();
    }

    private void setupUI() {
        setPadding(new Insets(40, 60, 40, 60));
        setStyle("-fx-background-color: " + Constants.BG_BLACK + ";");

        // App Title
        Label appTitle = new Label("Auralin Music Player");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 36));
        appTitle.setTextFill(Color.web("#FA2D48"));

        // Version
        Label version = new Label("Version 1.0.0");
        version.setFont(Font.font("System", FontWeight.NORMAL, 18));
        version.setTextFill(Color.web(Constants.COLOR_GRAY_TEXT));

        // Credits Section
        Label creditsTitle = new Label("Credits");
        creditsTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        creditsTitle.setTextFill(Color.WHITE);

        // Developer Credit
        VBox developerBox = createCreditBox(
            "Developer", 
            "Dip Karmokar", 
            "https://github.com/dipkarmokar21/",
            "https://www.linkedin.com/in/dipkarmokar/"
        );

        // Structure Design Credit
        VBox designerBox = createCreditBox(
            "Structure Design", 
            "Sekon Karmokar", 
            "https://github.com/sekon-karmokar",
            null // No LinkedIn provided
        );

        // Dependencies Section
        Label dependenciesTitle = new Label("Dependencies");
        dependenciesTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        dependenciesTitle.setTextFill(Color.WHITE);

        VBox javafxBox = createDependencyBox(
            "JavaFX 25.0.2", 
            "UI Framework", 
            "https://gluonhq.com/products/javafx/"
        );

        VBox mp3agicBox = createDependencyBox(
            "mp3agic 0.9.1", 
            "Audio Metadata Processing", 
            "https://github.com/mpatric/mp3agic/releases/tag/v0.9.1"
        );

        // Check for Updates Button
        Button updateButton = new Button("Check for Updates");
        updateButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        updateButton.setStyle(
            "-fx-background-color: " + "#ffffffff" + ";" +
            "-fx-text-fill: black;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        );
        updateButton.setOnAction(e -> showUpdateDialog());

        // Copyright
        Label copyright = new Label("Copyright © 2026 Dip's Auralin Tech Inc. All rights reserved.");
        copyright.setFont(Font.font("System", FontWeight.NORMAL, 12));
        copyright.setTextFill(Color.web(Constants.COLOR_GRAY_TEXT));

        // Layout
        VBox creditsSection = new VBox(15, creditsTitle, developerBox, designerBox);
        VBox dependenciesSection = new VBox(15, dependenciesTitle, javafxBox, mp3agicBox);
        
        HBox updateSection = new HBox(updateButton);
        updateSection.setAlignment(Pos.CENTER);

        getChildren().addAll(
            appTitle,
            version,
            new Separator(),
            creditsSection,
            new Separator(),
            dependenciesSection,
            new Separator(),
            updateSection,
            new Separator(),
            copyright
        );

        setAlignment(Pos.TOP_CENTER);
    }

    private VBox createCreditBox(String role, String name, String githubUrl, String linkedinUrl) {
        Label roleLabel = new Label(role + ":");
        roleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        roleLabel.setTextFill(Color.WHITE);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);

        // Create links container
        HBox linksBox = new HBox(15);
        linksBox.setAlignment(Pos.CENTER_LEFT);

        // GitHub link with icon
        if (githubUrl != null) {
            Button githubBtn = createSocialButton("GitHub", githubUrl, "🐙");
            linksBox.getChildren().add(githubBtn);
        }

        // LinkedIn link with icon
        if (linkedinUrl != null) {
            Button linkedinBtn = createSocialButton("LinkedIn", linkedinUrl, "💼");
            linksBox.getChildren().add(linkedinBtn);
        }

        return new VBox(5, roleLabel, nameLabel, linksBox);
    }

    private Button createSocialButton(String platform, String url, String icon) {
        Button btn;
        
        if (platform.equals("GitHub")) {
            // Create GitHub SVG icon
            javafx.scene.shape.SVGPath githubIcon = new javafx.scene.shape.SVGPath();
            githubIcon.setContent("M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8");
            githubIcon.setFill(javafx.scene.paint.Color.WHITE);
            githubIcon.setScaleX(1.2);
            githubIcon.setScaleY(1.2);
            
            javafx.scene.layout.StackPane iconContainer = new javafx.scene.layout.StackPane(githubIcon);
            iconContainer.setPrefSize(20, 20);
            
            Label textLabel = new Label(" " + platform);
            textLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            textLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            
            javafx.scene.layout.HBox content = new javafx.scene.layout.HBox(8, iconContainer, textLabel);
            content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            btn = new Button();
            btn.setGraphic(content);
            
        } else if (platform.equals("LinkedIn")) {
            // Create LinkedIn SVG icon
            javafx.scene.shape.SVGPath linkedinIcon = new javafx.scene.shape.SVGPath();
            linkedinIcon.setContent("M0 1.146C0 .513.526 0 1.175 0h13.65C15.474 0 16 .513 16 1.146v13.708c0 .633-.526 1.146-1.175 1.146H1.175C.526 16 0 15.487 0 14.854zm4.943 12.248V6.169H2.542v7.225zm-1.2-8.212c.837 0 1.358-.554 1.358-1.248-.015-.709-.52-1.248-1.342-1.248S2.4 3.226 2.4 3.934c0 .694.521 1.248 1.327 1.248zm4.908 8.212V9.359c0-.216.016-.432.08-.586.173-.431.568-.878 1.232-.878.869 0 1.216.662 1.216 1.634v3.865h2.401V9.25c0-2.22-1.184-3.252-2.764-3.252-1.274 0-1.845.7-2.165 1.193v.025h-.016l.016-.025V6.169h-2.4c.03.678 0 7.225 0 7.225z");
            linkedinIcon.setFill(javafx.scene.paint.Color.WHITE);
            linkedinIcon.setScaleX(1.2);
            linkedinIcon.setScaleY(1.2);
            
            javafx.scene.layout.StackPane iconContainer = new javafx.scene.layout.StackPane(linkedinIcon);
            iconContainer.setPrefSize(20, 20);
            
            Label textLabel = new Label(" " + platform);
            textLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            textLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            
            javafx.scene.layout.HBox content = new javafx.scene.layout.HBox(8, iconContainer, textLabel);
            content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            btn = new Button();
            btn.setGraphic(content);
            
        } else {
            // Fallback for other platforms
            btn = new Button(icon + " " + platform);
            btn.setFont(Font.font("System", FontWeight.NORMAL, 14));
        }
        
        btn.setStyle(
            "-fx-background-color: #2A2A2A;" +  // Gray background
            "-fx-text-fill: white;" +            // White text
            "-fx-background-radius: 15;" +
            "-fx-padding: 8 15;" +
            "-fx-cursor: hand;"
        );
        
        // Hover effect
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: black;" +     // Black background on hover
            "-fx-text-fill: white;" +            // White text on hover
            "-fx-background-radius: 15;" +
            "-fx-padding: 8 15;" +
            "-fx-cursor: hand;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #2A2A2A;" +  // Back to gray
            "-fx-text-fill: white;" +            // White text
            "-fx-background-radius: 15;" +
            "-fx-padding: 8 15;" +
            "-fx-cursor: hand;"
        ));

        btn.setOnAction(e -> {
            if (hostServices != null) {
                hostServices.showDocument(url);
            }
        });

        return btn;
    }

    private VBox createDependencyBox(String name, String description, String url) {
        Hyperlink nameLink = new Hyperlink(name);
        nameLink.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLink.setTextFill(Color.web(Constants.SPOTIFY_GREEN));
        nameLink.setOnAction(e -> {
            if (hostServices != null) {
                hostServices.showDocument(url);
            }
        });

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web(Constants.COLOR_GRAY_TEXT));

        return new VBox(3, nameLink, descLabel);
    }

    private void showUpdateDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Check for Updates");
        alert.setHeaderText("Update Check");
        alert.setContentText("Checking for updates...\n\nYou are currently running the latest version of Auralin Music Player (v1.0.0).\n\nNo updates available at this time.");
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + Constants.BG_BLACK + ";");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: " + Constants.SIDEBAR_BLACK + ";");
        
        // Fix header text color with multiple approaches
        javafx.application.Platform.runLater(() -> {
            dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: #ffffffff;");
            dialogPane.lookup(".header .label").setStyle("-fx-text-fill: #ffffffff;");
            dialogPane.lookupAll(".header-panel .label").forEach(node -> {
                node.setStyle("-fx-text-fill: #ffffffff;");
            });
        });
        
        alert.showAndWait();
    }
}