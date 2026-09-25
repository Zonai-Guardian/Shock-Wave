package com.spiritOfEldervine;

import com.spiritOfEldervine.client.Client;
import com.spiritOfEldervine.engine.EngineCalculator;
import com.spiritOfEldervine.graphics.GraphicsCalculator;
import com.spiritOfEldervine.graphics.Menu;
import com.spiritOfEldervine.graphics.MenuBackgroundManager;
import com.spiritOfEldervine.graphics.MenuManager;
import com.spiritOfEldervine.graphics.RenderEngine;
import com.spiritOfEldervine.graphics.g_components.GButton;
import com.spiritOfEldervine.graphics.g_components.GButtonTemplate;
import com.spiritOfEldervine.graphics.g_components.GTextField;
import com.spiritOfEldervine.graphics.g_components.GComponent.ActivationType;
import com.spiritOfEldervine.graphics.g_components.GComponent.GComponentSizes;
import com.spiritOfEldervine.input_devices.ConsoleController;
import com.spiritOfEldervine.input_devices.Keyboard;
import com.spiritOfEldervine.input_devices.Mouse;
import com.spiritOfEldervine.libraries.ImageLibrary;
import com.spiritOfEldervine.savers.KeyBindings;
import com.spiritOfEldervine.savers.Settings;
import com.spiritOfEldervine.savers.Settings.DebugEnum;
import com.spiritOfEldervine.server.Server;

//Maven imports
import com.studiohartman.jamepad.ControllerButton;

//java imports
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/*
Programmer Codes:
  #Danger  //Terminates or stops the game to some extent
  #Warning  //Code that should be enabled was dissabled for some reason
  #InDev  //Code that is unfinished that will need worked on more before it can work properly
*/

// Bible Link: https://www.biblegateway.com/passage/?search=Genesis%201&version=NIV

public class Game implements Runnable {
    //this class will handle all of the ui and game status/state and all variables that should not be deleted when the player exits a world
    
    private JFrame frame;
    private GamePanel panel;
    // Get the default screen device and set full-screen
    private GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    //resolution scaling
    public static Dimension gameResolution = new Dimension(1920, 1080); //this is the resolution that the game will be drawn at and then the Graphics will automatically scale and center it.
    public static FitTypes gameFitType = FitTypes.FIT;
    public static Point2D.Double displayScale = new Point2D.Double(1.0, 1.0);
    public static Point displayOffset = new Point(0, 0);
    //windowed or full-screen
    public static Dimension windowedSize = new Dimension(960, 540);
    public static boolean fullScreen = true;

    //game loop vars
    private int fps = 0;
    public static final int TARGET_UPS = 60;
    private final double TIME_BETWEEN_UPDATES = 1000000000 / TARGET_UPS;
    
    public String elementBeingLoaded = "Nothing";
    public Double loadDataProgress = 0.0;

    //loaders
    public static FileModifier fileModifier = new FileModifier();

    //server-client communications ??(don't know why this is here...)

    //client and server...obviously
    public static Client client = null; //this will handle the in-game stuff like moving.
    public static Server server = null; //this will handle hosting the game on LAN for others to join.

    public static boolean isGameFrozen = false; // This can freeze the game

