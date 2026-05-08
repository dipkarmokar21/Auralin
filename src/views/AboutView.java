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
        // Responsive padding based on screen size
        setPadding(new Insets(50, 100, 50, 100));
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

        // Check for Updates Button with premium design and refresh icon
        Button updateButton = new Button();
        
        // Create refresh SVG icon
        javafx.scene.shape.SVGPath refreshIcon = new javafx.scene.shape.SVGPath();
        refreshIcon.setContent("M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15");
        refreshIcon.setFill(Color.WHITE);
        refreshIcon.setScaleX(1.2);
        refreshIcon.setScaleY(1.2);
        refreshIcon.setStroke(Color.WHITE);
        refreshIcon.setStrokeWidth(1.5);
        
        javafx.scene.layout.StackPane iconContainer = new javafx.scene.layout.StackPane(refreshIcon);
        iconContainer.setPrefSize(20, 20);
        
        Label buttonText = new Label("  Check for Updates");
        buttonText.setTextFill(Color.WHITE);
        buttonText.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        javafx.scene.layout.HBox buttonContent = new javafx.scene.layout.HBox(8, iconContainer, buttonText);
        buttonContent.setAlignment(javafx.geometry.Pos.CENTER);
        
        updateButton.setGraphic(buttonContent);
        updateButton.setPrefWidth(280);
        updateButton.setPrefHeight(55);
        updateButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #007acc, #005a9e);" +
            "-fx-background-radius: 25;" +
            "-fx-border-color: #005a9e;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 25;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,122,204,0.4), 15, 0, 0, 5);"
        );
        
        // Add premium hover animation
        updateButton.setOnMouseEntered(e -> {
            updateButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1e88e5, #007acc);" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: #007acc;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,122,204,0.6), 20, 0, 0, 8);"
            );
        });
        
        updateButton.setOnMouseExited(e -> {
            updateButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #007acc, #005a9e);" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: #005a9e;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 25;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,122,204,0.4), 15, 0, 0, 5);"
            );
        });
        
        updateButton.setOnAction(e -> showUpdateDialog());

        // Copyright
        Label copyright = new Label("Copyright © 2026 Dip's Auralin Tech Inc. All rights reserved.");
        copyright.setFont(Font.font("System", FontWeight.NORMAL, 12));
        copyright.setTextFill(Color.web(Constants.COLOR_GRAY_TEXT));

        // Layout with better spacing and organization - RESPONSIVE CONTAINER
        VBox contentContainer = new VBox();
        contentContainer.setAlignment(Pos.TOP_CENTER);
        contentContainer.setMaxWidth(1000); // Max width for large screens
        contentContainer.setMinWidth(600);  // Min width for small screens
        
        VBox titleSection = new VBox(10);
        titleSection.setAlignment(Pos.CENTER);
        titleSection.getChildren().addAll(appTitle, version);
        
        VBox creditsSection = new VBox(20);
        creditsSection.setPadding(new Insets(20, 0, 20, 0));
        creditsSection.getChildren().addAll(creditsTitle, developerBox, designerBox);
        
        VBox dependenciesSection = new VBox(20);
        dependenciesSection.setPadding(new Insets(20, 0, 20, 0));
        
        // Dependencies in horizontal layout for better use of space
        HBox dependenciesGrid = new HBox(50);
        dependenciesGrid.setAlignment(Pos.CENTER_LEFT);
        dependenciesGrid.getChildren().addAll(javafxBox, mp3agicBox);
        
        dependenciesSection.getChildren().addAll(dependenciesTitle, dependenciesGrid);
        
        // Enhanced update button section
        VBox updateSection = new VBox(updateButton);
        updateSection.setAlignment(Pos.CENTER);
        updateSection.setPadding(new Insets(30, 0, 30, 0));
        
        VBox copyrightSection = new VBox(copyright);
        copyrightSection.setAlignment(Pos.CENTER);
        copyrightSection.setPadding(new Insets(20, 0, 0, 0));

        // Create styled separators
        javafx.scene.control.Separator sep1 = new javafx.scene.control.Separator();
        sep1.setMaxWidth(600);
        sep1.setStyle("-fx-background-color: #333333;");
        
        javafx.scene.control.Separator sep2 = new javafx.scene.control.Separator();
        sep2.setMaxWidth(600);
        sep2.setStyle("-fx-background-color: #333333;");
        
        javafx.scene.control.Separator sep3 = new javafx.scene.control.Separator();
        sep3.setMaxWidth(600);
        sep3.setStyle("-fx-background-color: #333333;");

        // Add content to container with improved spacing
        contentContainer.setSpacing(25);
        contentContainer.getChildren().addAll(
            titleSection,
            sep1,
            creditsSection,
            sep2,
            dependenciesSection,
            sep3,
            updateSection,
            copyrightSection
        );

        // Main layout - always centered
        setAlignment(Pos.CENTER);
        setFillWidth(false); // Don't fill full width
        getChildren().add(contentContainer);
        
        // Bind responsive behavior
        widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();
            if (width > 1400) {
                // Large screens - more padding
                setPadding(new Insets(60, 150, 60, 150));
            } else if (width > 1000) {
                // Medium screens - normal padding
                setPadding(new Insets(50, 100, 50, 100));
            } else {
                // Small screens - less padding
                setPadding(new Insets(40, 60, 40, 60));
            }
        });
    }

    private VBox createCreditBox(String role, String name, String githubUrl, String linkedinUrl) {
        VBox creditBox = new VBox(12);
        creditBox.setPadding(new Insets(15, 20, 15, 20));
        creditBox.setStyle(
            "-fx-background-color: #1A1A1A;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #333333;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;"
        );
        
        Label roleLabel = new Label(role);
        roleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        roleLabel.setTextFill(Color.web("#FA2D48"));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);

        // Create links container with better spacing
        HBox linksBox = new HBox(12);
        linksBox.setAlignment(Pos.CENTER_LEFT);
        linksBox.setPadding(new Insets(8, 0, 0, 0));

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

        creditBox.getChildren().addAll(roleLabel, nameLabel, linksBox);
        return creditBox;
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
        alert.setTitle("Update Check");
        alert.setHeaderText(null); // Remove default header
        
        // Create custom content with premium design
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30, 40, 30, 40));
        content.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1a1a1a, #0d1117);" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: #30363d;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 5);"
        );
        
        // Custom checkmark icon with green circular background
        javafx.scene.shape.SVGPath checkIcon = new javafx.scene.shape.SVGPath();
        checkIcon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        checkIcon.setFill(Color.WHITE);
        checkIcon.setScaleX(1.5);
        checkIcon.setScaleY(1.5);
        
        javafx.scene.shape.Circle iconBackground = new javafx.scene.shape.Circle(25);
        iconBackground.setFill(Color.web("#28a745"));
        iconBackground.setEffect(new javafx.scene.effect.DropShadow(
            javafx.scene.effect.BlurType.GAUSSIAN, 
            Color.web("#28a745", 0.3), 
            10, 0, 0, 2
        ));
        
        javafx.scene.layout.StackPane iconContainer = new javafx.scene.layout.StackPane();
        iconContainer.getChildren().addAll(iconBackground, checkIcon);
        
        // Title
        Label titleLabel = new Label("Update Check Complete");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);
        
        // Message with premium styling
        Label messageLabel = new Label("You are running the latest version!");
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        messageLabel.setTextFill(Color.web("#8b949e"));
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        // Version info
        Label versionLabel = new Label("Auralin Music Player v1.0.0");
        versionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        versionLabel.setTextFill(Color.web("#FA2D48"));
        
        // Status message
        Label statusLabel = new Label("No updates available at this time.");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        statusLabel.setTextFill(Color.web("#6e7681"));
        
        content.getChildren().addAll(iconContainer, titleLabel, messageLabel, versionLabel, statusLabel);
        
        // Set custom content
        alert.getDialogPane().setContent(content);
        
        // Style the dialog pane with premium design
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setPrefSize(450, 280);
        dialogPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0d1117, #161b22);" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: #30363d;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 25, 0, 0, 8);"
        );
        
        // Style the OK button with premium green gradient
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("OK");
        okButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        okButton.setPrefWidth(120);
        okButton.setPrefHeight(40);
        okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #28a745, #1e7e34);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #1e7e34;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(40,167,69,0.3), 8, 0, 0, 2);"
        );
        
        // Add hover effect to OK button
        okButton.setOnMouseEntered(e -> okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #34ce57, #28a745);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #28a745;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(40,167,69,0.5), 12, 0, 0, 3);"
        ));
        
        okButton.setOnMouseExited(e -> okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #28a745, #1e7e34);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #1e7e34;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(40,167,69,0.3), 8, 0, 0, 2);"
        ));
        
        alert.showAndWait();
    }
}