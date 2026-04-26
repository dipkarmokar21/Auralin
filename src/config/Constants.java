package config;

public class Constants {

    public static final String COLOR_RED       = "#FA2D48";
    
    public static final String COLOR_GRAY_TEXT = "#B3B3B3";
    public static final String COLOR_BG_DARK   = "#121212";
    public static final String COLOR_BG_BLACK  = "#000000";
    public static final String COLOR_BG_CARD   = "#181818";
    public static final String COLOR_BG_HOVER  = "#2A2A2A";

    public static final String SPOTIFY_GREEN = COLOR_RED;
    public static final String BG_BLACK      = COLOR_BG_DARK;
    public static final String SIDEBAR_BLACK = COLOR_BG_BLACK;
    public static final String TEXT_GRAY     = COLOR_GRAY_TEXT;

    private static final java.net.URL _ART_URL =
            Constants.class.getResource("/resources/9973171-Photoroom.png");
    public static final String DEFAULT_ART =
            _ART_URL != null ? _ART_URL.toExternalForm() : "";

    

    public static final String GLOBAL_CSS = buildGlobalCss();

    private static String buildGlobalCss() {
        return
        ".nav-btn { -fx-background-color: transparent; -fx-text-fill: " + COLOR_GRAY_TEXT + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; }" +
        ".nav-btn:hover { -fx-text-fill: white; }" +
        ".action-btn { -fx-background-color: #282828; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand; }" +
        ".action-btn:hover { -fx-background-color: #3E3E3E; }" +
        ".music-card { -fx-background-color: " + COLOR_BG_CARD + "; -fx-background-radius: 15; -fx-cursor: hand; }" +
        ".music-card:hover { -fx-background-color: #282828; }" +
        ".spotify-table { -fx-background-color: transparent; -fx-table-cell-border-color: transparent; }" +
        ".spotify-table .column-header-background { -fx-background-color: #282828; -fx-background-radius: 10; -fx-padding: 0 4; }" +
        ".spotify-table .column-header { -fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 10 14; }" +



        ".spotify-table .column-header .sort-order-dots-container { -fx-padding: 0; -fx-pref-width: 0; }" +
        ".spotify-table .table-row-cell { -fx-background-color: transparent; -fx-text-fill: white; }" +
        ".spotify-table .table-row-cell:hover { -fx-background-color: " + COLOR_BG_HOVER + "; }" +
        ".play-circle { -fx-background-color: white; -fx-background-radius: 50; -fx-min-width: 50; -fx-min-height: 50; -fx-text-fill: black; -fx-font-size: 20px; -fx-cursor: hand; -fx-border-width: 0; }" +
        ".play-circle:hover { -fx-scale-x: 1.05; -fx-scale-y: 1.05; }" +
        ".control-icon { -fx-background-color: transparent; -fx-text-fill: " + COLOR_GRAY_TEXT + "; -fx-cursor: hand; -fx-border-width: 0; }" +
        ".control-icon:hover { -fx-text-fill: white; }" +
        ".spotify-slider { -fx-pref-height: 10; }" +
        ".spotify-slider .track { -fx-background-color: #4D4D4D; -fx-background-radius: 5; }" +
        ".spotify-slider .thumb { -fx-background-color: white; -fx-background-radius: 10; -fx-pref-width: 14; -fx-pref-height: 14; }" +
        ".search-field { -fx-background-color: white; -fx-background-radius: 25; -fx-padding: 12 25; -fx-font-size: 16px; }";


    }
}