    // Nearly all enums are kept here in Game.java
    // enums (being static makes them belong to the class, not the object)
    public static enum HostOrGuest {MENUS, HOST, GUEST}
    public static enum GameState {STARTUP, LOADING, MENUS, PLAY} // used to have: NOTIFICATION
    public static enum FitTypes {
        FIT, //resizes the image so that all of it is displayed, even if there is some empty space on the screen      [Keeps Aspect Ratio]
        FILL, //resizes the image so that all of the screen is filled, even if some of the image isn't shown          [Keeps Aspect Ratio]
        STRETCH, //resizes the image so that it matches the X and Y axes of the screen even if the image is stretched [Breaks Aspect Ratio]
        CENTER //doesn't change the size of the image, just centers it in the screen                                  [Keeps Aspect Ratio]
    }
    public static enum InputTypes {KEYBOARD_AND_MOUSE, KEYBOARD, MOUSE, CONTROLLER}
    public static enum ControllerTypes {XBOX, NINTENDO, PLAY_STATION}
    public static enum MouseButton {LEFT, MIDDLE, RIGHT}
    public static enum Direction4 {RIGHT, BOTTOM, LEFT, TOP, CENTER;} // The Word is where the direction is going toward (from Opposite_of_direction to direction) These will not be used for entity movement! They are for menu animations only!
    public static enum Direction8 {CENTER_CENTER, RIGHT_CENTER, RIGHT_BOTTOM, CENTER_BOTTOM, LEFT_BOTTOM, LEFT_CENTER, LEFT_TOP, CENTER_TOP, RIGHT_TOP} //These are for GComponent placements
    public static enum ControlType {KEYBOARD_AND_MOUSE, CONTROLLER}

    //game state stuff
    public static HostOrGuest hostOrGuest = HostOrGuest.MENUS;
    public static GameState gameState = GameState.STARTUP;
    public static ControlType controllType = ControlType.KEYBOARD_AND_MOUSE;
    
    //libraries
    public static ImageLibrary images = new ImageLibrary();
    public MenuBackgroundManager menuBackgroundManager = new MenuBackgroundManager(
        new String[] {
            "background1",
            "background2",
            "background3",
            "background4",
            "background5"
        }, //all background image names [!!MUST BE ACTUAL LOADED IMAGES!!]
        EngineCalculator.secondsToFrames(4.0), //display time in seconds
        EngineCalculator.secondsToFrames(0.5), //fade time in seconds
        FitTypes.FIT
    );
    public static Settings settings;
    public static MenuManager menuManager = new MenuManager();

    //Input
    public static KeyBindings keyBindings = new KeyBindings(); // Key Bindings
    public static Keyboard keyboard = new Keyboard(); // Keyboard
    public static Mouse mouse = new Mouse(); // Mouse
    public static ConsoleController controller = new ConsoleController(); // Controller
    public static String word = ""; // Test for typing


