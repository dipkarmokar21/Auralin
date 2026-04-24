package views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import components.MusicCard;
import model.Song;
import config.Constants;
import java.util.List;

/**
 * HomeView — greeting + Recently Played + Made For You grids.
 *
 * CHANGES:
 *  - Removed setMinWidth(800) — fills whatever space is available
 *  - FlowPane uses TilePane-style equal sizing so cards fill every row
 *  - prefWrapLength binds to view width so grid reflows on window resize
 *  - No empty space: cards are 160px wide with 16px gaps; FlowPane fills the row
 */
public class HomeView extends VBox {

    private final String userName;
    private final MusicCard.MusicCardListener cardListener;
    private FlowPane recentGrid;
    private FlowPane recommendGrid;

    public HomeView(String userName, MusicCard.MusicCardListener cardListener) {
        super(30);
        this.userName = userName;
        this.cardListener = cardListener;
        setPadding(new Insets(40));
        setStyle("-fx-background-color: linear-gradient(to bottom, #222222, " + Constants.BG_BLACK + ");");
    }

    /** Called by ViewManager to populate the view with song data */
    public void setContent(List<Song> recentlyPlayed, List<Song> recommendations) {
        getChildren().clear();

        Label greeting = new Label("Hello " + userName);
        greeting.setFont(Font.font("System", FontWeight.BOLD, 32));
        greeting.setTextFill(Color.WHITE);

        recentGrid = createCardGrid(recentlyPlayed);
        recommendGrid = createCardGrid(recommendations);

        VBox recentSection = new VBox(15,
            createSectionLabel("Recently Played"),
            recentGrid
        );
        VBox recommendSection = new VBox(15,
            createSectionLabel("Made For You"),
            recommendGrid
        );

        VBox.setVgrow(recentSection,    Priority.ALWAYS);
        VBox.setVgrow(recommendSection, Priority.ALWAYS);

        getChildren().addAll(greeting, recentSection, recommendSection);
    }

    private Label createSectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        lbl.setTextFill(Color.WHITE);
        return lbl;
    }

    /**
     * Creates a FlowPane of MusicCards that fills the available width.
     *
     * CHANGED: prefWrapLength binds to this view's width so the grid always
     * reflows correctly when the window is resized — no empty space on the right.
     * Cards are 160px wide with 16px gaps; FlowPane packs as many per row as fit.
     */
    private FlowPane createCardGrid(List<Song> songs) {
        FlowPane grid = new FlowPane(16, 16);
        // CHANGED: bind wrap width to HomeView width so cards fill every row on resize
        grid.prefWrapLengthProperty().bind(widthProperty().subtract(80)); // subtract padding
        for (Song s : songs) {
            grid.getChildren().add(new MusicCard(s, cardListener));
        }
        return grid;
    }
}
