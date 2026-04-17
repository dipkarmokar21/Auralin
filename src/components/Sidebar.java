package components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import config.Constants;

public class Sidebar extends VBox {
    private VBox navButtons;
    private ProgressBar loadingBar = new ProgressBar(0);
    private Label lblLoadingStatus = new Label("");
    private String activeView = "Home";
    private SidebarListener listener;
    
    public interface SidebarListener {
        void onNavigate(String view);
    }
    
    public Sidebar(SidebarListener listener) {
        super(25);
        this.listener = listener;
        setupUI();
    }
    
    private void setupUI() {
        setPrefWidth(240);
        setPadding(new Insets(30, 20, 20, 20));
        setStyle("-fx-background-color: " + Constants.SIDEBAR_BLACK + ";");
        
        Label logo = new Label("Auralin");
        logo.setFont(Font.font("System", FontWeight.BOLD, 28));
        logo.setTextFill(Color.web("#FA2D48"));
        
        navButtons = new VBox(15);
        updateNavButtons();
        
        VBox loadingBox = new VBox(5, lblLoadingStatus, loadingBar);
        loadingBar.setMaxWidth(Double.MAX_VALUE);
        loadingBar.setVisible(false);
        lblLoadingStatus.setTextFill(Color.web(Constants.SPOTIFY_GREEN));
        lblLoadingStatus.setFont(Font.font(11));
        
        getChildren().addAll(logo, navButtons, new Separator(), loadingBox);
    }

    private void updateNavButtons() {
        navButtons.getChildren().clear();
        navButtons.getChildren().addAll(
                createNavBtn("Home", "🏠", activeView.equals("Home"), e -> navigate("Home")),
                createNavBtn("Search", "🔍", activeView.equals("Search"), e -> navigate("Search")),
                createNavBtn("Your Library", "📚", activeView.equals("Library"), e -> navigate("Library")),
                createNavBtn("Liked Songs", "❤", activeView.equals("Liked"), e -> navigate("Liked"))
        );
    }
    
    private Button createNavBtn(String text, String icon, boolean isActive, javafx.event.EventHandler<javafx.event.ActionEvent> event) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-btn");
        if (isActive) btn.setStyle("-fx-text-fill: " + Constants.SPOTIFY_GREEN + ";");
        btn.setOnAction(event);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }
    
    private void navigate(String view) {
        activeView = view;
        updateNavButtons();
        listener.onNavigate(view);
    }
    
    public void setActiveView(String view) {
        activeView = view;
        updateNavButtons();
    }
    
    public void showLoading(boolean show, String status) {
        loadingBar.setVisible(show);
        lblLoadingStatus.setText(status);
    }
    
    public ProgressBar getLoadingBar() {
        return loadingBar;
    }
}