    public void start() {
        frame = new JFrame("Spirit Of Eldervine");
        frame.setPreferredSize(new Dimension(gameResolution.width / 2, gameResolution.height / 2));

        frame.setPreferredSize(windowedSize);
        panel = new GamePanel();

        updateFullScreen();

        new Thread(this).start();
    }
    public static void exitGame(int index) {
        Game.controller.controllerManager.quitSDLGamepad(); //discard controller data?
        // should save settings

        // Shut down server
        shutDownServer();
        System.exit(0);
    }
    public void toggleFullScreen() {
        fullScreen = !fullScreen;
        updateFullScreen();
    }
    private void updateFullScreen() {
        if (fullScreen) {
            setFullScreen();
        } else {
            setWindowed();
        }
    }
    public void setFullScreen() {
        fullScreen = true;
        frame.dispose();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.add(panel);
        if (graphicsDevice.isFullScreenSupported()) {
            graphicsDevice.setFullScreenWindow(frame);
        } else {
            // Fallback if full-screen not supported
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        }
    }
    public void setWindowed() {
        fullScreen = false;
        frame.dispose();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                windowedSize = new Dimension(panel.getWidth(), panel.getHeight());
            }
        });
        frame.setUndecorated(false);
        frame.setResizable(true);
        frame.add(panel);
        if (graphicsDevice.isFullScreenSupported()) {
            graphicsDevice.setFullScreenWindow(null);
        }
        
        frame.setExtendedState(JFrame.NORMAL);
        frame.setPreferredSize(windowedSize);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
    }

    @Override
    public void run() {
        //start game (client will start later)
        long lastUpdateTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        int updates = 0;
        
        while (true) {
            long now = System.nanoTime();

            // Update game logic
            if (now - lastUpdateTime >= TIME_BETWEEN_UPDATES) {
                updateGame((now - lastUpdateTime) / TIME_BETWEEN_UPDATES);
                if (client != null && updates % 3 == 0) {client.socketManager.handleSendingPackets();} // called every 3 frames (20 times per second)
                updates++;
                lastUpdateTime += TIME_BETWEEN_UPDATES;
            }

            // Print FPS and UPS every second
            if (System.currentTimeMillis() - timer >= 1000) {
                if (updates < 40) {System.out.println("FPS: " + updates);} // prints if fps drops below 40
                fps = updates;
                updates = 0;
                timer += 1000;
            }
            
        }
    }

    // Methods for starting up the client and server
    public void startServer() {
        server = new Server();
    }
    public static void shutDownServer() {
        if (server != null) {server.shutDown();}
    }
    public void openServerToLan(int port) {
        if (server == null) {
            System.out.println("server is null in Game.openServerToLan()! Printing Stack Trace:");
            EngineCalculator.printStackTrace();
        } else {
            server.openToLan(port);
        }
    }
    public void startClient(String displayName) {
        // Should start the client
        client = new Client(displayName);
    }
    public void startClient(String displayName, InetAddress serverAddress, int serverPort) {
        // Should start the client
        client = new Client(displayName, serverAddress, serverPort);
    }

    // Loaders
    private void loadAllData() {
        //confirm that game_data folder structure is as it is supposed to be and if not, create the correct structure
        fileModifier.init();
        fileModifier.confirmDataFolderStructure();
        
        //load or create keybinds
        fileModifier.loadKeyBindings();
        //setup button handling for all input devices (give each one a JFrame object and this Game object)
        keyboard.setupKeyHandleing(this); // Allow keyboard control
        mouse.setupButtonHandleing(this, frame); // Allow mouse control
        controller.init(); // Initialize Controller (Controller input is handled in the updateGame() method)

        //settings and menus
        fileModifier.loadSettings();
        loadMenus();

        //load images
        loadImages();

        //loads all Menus, GComponents, etc. (Most gui)
        //menuManager.init();

        loadDataProgress += 0.5;
        
        //setCursor(); //  #Warning  
        gameState = GameState.MENUS;
    }
    private void loadMenus() {
        //Fill menuMap with propper Menus that are filled with propper GComponents
        //This is the main place to load/Create GComponents and Menus
        //use geGComponentID for GComponent IDs!
        
        // Vars:
        Point menuCorner = new Point(300, Game.gameResolution.height / 3);
        Point componentOffset = new Point(0, 60);

        MenuManager.assembleMenuOfButtons(
            "start",
            new GButtonTemplate[]{
                new GButtonTemplate("Play", "Play Spirit Of Eldervine in Singleplayer, Multiplayer, or Teams modes", ActivationType.GO_TO_MENU, "play"),
                new GButtonTemplate("Settings", "Change or view all of the changable settings", ActivationType.GO_TO_MENU, "settings"),
                new GButtonTemplate("Key Bindings", "Change or view all of the key bindings", ActivationType.NONE),
                new GButtonTemplate("Extras", "Extra things like links, credits, etc.", ActivationType.GO_TO_MENU, "extras"),
                new GButtonTemplate("Quit", "Close this program and exit to desktop", ActivationType.EXIT)
            },
            menuCorner,
            Direction8.LEFT_CENTER,
            componentOffset,
            GComponentSizes.LARGE,
            Direction8.LEFT_CENTER,
            Direction4.LEFT
        );
        MenuManager.assembleMenuOfButtons(
            "play",
            new GButtonTemplate[]{
                new GButtonTemplate("Start World", "Start a new world to play alone or let others to join", ActivationType.NONE),
                new GButtonTemplate("Open World", "Open a world to play alone or let others to join", ActivationType.NONE),
                new GButtonTemplate("Join World", "Join another player's game", ActivationType.GO_TO_MENU, "join"),
                new GButtonTemplate("Back", "Go back to the previous menu", ActivationType.BACK)
            },
            menuCorner,
            Direction8.LEFT_CENTER,
            componentOffset,
            GComponentSizes.LARGE,
            Direction8.LEFT_CENTER,
            Direction4.LEFT
        );
        MenuManager.assembleMenuOfButtons(
            "extras",
            new GButtonTemplate[]{
                new GButtonTemplate("Credits", "Read about who helped make Spirit Of Eldervine", ActivationType.NONE),
                new GButtonTemplate("Wiki", "Visit the official Spirit Of Eldervine Wiki", ActivationType.NONE),
                new GButtonTemplate("Discord", "Visit the official Spirit Of Eldervine Discord Server", ActivationType.NONE),
                new GButtonTemplate("Tutorials", "Find tutorials for Spirit Of Eldervine", ActivationType.OPEN_LINK, "https://m.youtube.com/results?sp=mAEA&search_query=Spirit+Of+Eldervine+Tutorials"),
                new GButtonTemplate("Holy Bible", "Read the Bible to read between the hidden, in-game verses", ActivationType.OPEN_LINK, "https://www.biblegateway.com/passage/?search=Genesis%201&version=NIV"),
                new GButtonTemplate("Back", "Go back to the previous menu", ActivationType.BACK)
            },
            menuCorner,
            Direction8.LEFT_CENTER,
            componentOffset,
            GComponentSizes.LARGE,
            Direction8.LEFT_CENTER,
            Direction4.LEFT
        );

        Point center = new Point(gameResolution.width / 2, gameResolution.height / 2);
        int distance = 30;

        Menu joinMenu = new Menu("join");
        joinMenu.shadingDirection = Direction4.CENTER;
        GTextField field = new GTextField("Host IP Address", "1234567890.:", 17, true);
        field.setCommons(null, "The IP address of the world host");
        field.setDisplayRect(new Point(center.x, center.y - distance), GComponentSizes.MEDIUM, Direction8.CENTER_BOTTOM);
        field.activationType = ActivationType.TARGET;
        field.displayTextDirection = Direction8.LEFT_CENTER;
        joinMenu.addGComponent(field);
        //Back Button
        GButton button = new GButton();
        button.setCommons("Back", "Go back to the previous menu");
        button.setDisplayRect(new Point(center.x - distance, center.y + distance), GComponentSizes.SMALL, Direction8.RIGHT_TOP);
        button.activationType = ActivationType.BACK;
        button.displayTextDirection = Direction8.CENTER_TOP;
        joinMenu.addGComponent(button);
        // Join Button
        button.setCommons("Join", "Try to join a world with this IP address");
        button.setDisplayRect(new Point(center.x + distance, center.y + distance), GComponentSizes.SMALL, Direction8.LEFT_TOP);
        button.activationType = ActivationType.NONE;
        joinMenu.addGComponent(button);

        menuManager.addMenu(joinMenu);
        /*
        // Start Menu (displays immediatly after loading finishes)
        menu = new Menu("start");
        button.setCommons("Play", "Play the game in Singleplayer, Multiplayer, or Teams modes");
        button.setDisplayRect(new Point(positionX, Game.gameResolution.height / 3), GComponentSizes.MEDIUM, Direction8.LEFT_CENTER);
        button.displayTextDirection = Direction8.LEFT_CENTER;
        button.setControllerSelectionRect(new Rectangle(0, 0, 1, 1));
        button.componentStyle = GComponent.ComponentStyle.SIMPLE;
        button.animationDirection = Direction4.LEFT;
        button.actionValue = "play";
        button.activationType = GComponent.ActivationType.GO_TO_MENU;
        menu.addGComponent(button);

        button.setCommons("Settings", "Change or view all of the changable settings");
        button.changeDisplayRect(new Point(0, spacing));
        button.actionValue = "settings";
        button.activationType = GComponent.ActivationType.GO_TO_MENU;
        menu.addGComponent(button);

        button.setDisplayRect(new Point(positionX, (Game.gameResolution.height / 3) + spacing * 2), GComponentSizes.LARGE, Direction8.LEFT_CENTER);
        button.setCommons("Key Bindings", "Change or view all of the key bindings");
        button.activationType = GComponent.ActivationType.NONE;
        menu.addGComponent(button);
        
        button.setCommons("Quit", "Close this program and exit to desktop");
        button.changeDisplayRect(new Point(0, spacing));
        button.activationType = GComponent.ActivationType.CODE;
        button.actionMethod = () -> {Game.exitGame(0);};
        menu.addGComponent(button);

        menuManager.addMenu(menu);

        // Play Menu
        menu = new Menu("play");

        button.actionValue = null;
        button.activationType = GComponent.ActivationType.NONE;
        button.setCommons("Start World", "Start a new world to play alone or let others to join");
        button.setDisplayRect(new Point(positionX, Game.gameResolution.height / 3), GComponentSizes.MEDIUM, Direction8.LEFT_CENTER);
        menu.addGComponent(button);
        button.setCommons("Open World", "Open a world to play alone or let others to join");
        button.changeDisplayRect(new Point(0, spacing));
        menu.addGComponent(button);
        button.setCommons("Join World", "Join another player's game");
        button.changeDisplayRect(new Point(0, spacing));
        menu.addGComponent(button);

        menuManager.addMenu(menu);
        */
       EngineCalculator.printEnumValues(Settings.DebugEnum.class);
       System.out.println(EngineCalculator.enumToInteger(Settings.DebugEnum.class, DebugEnum.FPS));
       //System.out.println(EngineCalculator.integerToEnum(DebugEnum.FPS));
    }
    private void loadImages() {
        images.add("cursor", fileModifier.loadImage("cursor.png", FileModifier.IMAGE_PATH_GUI));
        images.add("titleVerticalLarge", fileModifier.loadImage("title_vertical_large.png", FileModifier.IMAGE_PATH_GUI));
        images.add("titleVerticalMediumLarge", fileModifier.loadImage("title_vertical_medium_large.png", FileModifier.IMAGE_PATH_GUI));
        images.add("titleVerticalMedium", fileModifier.loadImage("title_vertical_medium.png", FileModifier.IMAGE_PATH_GUI));
        images.add("titleVerticalSmall", fileModifier.loadImage("title_vertical_small.png", FileModifier.IMAGE_PATH_GUI));
        images.add("menuShadingLeft", fileModifier.loadImage("menu_shading_left.png", FileModifier.IMAGE_PATH_GUI));
        images.add("menuShadingCenter", fileModifier.loadImage("menu_shading_center.png", FileModifier.IMAGE_PATH_GUI));
        fileModifier.loadImages(new LinkedHashMap<String, String>(Map.of(
            "menu_shading_left.png", "menuShadingLeft",
            "menu_shading_top.png", "menuShadingTop",
            "menu_shading_right.png", "menuShadingRight",
            "menu_shading_bottom.png", "menuShadingBottom",
            "menu_shading_center.png", "menuShadingCenter"
        )), FileModifier.IMAGE_PATH_GUI);
        fileModifier.loadImages(new LinkedHashMap<String, String>(Map.of(
            "background_1.png", "background1",
            "background_2.png", "background2",
            "background_3.png", "background3",
            "background_4.png", "background4",
            "background_5.png", "background5"
        )), FileModifier.IMAGE_PATH_BACKGROUNDS);
    }
    private void setCursor() {
        Dimension bestSize = Toolkit.getDefaultToolkit().getBestCursorSize(64, 64);
        Image cursorImage = GraphicsCalculator.getImageCopy(images.get("cursor")).getScaledInstance(
            bestSize.width,
            bestSize.height,
        BufferedImage.SCALE_SMOOTH);
        Cursor modernCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(4, 4), "Modern-Cursor");
        frame.setCursor(modernCursor);
    }
    public void handleButtonPress(String button, boolean wasPressed) {
        keyBindings.handleButtonPress(this, button, wasPressed);
    }
    public void handleButtonPress(MouseButton button, boolean wasPressed) {
        keyBindings.handleButtonPress(this, button, wasPressed);
    }
    public void handleButtonPress(ControllerButton button, boolean wasPressed) {
        keyBindings.handleButtonPress(this, button, wasPressed);
    }
    
    private void bindKey(String keyCode) {
        if (keyCode.toLowerCase().equals("escape")) {keyCode = "none";} //assign to nothing if escape was pressed

        //keyBindings.(keyCode, keyboard.bindingAction);   // #InDev  I need to make propper key binding for each input type.
        keyboard.isBindingKey = false;
        keyboard.bindingAction = null;

        //handle buttons, ui, etc.
    }
    public void activateAction(String action, boolean wasPressed) {
        if (wasPressed) {
            // Action was ended/released
            switch (action) {
                case "back":
                    menuManager.goBackPath(null);
                    break;
                case "force_quit":
                    if (keyboard.heldKeys.contains("ctrl")) {Game.exitGame(0);}
                    break;
                case "vibrate_up":
                    controller.startVibration(1.0, 1.0, 250);
                    break;
                case "vibrate_right":
                    controller.startVibration(0.0, 0.5, 100);
                    break;
                case "vibrate_left":
                    controller.startVibration(0.5, 0.0, 100);
                    break;
                case "vibrate_down":
                    controller.startVibration(0.3, 0.3, 50);
                    break;
                case "click":
                    //handle clicking buttons, selecting sliders, etc.
                    switch (controllType) {
                        case KEYBOARD_AND_MOUSE:
                            menuManager.handleMenuMouseClick();
                            break;
                        case CONTROLLER:
                            menuManager.handleMenuControllerClick();
                            break;
                    }
                    break;
                case "attack":
                    System.out.println("Attack!");
                    break;
                case "freeze":
                    isGameFrozen = !isGameFrozen;
                    break;
                case "copy":
                    if (Game.keyboard.heldKeys.contains("ctrl")) {Game.menuManager.textManager.handleKey("copy");}
                    break;
                case "paste":
                    if (Game.keyboard.heldKeys.contains("ctrl")) {Game.menuManager.textManager.handleKey("paste");}
                    break;
                default:
                    System.out.println("Un-handled action \"" + action + "\" in Game.activateAction()");
                    break;
            }
        } else {
            // Action was started/pressed
            switch (action) {
                default:
                    // Nothing needs to be printed when the release of an action is not handled.
                    break;
            }
        }
    }
    
    //all of the data loading for Game.java will be before updateGame()
    public void updateGame(Double delta) {
        if (isGameFrozen) {
            panel.repaint();
            return;
        }
        controller.update(this);
        if (controllType == ControlType.CONTROLLER && controller.shouldChangeControlType()) {controllType = ControlType.KEYBOARD_AND_MOUSE;} // Switch to keyboard and mouse if controller has been disconnected

        boolean updateMenus = true; // makes menuManager not get updated if the gameState is certains values
        switch (gameState) {
            case STARTUP:
                // Start Data Loading On New Thread
                gameState = GameState.LOADING;
                new Thread(() -> {loadAllData();}).start();
                updateMenus = false;
                break;
            case LOADING:
                // Do Nothing (Waiting for data to load)
                updateMenus = false;
                break;
            case MENUS:
                menuBackgroundManager.update();
                break;
            //case NOTIFICATION:
            //    //update notifications
            //    break;
            case PLAY:
                //tell client to update game
                break;
        }
        if (updateMenus) {menuManager.update();}
        panel.repaint();
    }
    //game methods, calculations, etc. will be after updateGame()

    public class GamePanel extends JPanel {

        public GamePanel() {
            setBackground(Color.BLACK); // Set background color
            setFocusable(true); // Make sure key events are received
            requestFocusInWindow();
            
            //debugEnabled.add("fps");
            //setupMouseButtonBindings();
        }
        //Render Game
        @Override
        protected void paintComponent(Graphics gOriginal) {
            super.paintComponent(gOriginal); // Clear background
            
            gOriginal.setColor(Color.WHITE);
            gOriginal.fillRect(0, 0, getWidth(), getHeight());

            //apply scaling and centering:
            GraphicsCalculator.setDisplayScaleAndOffset(new Dimension(getWidth(), getHeight()), gameResolution, gameFitType);
            Graphics2D g = (Graphics2D) gOriginal.create();
            g.translate(displayOffset.x, displayOffset.y);
            g.scale(displayScale.x, displayScale.y);

            switch(gameState) {
                case STARTUP:
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(0, 0, gameResolution.width, gameResolution.height);
                    return;
                case LOADING:
                    renderLoadingScreen(true, g); //(simple, graphics)
                    return; // Should not try to render any of the other things after the switch statement if the game is still loading
                case MENUS:
                    renderMenuBackground(g);
                    //this draws the game title in front of the backgrounds
                    g.drawImage(Game.images.get("titleVerticalMediumLarge"), Game.gameResolution.width / 3, Game.gameResolution.height / 8, null);
                    break;
                case PLAY:
                    //tell client to render game...
                    client.render(g);
                    // does not catch the client being null because if it is null and gameState is on PLAY there are BIG PROBLEMS and all errors are welcome!
                    
                    break;
                //case NOTIFICATION:
                //    //not sure yet...
                //    break;
            }
            menuManager.render(g);
            //renderRings(g);
            
            renderDebug(g);
        }
        private void renderMenuBackground(Graphics g) {
            //gameResolution
            menuBackgroundManager.render(gameResolution, g);
        }
        private void renderLoadingScreen(boolean simple, Graphics2D g) {
            //the boolean "simple" was for early development before I was ready to bug test the loading bar
            Dimension loadingBarSize = new Dimension(300, 30);
            Point barCorner = new Point((getWidth() - loadingBarSize.width) / 2 , (getHeight() - loadingBarSize.height) / 2);
            int margin = 20;
            int smallMargin = 5;
            int cornerSize = 15;
            Font textFont = new Font(null, Font.BOLD, 20);

            if (simple) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.BLACK);
                g.setFont(textFont);
                g.drawString("Loading " + elementBeingLoaded + "..." + loadDataProgress, (getWidth() - loadingBarSize.width) / 2, (getHeight() - loadingBarSize.height) / 2 - margin);
                int colorNum = EngineCalculator.randomRange(0, 255);
                g.setColor(new Color(colorNum, colorNum, colorNum));
                g.fillRect(50, 50, 100, 100);
            } else { 
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setFont(textFont);
                g.setColor(Color.BLACK);
                //draw text
                g.drawString("Loading " + elementBeingLoaded + "...", (getWidth() - loadingBarSize.width) / 2, (getHeight() - loadingBarSize.height) / 2 - margin);
                //draw bar
                //create images
                BufferedImage barBackground = new BufferedImage(loadingBarSize.width, loadingBarSize.height, BufferedImage.TYPE_INT_ARGB);
                BufferedImage barForeground = new BufferedImage(loadingBarSize.width, loadingBarSize.height, BufferedImage.TYPE_INT_ARGB);
                //create graphics
                Graphics2D backgroundG = (Graphics2D) barBackground.createGraphics();
                Graphics2D foregroundG = (Graphics2D) barForeground.createGraphics();
                //set alpha composites
                backgroundG.setComposite(AlphaComposite.Clear);
                foregroundG.setComposite(AlphaComposite.Clear);
                //set colors
                backgroundG.setColor(Color.BLACK);
                foregroundG.setColor(Color.GREEN);
                //draw onto background image
                backgroundG.fillRoundRect(0, 0, loadingBarSize.width, loadingBarSize.height, cornerSize, cornerSize);
                backgroundG.setColor(new Color(0, 0, 0, 0));
                backgroundG.fillRoundRect(-smallMargin * 2, -smallMargin * 2, loadingBarSize.width - smallMargin * 2, loadingBarSize.height - smallMargin * 2, cornerSize, cornerSize);
                //draw onto foreground image
                foregroundG.fillRoundRect(-smallMargin * 2, -smallMargin * 2, loadingBarSize.width - smallMargin * 2, loadingBarSize.height - smallMargin * 2, cornerSize, cornerSize);
                foregroundG.setColor(new Color(0, 0, 0, 0));
                foregroundG.fillRect((int)(barCorner.x + loadingBarSize.width * (1 - loadDataProgress)), barCorner.y, loadingBarSize.width, loadingBarSize.height);
                g.drawImage(barBackground, barCorner.x, barCorner.y, loadingBarSize.width, loadingBarSize.height, null);
                g.drawImage(barForeground, barCorner.x, barCorner.y, loadingBarSize.width, loadingBarSize.height, null);
            }
        }
        private void renderDebug(Graphics2D g) {
            if (settings.isDebugEnabled == false) {return;}
            
            ArrayList<String> debugStrings = new ArrayList<>();
            String header = "<header>";
            // This setup will controll debug rendering order.
            for (Settings.DebugEnum value : Settings.DebugEnum.values()) {
                if (settings.getDebugList().contains(value) == false) {continue;}
                switch(value) {
                    case GCOMPONENT_BACKGROUNDS:
                        menuManager.renderDebug(g);
                        break;
                    case CONTROLLER_INFO:
                        RenderEngine.renderControllerInfo(new Color(30, 30, 30), controller, g);
                        break;
                    case CURSOR_INFO:
                        RenderEngine.renderCursorDebugInfo(g);
                        break;
                    case MOUSE_INFO:
                        debugStrings.add(header + "Mouse Info");
                        debugStrings.add("Held Buttons: " + mouse.heldButtons);
                        break;
                    case KEYBOARD_INFO:
                        debugStrings.add(header + "Keyboard Info");
                        debugStrings.add("Held Keys: " + keyboard.heldKeys);
                        break;
                    case WINDOW_RINGS:
                        RenderEngine.renderRings(g);
                    case FPS:
                        debugStrings.add(header + "FPS");
                        debugStrings.add("FPS: " + fps);
                        break;
                    case WHITE_OUT:
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, gameResolution.width, gameResolution.height);
                        break;
                    case MENU_INFO:
                        debugStrings.add(header + "Menu Info");
                        debugStrings.add("Menu Path: " + menuManager.getMenuPath());
                        debugStrings.add("Controller Selection Point: " + EngineCalculator.toString(controller.getSelectionPoint()));
                        debugStrings.add("Controller Selection Rect: " + EngineCalculator.toString(menuManager.getMenu(menuManager.getActiveMenuName()).controllerSelectionRectsBounds));
                        debugStrings.add("Menu Shading Direction: " + menuManager.getMenu(menuManager.getActiveMenuName()).shadingDirection.toString());
                        debugStrings.add("Menu Selection ID: " + EngineCalculator.toString(menuManager.getSelectionID()));
                        break;
                    case MENU_ANIMATION_INFO:
                        EngineCalculator.addArrayListToArrayList(debugStrings, menuManager.getListOfDebugValues(header));
                        break;
                    case TYPING:
                        debugStrings.add(header + "Typing");
                        debugStrings.add("Targeted Text ID: " + menuManager.textManager.getTargetID());
                        debugStrings.add("Typing Cursor Position: " + menuManager.textManager.getCursorPosition());
                        debugStrings.add("Typing Selection Position: " + EngineCalculator.toString(menuManager.textManager.getSelectionPositions()));
                        break;
                    case TYPING_TEST:
                        debugStrings.add(header + "Typing Test");
                        debugStrings.add("Word: " + word);
                        debugStrings.add("Before: " + Game.menuManager.textManager.getTextBeforeSelection());
                        debugStrings.add("In: " + Game.menuManager.textManager.getTextInSelection());
                        debugStrings.add("After: " + Game.menuManager.textManager.getTextAfterSelection());
                        break;
                }
            }
            RenderEngine.renderTextDebugInfo(debugStrings, header, 0, g);
        }
    }
}
