package com.shockWave;

import com.shockWave.client.Client;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.graphics.GraphicsCalculator;
import com.shockWave.graphics.Menu;
import com.shockWave.graphics.MenuBackgroundManager;
import com.shockWave.graphics.MenuManager;
import com.shockWave.graphics.RenderEngine;
import com.shockWave.graphics.g_components.GButton;
import com.shockWave.graphics.g_components.GButtonTemplate;
import com.shockWave.graphics.g_components.GComponent;
import com.shockWave.graphics.g_components.GTextField;
import com.shockWave.graphics.g_components.GComponent.ActivationType;
import com.shockWave.graphics.g_components.GComponent.GComponentSizes;
import com.shockWave.input.ConsoleController;
import com.shockWave.input.InputPlaceHolder;
import com.shockWave.input.Keyboard;
import com.shockWave.input.Mouse;
import com.shockWave.libraries.ImageLibrary;
import com.shockWave.notification.Notification;
import com.shockWave.notification.NotificationManager;
import com.shockWave.savers.KeyBindings;
import com.shockWave.savers.Settings;
import com.shockWave.savers.Settings.DebugEnum;
import com.shockWave.server.Server;

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
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

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
    public static Dimension windowedSize = new Dimension(gameResolution.width / 2, gameResolution.height / 2);
    public static boolean fullScreen = false;

    //game loop vars
    private int fps = 0;
    private int pgsps = 0;
    public static final int TARGET_UPS = 60; // Updates Per Second
    public static final int TARGET_PGSPS = 20; // Packet Groups Sent Per Second
    private final double TIME_BETWEEN_UPDATES = 1000000000 / TARGET_UPS;
    private final double TIME_BETWEEN_SENDING_PACKETS = 1000000000 / TARGET_PGSPS;
    public static volatile boolean shouldSendPackets = false; // This tells a separate thread when to send packets, don't know if/how well it works...
    
    public String elementBeingLoaded = "Nothing";
    public Double loadDataProgress = 0.0;

    //loaders
    public static FileModifier fileModifier = new FileModifier();

    //server-client communications ??(don't know why this is here...)

    //client and server...obviously
    public static Client client = null; //this will handle the in-game stuff like moving.
    public static Server server = null; //this will handle hosting the game on LAN for others to join.

    public static boolean isGameFrozen = false; // This can freeze the game and is toggled by the action "freeze"

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
    public static enum Direction5 {RIGHT, BOTTOM, LEFT, TOP, CENTER} // The Word is where the direction is going toward (from Opposite_of_direction to direction) These will not be used for entity movement! They are for menu animations only!
    public static enum Direction4 {EAST, SOUTH, WEST, NORTH} // This is what will be used for entity direction. The enum/word that is selected is what direction the entity is facing
    public static enum Direction8 {CENTER_CENTER, RIGHT_CENTER, RIGHT_BOTTOM, CENTER_BOTTOM, LEFT_BOTTOM, LEFT_CENTER, LEFT_TOP, CENTER_TOP, RIGHT_TOP} //These are for GComponent placements
    public static enum ControlType {KEYBOARD_AND_MOUSE, CONTROLLER}

    //game state stuff
    public static HostOrGuest hostOrGuest = HostOrGuest.MENUS;
    public static GameState gameState = GameState.STARTUP;
    public static ControlType controllType = ControlType.KEYBOARD_AND_MOUSE; // currently not really used
    
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
    public static NotificationManager notificationManager = new NotificationManager();

    //Input
    public static KeyBindings keyBindings = new KeyBindings(); // Key Bindings
    public static Keyboard keyboard = new Keyboard(); // Keyboard
    public static Mouse mouse = new Mouse(); // Mouse
    public static ConsoleController controller = new ConsoleController(); // Controller
    public static LinkedBlockingQueue<InputPlaceHolder> inputQueue = new LinkedBlockingQueue<>();
    public static String word = ""; // Test for typing


    public void start() {
        frame = new JFrame("Shock Wave");

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
        long lastPacketGroupSendTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        int updates = 0;
        int packetGroupsSent = 0;
        
        while (true) {
            long now = System.nanoTime();

            // Update game logic
            if (now - lastUpdateTime >= TIME_BETWEEN_UPDATES) {
                updateGame((now - lastUpdateTime) / TIME_BETWEEN_UPDATES);
                updates++;
                lastUpdateTime += TIME_BETWEEN_UPDATES;
            }

            // Having a separate tracker for sending packets makes sure that if the game loop is running very slow then sending packets doesn't necessarily have to be slow too.
            // Tell separate thread to send packets
            if (now - lastPacketGroupSendTime >= TIME_BETWEEN_SENDING_PACKETS) {
                if (client != null) {shouldSendPackets = true;}
                packetGroupsSent++;
                lastPacketGroupSendTime += TIME_BETWEEN_SENDING_PACKETS;
            }

            // Print FPS and UPS every second
            if (System.currentTimeMillis() - timer >= 1000) {
                if (updates < 40) {System.out.println("FPS: " + updates);} // prints if fps drops below 40
                if (packetGroupsSent < 2) {System.out.println("PGSPS: " + packetGroupsSent);} // prints if pgsps (packet groups sent per second) drops below 15
                fps = updates;
                pgsps = packetGroupsSent;
                updates = 0;
                packetGroupsSent = 0;
                timer += 1000;
            }
            
        }
    }

    // Methods for starting up the client and server
    public void startServer(int port) {
        Game.server = new Server(port);
        if (client != null) {gameState = GameState.PLAY;}
        String message = "Connect locally with \"" + server.socketManager.getLocalAddress() + "\" or globaly with \"" + server.socketManager.getGlobalAddress() + "\"";
        notificationManager.addNotification(new Notification("Successfully Started Server", message, "success", 30.0));
    }
    public static void shutDownServer() {
        if (server != null) {server.shutDown();}
        gameState = GameState.MENUS;
    }
    public void startClient(String displayName) {
        // Should start the client
        client = new Client(displayName);
        if (server != null) {gameState = GameState.PLAY;}
    }
    public void startClient(String displayName, InetAddress serverAddress, int serverPort) {
        // Should start the client
        client = new Client(displayName, serverAddress, serverPort);
        if (client.socketManager.isConnected) {
            gameState = GameState.PLAY;
            notificationManager.addNotification(new Notification("Successfully Connected To Server", "Server accepted connection", "success", 5.0));
        } else {
            notificationManager.addNotification(new Notification("Failed To Connect To Server", "Connecting to the server was rejected by the server", "error", 5.0));
            client = null;
        }
        
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
                new GButtonTemplate("Host Server", "Host a Shock Wave server for others to join", ActivationType.GO_TO_MENU, "host"),
                new GButtonTemplate("Join Server", "Join a Shock Wave server that someone else is hosting", ActivationType.GO_TO_MENU, "join"),
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
            Direction5.LEFT
        );
        MenuManager.assembleMenuOfButtons(
            "extras",
            new GButtonTemplate[]{
                new GButtonTemplate("Credits", "Read about who helped make Shock Wave", ActivationType.NONE),
                new GButtonTemplate("Wiki", "Visit the official Shock Wave Wiki", ActivationType.NONE),
                new GButtonTemplate("Discord", "Visit the official Shock Wave Discord Server", ActivationType.NONE),
                new GButtonTemplate("Tutorials", "Find tutorials for Shock Wave", ActivationType.OPEN_LINK, "https://m.youtube.com/results?sp=mAEA&search_query=Spirit+Of+Eldervine+Tutorials"),
                new GButtonTemplate("Holy Bible", "Read the Bible to read between the hidden, in-game verses", ActivationType.OPEN_LINK, "https://www.biblegateway.com/passage/?search=Genesis%201&version=NIV"),
                new GButtonTemplate("Back", "Go back to the previous menu", ActivationType.BACK)
            },
            menuCorner,
            Direction8.LEFT_CENTER,
            componentOffset,
            GComponentSizes.LARGE,
            Direction8.LEFT_CENTER,
            Direction5.LEFT
        );
        if (true) { // Join Menu
            Point center = new Point(gameResolution.width / 2, gameResolution.height / 2);
            int distance = 30;
            int fieldHeight = GComponentSizes.MEDIUM.textFieldSize.height;

            Menu joinMenu = new Menu("join");
            joinMenu.shadingDirection = Direction5.CENTER;
            GTextField field = new GTextField("Host IP Address", "1234567890.:", 17, true);
            field.setCommons(null, "The IP address of the world host");
            field.setDisplayRect(new Point(center.x, center.y - distance * 2 - fieldHeight), GComponentSizes.MEDIUM, Direction8.CENTER_BOTTOM);
            field.activationType = ActivationType.TARGET;
            field.displayTextDirection = Direction8.LEFT_CENTER;
            int connectionFieldID = joinMenu.addGComponent(field);
            
            field = new GTextField("Username/Gamer Tag", "", 30, true);
            field.setCommons(null, "Your player's name that will be displayed over their head");
            field.setDisplayRect(new Point(center.x, center.y - distance), GComponentSizes.MEDIUM, Direction8.CENTER_BOTTOM);
            field.activationType = ActivationType.TARGET;
            field.displayTextDirection = Direction8.LEFT_CENTER;
            int nameFieldID = joinMenu.addGComponent(field);
            //Back Button
            GButton button = new GButton();
            button.setCommons("Back", "Go back to the previous menu");
            button.setDisplayRect(new Point(center.x - distance, center.y + distance), GComponentSizes.SMALL, Direction8.RIGHT_TOP);
            button.activationType = ActivationType.BACK;
            button.displayTextDirection = Direction8.CENTER_TOP;
            joinMenu.addGComponent(button);
            // Join Button
            button = new GButton(){
                @Override
                public void activateComponent() {
                    String connectionText = null;
                    String nameText = null;

                    GComponent gcomp = menuManager.getGComponentByID(connectionFieldID, true);
                    if (gcomp instanceof GTextField tf) {connectionText = tf.text;}
                    gcomp = menuManager.getGComponentByID(nameFieldID, true);
                    if (gcomp instanceof GTextField tf) {nameText = tf.text;}

                    InetAddress address = null;
                    Integer port = null;
                    boolean isValid = true;

                    System.out.println("Extra Print: Text: " + connectionText + ("1:2").contains(":") + ", " + connectionText.contains(":") + ", " + connectionText.split(":").length);

                    if (connectionText.contains(":") && connectionText.split(":").length == 2) {
                        String[] splitText = connectionText.split(":");
                        
                        if (splitText[0].isBlank() || splitText[1].isBlank()) {
                            System.out.println("Error 1");
                            isValid = false;
                            return;
                        }

                        try {
                            address = InetAddress.getByName(splitText[0]);
                        } catch(IOException e) {
                            System.out.println("Error 2");
                            isValid = false;
                        }
                        try {
                            port = Integer.valueOf(splitText[1]);
                        } catch(NumberFormatException e) {
                            System.out.println("Error 3");
                            isValid = false;
                        }
                    } else {
                        System.out.println("Error 4");
                        isValid = false;
                    }
                    if (isValid) {
                        System.out.println("Starting Client from GButton! Address: " + address + ", Port: " + port);
                        startClient(nameText, address, port);
                    } else {
                        notificationManager.addNotification(new Notification("Failed To Join Server", "Failed to join server with address and port " + address + ":" + port, "error", 3.0));
                    }
                }
            };
            button.setCommons("Join", "Try to join a world with this IP address");
            button.setDisplayRect(new Point(center.x + distance, center.y + distance), GComponentSizes.SMALL, Direction8.LEFT_TOP);
            button.activationType = ActivationType.CODE;
            joinMenu.addGComponent(button);

            menuManager.addMenu(joinMenu);
        }
        if (true) { // Host Menu
            Point center = new Point(gameResolution.width / 2, gameResolution.height / 2);
            int distance = 30;
            int fieldHeight = GComponentSizes.MEDIUM.textFieldSize.height;

            Menu hostMenu = new Menu("host");
            hostMenu.shadingDirection = Direction5.CENTER;
            GTextField field = new GTextField("Port Number", "1234567890", 5, true);
            field.setCommons(null, "The port of the server that will be hosted");
            field.setDisplayRect(new Point(center.x, center.y - distance * 2 - fieldHeight), GComponentSizes.MEDIUM, Direction8.CENTER_BOTTOM);
            field.activationType = ActivationType.TARGET;
            field.displayTextDirection = Direction8.LEFT_CENTER;
            int connectionFieldID = hostMenu.addGComponent(field);
            
            field = new GTextField("Username/Gamer Tag", "", 30, true);
            field.setCommons(null, "Your player's name that will be displayed over their head");
            field.setDisplayRect(new Point(center.x, center.y - distance), GComponentSizes.MEDIUM, Direction8.CENTER_BOTTOM);
            field.activationType = ActivationType.TARGET;
            field.displayTextDirection = Direction8.LEFT_CENTER;
            int nameFieldID = hostMenu.addGComponent(field);

            //Back Button
            GButton button = new GButton();
            button.setCommons("Back", "Go back to the previous menu");
            button.setDisplayRect(new Point(center.x - distance, center.y + distance), GComponentSizes.SMALL, Direction8.RIGHT_TOP);
            button.activationType = ActivationType.BACK;
            button.displayTextDirection = Direction8.CENTER_TOP;
            hostMenu.addGComponent(button);
            // Start Button
            button = new GButton(){
                @Override
                public void activateComponent() {
                    String connectionText = null;
                    String nameText = null;

                    GComponent gcomp = menuManager.getGComponentByID(connectionFieldID, true);
                    if (gcomp instanceof GTextField tf) {connectionText = tf.text;}
                    gcomp = menuManager.getGComponentByID(nameFieldID, true);
                    if (gcomp instanceof GTextField tf) {nameText = tf.text;}

                    Integer port = null;

                    try {
                        port = Integer.valueOf(connectionText);
                        startServer(port);
                        startClient(nameText);
                        System.out.println("Started Server!!");
                    } catch (NumberFormatException e) {
                        System.out.println("Error 5");
                    }
                }
            };
            button.setCommons("Start", "Start hosting the server so others can join");
            button.setDisplayRect(new Point(center.x + distance, center.y + distance), GComponentSizes.SMALL, Direction8.LEFT_TOP);
            button.activationType = ActivationType.NONE;
            hostMenu.addGComponent(button);

            menuManager.addMenu(hostMenu);
        }
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
        fileModifier.loadImages(new LinkedHashMap<String, String>(Map.of(
            "error.png", "errorIcon",
            "success.png", "successIcon",
            "client.png", "clientIcon",
            "server.png", "serverIcon"
        )), FileModifier.IMAGE_PATH_ICONS);
        
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
    public void handleAction(String action, boolean wasPressed) {
        inputQueue.add(new InputPlaceHolder(action, wasPressed));
    }
    private void handleInputs() {
        ArrayList<InputPlaceHolder> inputs = new ArrayList<>();
        inputQueue.drainTo(inputs);
        
        for (InputPlaceHolder input : inputs) {
            activateAction(input.action, input.wasPressed);
        }
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
                    //System.out.println("Attack!");
                    break;
                case "freeze":
                    isGameFrozen = !isGameFrozen;
                    break;
                case "notification":
                    notificationManager.addNotification(new Notification("Test Notification " + EngineCalculator.randomRange(0, 10), "Randomly generated numbers: " + EngineCalculator.randomRange(0, 50), "successIcon", 5.0));
                    break;
                case "copy":
                    if (Game.keyboard.heldKeys.contains("ctrl")) {Game.menuManager.textManager.handleKey("copy");}
                    break;
                case "paste":
                    if (Game.keyboard.heldKeys.contains("ctrl")) {Game.menuManager.textManager.handleKey("paste");}
                    break;
                default:
                    if (client != null) {
                        client.handleInput(action, wasPressed);
                    }
                    //System.out.println("Un-handled action \"" + action + "\" in Game.activateAction()");
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
    public void updateInputs(double delta) {
        ArrayList<String> actionsHeld = new ArrayList<>(keyBindings.actionsHeld); // Prevents the game throwing a ConcurrentModificationException
        for (String action : actionsHeld) {
            switch (action) {
                default:
                    if (client != null) {client.updateInput(action, delta);}
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

        handleInputs();
        updateInputs(delta);
        
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
                if (client != null) {
                    client.updateClient();
                } else {
                    System.out.println("Could not update client because client was null. Not throwing exception but continuing with thread:");
                }
                break;
        }
        if (updateMenus) {menuManager.update();}
        notificationManager.update();
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
            notificationManager.render(g);
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
