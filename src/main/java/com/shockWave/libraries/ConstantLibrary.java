package com.shockWave.libraries;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

public class ConstantLibrary {
    // For in Gameplay
    public static class GamePlay {
        public static final Font PLAYER_DISPLAY_NAME_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    }
    //gui colors
    public static class GUI {
        //   Layout
        public static final Point MENU_CORNER = new Point(300, 360);
        public static final Point MENU_COMPONENT_OFFSET = new Point(100, 60);

        //   Colors
        public static final Color SETTINGS_BACKGROUND = new Color(40, 40, 40);
        public static final Color SETTINGS_BORDER = new Color(80, 80, 80);
        // A button will usually be standard.
        // If a button is considered "true" it will be Enabled.
        // If the cursor is over it or the controller input has it in focus it will be Selected.

        public static final int BORDER_SIZE = 5;
        public static final int BORDER_ROUNDED_SIZE = 20;
        public static final Font GBUTTON_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 30);
        public static final Font GTEXTFIELD_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 30);
        public static final Font CLIENT_PLACEHOLDER_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 20);

        public static class MenuComponent {
            private static Color clearColor = new Color(0, 0, 0, 0);

            //standard
            public static final Color STANDARD_CENTER = clearColor;
            public static final Color STANDARD_BORDER = clearColor;
            public static final Color STANDARD_TEXT = Color.LIGHT_GRAY;
            //enabled
            public static final Color ENABLED_CENTER = clearColor; // These will probably never be used.
            public static final Color ENABLED_BORDER = clearColor; // These will probably never be used.
            public static final Color ENABLED_TEXT = Color.WHITE;
            //selected
            public static final Color SELECTED_CENTER = clearColor;
            public static final Color SELECTED_BORDER = clearColor;
            //public static final Color SELECTED_BORDER = Color.LIGHT_GRAY;
            public static final Color SELECTED_TEXT = Color.WHITE;
        }
        public static class SettingsComponent {
            //standard
            public static final Color STANDARD_CENTER = new Color(50, 50, 50);
            public static final Color STANDARD_BORDER = new Color(70, 70, 70);
            public static final Color STANDARD_TEXT = new Color(140, 140, 140);
            //enabled
            public static final Color ENABLED_CENTER = new Color(70, 70, 70);
            public static final Color ENABLED_BORDER = new Color(100, 100, 100);
            public static final Color ENABLED_TEXT = new Color(200, 200, 200);
            //selected
            public static final Color SELECTED_CENTER = new Color(100, 100, 100);
            public static final Color SELECTED_BORDER = new Color(140, 140, 140);
            public static final Color SELECTED_TEXT = new Color(255, 255, 255);
        }
    }
}
