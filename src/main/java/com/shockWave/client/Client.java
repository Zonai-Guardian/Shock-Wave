package com.shockWave.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import com.shockWave.Game;
import com.shockWave.Game.Direction4;
import com.shockWave.Game.Direction5;
import com.shockWave.Game.Direction8;
import com.shockWave.client.players.CPlayer;
import com.shockWave.client.players.CPlayerManager;
import com.shockWave.client.players.CSimplePlayer;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.graphics.Menu;
import com.shockWave.graphics.MenuManager;
import com.shockWave.graphics.RenderEngine;
import com.shockWave.graphics.g_components.GButton;
import com.shockWave.graphics.g_components.GButtonTemplate;
import com.shockWave.graphics.g_components.GComponent.ActivationType;
import com.shockWave.graphics.g_components.GComponent.GComponentSizes;
import com.shockWave.libraries.ConstantLibrary;
import com.shockWave.networking.Packet;
import com.shockWave.networking.PacketManager.PacketPurpose;
import com.shockWave.notification.Notification;

public class Client {
    public ClientSocketManager socketManager;
    public volatile long lastPacketReceiveTime = 0;
    private int packetGenerationTick = 0;

    // enums
    public static enum ClientState {STARTUP, LOADING, CONNECTING, RUNNING, LOST_CONNECTION /*not intentional*/, DISCONNECTING  /*is intentional*/, KICKED, SHUTTING_DOWN} // I don't know if I'll actually use all of these...
    
    //all variables that can/should be deleted when the player exits the world
    public ClientState clientState = ClientState.STARTUP;
    
    public String playerDisplayName;
    public CPlayer player = null; // This should be filled while ClientState is CONNECTING
    public CPlayerManager playerManager = new CPlayerManager();

    public Client(String displayName, InetAddress serverAddress, int serverPort) { // serverPort does not accept null
        socketManager = new ClientSocketManager(serverAddress, serverPort);
        playerDisplayName = displayName;
        System.out.println("Successfully started Client with displayName: " + displayName);
    }
    public Client(String displayName) {
        socketManager = new ClientSocketManager();
        playerDisplayName = displayName;
        System.out.println("Successfully started Client with displayName: " + displayName);
    }

    public void shutDownClientGently() {
        clientState = ClientState.SHUTTING_DOWN;
        socketManager.shouldShutDown = true;
        socketManager.shutDown();
    }

    public void loadAllData() {
        //load all required data to run the client
        loadMenus();

        socketManager.packetsToGenerate.add(PacketPurpose.REGISTER_PLAYER_TO_SERVER_S1);
        clientState = ClientState.CONNECTING;
    }
    private void loadMenus() {
        if (true) {
            Menu menu = new Menu("c_play"){
                @Override
                public boolean handleGoingBackPack() {
                    Game.menuManager.setPath("c_pause", Direction5.RIGHT);
                    return true;
                }
            };
            Game.menuManager.addMenu(menu);
        }
        
        if (true) {
            Menu menu = MenuManager.getAssembledMenuOfButtons(
                new Menu("c_pause"){
                    @Override
                    public boolean handleGoingBackPack() {
                        Game.menuManager.setPath("c_play", Direction5.LEFT);
                        return true;
                    }
                },
                new GButtonTemplate[]{
                    new GButtonTemplate("Quit Game", "Stop the game and exit to main menu", ActivationType.CODE, null, () -> {
                        if (Game.server != null) {
                            Game.shutDownServer();
                        }
                        Game.shutDownClientGently();
                    }, null),
                    new GButtonTemplate("", "", ActivationType.NONE),
                    new GButtonTemplate("Settings", "Change or view all of the changable settings", ActivationType.GO_TO_MENU, "s_general"),
                    new GButtonTemplate("", "", ActivationType.NONE),
                    new GButtonTemplate("Resume Game", "Continue playing the game", ActivationType.CODE, null, () -> {Game.menuManager.addToPath("c_play", Direction5.LEFT);}, null)
                },
                ConstantLibrary.GUI.MENU_CORNER,
                Direction8.LEFT_CENTER,
            new Point(0, ConstantLibrary.GUI.MENU_COMPONENT_OFFSET.y),
                GComponentSizes.LARGE,
                Direction8.LEFT_CENTER,
                Direction5.LEFT
            );
            
            menu.shadingDirection = Direction5.LEFT;
            menu.shouldRenderShading = true; // Overrides shading being off my default
            Game.menuManager.addMenu(menu);
        }
        // make that take a menu that has already been created...and modified...
        
    }
    public void handleInput(String action, boolean wasPressed) { // This method handles actions that need things done when they get pressed or released
        //handle what to do when buttons are pressed and actions are activated
        if (wasPressed) { // Handle Presses
            switch (action) {
                
            }
        } else { // Handle Releases
            switch (action) {

            }
        }
    }
    public void updateInput(String action, double delta) { // This method handles actions that need things done as they are being held down
        switch (action) {
            case "move_south":
                if (player != null) {
                    if (Game.keyBindings.actionsHeld.contains("move_north") == false) {
                        player.testMove(Direction4.SOUTH, delta);
                        // Both east and west are held down, or neither are held down
                        if (((Game.keyBindings.actionsHeld.contains("move_east") == false) && (Game.keyBindings.actionsHeld.contains("move_west") == false)) || (Game.keyBindings.actionsHeld.contains("move_east") && Game.keyBindings.actionsHeld.contains("move_west"))) {
                            player.direction = Direction4.SOUTH;
                        }
                    }
                }
                break;
            case "move_west":
                if (player != null) {
                    if (Game.keyBindings.actionsHeld.contains("move_east") == false) {
                        player.testMove(Direction4.WEST, delta);
                        player.direction = Direction4.WEST;
                    }
                }
                break;
            case "move_north":
                if (player != null) {
                    if (Game.keyBindings.actionsHeld.contains("move_south") == false) {
                        player.testMove(Direction4.NORTH, delta);
                        // Both east and west are held down, or neither are held down
                        if (((Game.keyBindings.actionsHeld.contains("move_east") == false) && (Game.keyBindings.actionsHeld.contains("move_west") == false)) || (Game.keyBindings.actionsHeld.contains("move_east") && Game.keyBindings.actionsHeld.contains("move_west"))) {
                            player.direction = Direction4.NORTH;
                        }
                    }
                }
                break;
            case "move_east":
                if (player != null) {
                    if (Game.keyBindings.actionsHeld.contains("move_west") == false) {
                        player.testMove(Direction4.EAST, delta);
                        player.direction = Direction4.EAST;
                    }
                }
                break;
        }
    }
    public void updateClient() {
        handleReceivedPackets();
        
        // update game logic simulation
        switch (clientState) {
            case STARTUP:
                // It just needs to change ClientState to Loading
                clientState = ClientState.LOADING;
                new Thread(() -> {
                    loadAllData();
                    Game.menuManager.addToPath("c_play", null);
                }).start(); // Start loading data on new Thread
                break;
            case LOADING:
                // Do nothing as the client's assets are being loaded...
                break;
            case CONNECTING:
                // Do nothing...
                break;
            case RUNNING:
                if (lastPacketReceiveTime != 0 && (System.currentTimeMillis() - lastPacketReceiveTime) / 1000 > 5) {
                    lastPacketReceiveTime = 0;
                    socketManager.packetsToGenerate.add(PacketPurpose.DISCONNECT);
                    clientState = ClientState.LOST_CONNECTION;
                    break;
                }

                if (player != null) {player.update();}
                playerManager.interpolatePlayerMovement();

                break;
            case LOST_CONNECTION:
                Game.shutDownClientGently();
                Game.notificationManager.addNotification(new Notification("Lost Connection", "Lost connection with the server! Try connecting again?", "errorIcon", 5.0));
                break;
            case DISCONNECTING:
                Game.shutDownClientGently();
                Game.notificationManager.addNotification(new Notification("Exited Game", "Intentionally disconnected from server", "clientIcon", 5.0));
                break;
            case KICKED:
                Game.shutDownClientGently();
                Game.notificationManager.addNotification(new Notification("Kicked From Game", "Server kicked you from the game", "errorIcon", 5.0));
                break;
            case SHUTTING_DOWN: // Waiting 
                break;
        }
        
        if (Game.shouldSendPackets) {
            Game.shouldSendPackets = false;
            generatePackets();
            socketManager.sendPackets();
        }
    }

    private void handleReceivedPackets() {
        ArrayList<Packet> packetList = socketManager.getReceivedPackets(); // This will automatically get the device packets if the server is on this device.

        for (Packet packet : packetList) {
            Game.client.lastPacketReceiveTime = System.currentTimeMillis();
            
            //System.out.println("    Receiving packet in Client with purpose: " +  EngineCalculator.shortToPacketPurpose(packet.packetPurpose));
            
            switch (EngineCalculator.shortToPacketPurpose(packet.packetPurpose)) {
                case DISCONNECT:
                    clientState = ClientState.KICKED;
                    break;
                case REGISTER_PLAYER_TO_SERVER_S2:
                    System.out.println("    Starting Register 2!!");
                    // bookmark
                    // Create new player-type (??) object in the server
                    player = new CPlayer(
                        playerDisplayName,
                        packet.readData().shortVar, //short id
                        packet.readData().doubleVar.floatValue(), // double colorFloat
                        packet.readData().integerVar, // int posX
                        packet.readData().integerVar // int posY
                    );
                    short idOrStop = 0;
                    while ((idOrStop = packet.readData().shortVar) != -2) {
                        playerManager.registerPlayer(new CSimplePlayer(
                            idOrStop,
                            packet.readData().stringVar,
                            packet.readData().doubleVar.floatValue(),
                            packet.readData().integerVar,
                            packet.readData().integerVar,
                            EngineCalculator.byteToDirection4(packet.readData().byteVar)
                        ));
                    }
                    clientState = ClientState.RUNNING;
                    break;
                case UPDATE_PLAYER_TRANSFORM: // This will only be used if the server needs to move the client like if it should be teleported or something
                    player.posX = packet.readData().integerVar;
                    player.posY = packet.readData().integerVar;
                    player.velocityX = packet.readData().doubleVar;
                    player.velocityY = packet.readData().doubleVar;
                    player.direction = EngineCalculator.byteToDirection4(packet.readData().byteVar);
                    break;
                case UPDATE_PLAYERS_TRANSFORM:
                    //System.out.println(packet);

                    short id = 0;
                    while((id = packet.readData().shortVar) != -2) {
                        if (playerManager.containsPlayer(id)) {
                            CSimplePlayer sp = playerManager.getPlayer(id);
                            sp.posX = packet.readData().integerVar;
                            sp.posY = packet.readData().integerVar;
                            sp.velocityX = packet.readData().integerVar;
                            sp.velocityY = packet.readData().integerVar;
                            sp.direction = EngineCalculator.byteToDirection4(packet.readData().byteVar);
                            System.out.println("Received player data in Client! pos: " + sp.posX + ", " + sp.posY + ", id: " + id);
                        } else {
                            if (player != null && player.id != id) {System.out.println("Player data was sent but the Client has not registered a player with id " + id + " yet!");}
                            // Skip this player because it has not been registered or it is this client's player
                            packet.readData();
                            packet.readData();
                            packet.readData();
                            packet.readData();
                            packet.readData();
                            if (player != null && player.id != id) {System.out.println("Skipped player with id " + id + " in Client.handleReceivedPackets()! Skipping and continueing as normal...");}
                        }
                    }
                    break;
                case REGISTER_NEW_PLAYER_TO_CLIENT:
                    // String name, Short id, Int posX, Int posY, Byte Direction4
                    short id2 = packet.readData().shortVar;
                    if ((player.id == id2) == false && playerManager.containsPlayer(id2) == false) {
                        playerManager.registerPlayer(new CSimplePlayer(
                            id2, //short id
                            packet.readData().stringVar, // string name
                            packet.readData().doubleVar.floatValue(), // float colorFloat
                            packet.readData().integerVar, // int posX
                            packet.readData().integerVar, // int posY
                            EngineCalculator.byteToDirection4(packet.readData().byteVar)
                        ));
                    } else {
                        // Skip registering because player already exists in this client
                        packet.readData();
                        packet.readData();
                        packet.readData();
                        packet.readData();
                    }
                    break;
                case DELETE_PLAYER_IN_CLIENT:
                    playerManager.removePlayer(packet.readData().shortVar);
                    break;
            }
        }
    }
    private void generatePackets() {
        if (clientState == ClientState.RUNNING) {
            packetGenerationTick++;
            if (packetGenerationTick > 1) {
                packetGenerationTick = 0;
                socketManager.addPurposeToGenerate(PacketPurpose.UPDATE_PLAYER_TRANSFORM);
            }
        }

        ArrayList<PacketPurpose> purposes = new ArrayList<>();
        socketManager.packetsToGenerate.drainTo(purposes);

        for (PacketPurpose purpose : purposes) {
            //System.out.println("    Generating packet in Client with purpose: " + purpose);

            Packet packet = new Packet(EngineCalculator.enumToShort(PacketPurpose.class, purpose), -1);
            switch (purpose) {
                case REGISTER_PLAYER_TO_SERVER_S1:
                    // string displayName
                    packet.writeData(playerDisplayName);
                    System.out.println("Success 1!");
                    break;
                case UPDATE_PLAYER_TRANSFORM:
                    packet.writeData((int)player.posX);
                    packet.writeData((int)player.posY);
                    packet.writeData(player.velocityX);
                    packet.writeData(player.velocityY);
                    packet.writeData(EngineCalculator.enumToByte(Direction4.class, player.direction));
                    break;
            }
            socketManager.packetsToSend.add(packet);
        }
    }

    //Rendering
    public void render(Graphics2D g) {
        //render everything the client should
        //g.fillRect(0, 0, Game.gameResolution.width, Game.gameResolution.height);
        g.setFont(new Font("Sanserif", Font.BOLD, 50));

        switch (clientState) {
            case STARTUP:
                // Starting the client
                g.setColor(Color.BLACK);
                g.setFont(ConstantLibrary.GUI.CLIENT_PLACEHOLDER_FONT);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Starting Client...", g);
                break;
            case LOADING:
                // Loading client assets
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, Game.gameResolution.width, Game.gameResolution.height);
                g.setColor(Color.BLACK);
                g.setFont(ConstantLibrary.GUI.CLIENT_PLACEHOLDER_FONT);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Loading Client Assets...", g);
                break;
            case CONNECTING:
                // Starting the client
                g.setColor(Color.GRAY);
                g.fillRect(0, 0, Game.gameResolution.width, Game.gameResolution.height);
                g.setColor(Color.BLACK);
                g.setFont(ConstantLibrary.GUI.CLIENT_PLACEHOLDER_FONT);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Connecting to Server...", g);
                break;
            case RUNNING:
                // draw players, objects, scene, etc.
                g.setColor(Color.CYAN);
                g.setFont(ConstantLibrary.GUI.CLIENT_PLACEHOLDER_FONT);
                g.fillRect(0, 0, Game.gameResolution.width, Game.gameResolution.height);
                renderPlayers(g);
                break;
            case DISCONNECTING:
                // When the client lost connection with the server and has not stopped the client yet to go back to the main menus
                g.setColor(Color.BLACK);
                g.setFont(ConstantLibrary.GUI.CLIENT_PLACEHOLDER_FONT);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Disconnecting...", g);
                break;
            case SHUTTING_DOWN:
                // idk when this would be used
                g.setColor(Color.RED);
                g.fillRect(0, 0, Game.gameResolution.width, Game.gameResolution.height);
                g.setColor(Color.BLACK);
                g.setFont(ConstantLibrary.GUI.CLIENT_PLACEHOLDER_FONT);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Shutting Down Client...", g);
                break;
        }
    }
    public void renderPlayers(Graphics2D g) {

        for (CSimplePlayer sp : playerManager.playerMap.values()) {
            renderPlayer(sp.posX, sp.posY, sp.direction, sp.colorFloat, g);
        }
        for (CSimplePlayer sp : playerManager.playerMap.values()) {
            renderPlayerName(sp.posX, sp.posY, sp.displayName, sp.direction, g);
        }
        renderPlayer((int)player.posX, (int)player.posY, player.direction, player.colorFloat, g);
        renderPlayerName((int)player.posX, (int)player.posY, player.displayName, player.direction, g);
    }
    public void renderPlayer(int x, int y, Direction4 direction, float colorFloat, Graphics2D g) {
        Color color = Color.getHSBColor(colorFloat, 1.0f, 1.0f);
        int size = 30;
        int hs = size / 2; // hs is for Half Size
        boolean simple = false;

        if (simple == false) { // Complex player rendering
            double em = 0.5; // em is for Edge Multiplier
            ArrayList<Point> points = new ArrayList<Point>(Arrays.asList(new Point[]{
                new Point(-hs, (int)(hs * em)),          // bottom left
                new Point(-hs, -hs),         // top left
                new Point(hs, -hs),          // top right
                new Point(hs, (int)(hs * em)),
                new Point((int)(hs * 1.5), (int)(hs * em)),
                new Point(0, (int)(hs * 2)),
                new Point((int)(-hs * 1.5), (int)(hs * em))
            }));
            for (Point p : points) {
                p.x += x;
                p.y += y;
            }

            if (direction != Direction4.SOUTH) {
                int angle = 0;
                switch (direction) {
                    case WEST:
                        angle = 90;
                        break;
                    case NORTH:
                        angle = 180;
                        break;
                    case EAST:
                        angle = 270;
                        break;
                }
                for (int i = points.size() - 1; i >= 0; i--) {
                    points.set(i, EngineCalculator.rotatePoint(new Point(x, y), points.get(i), (double)angle));
                }
            }
            Polygon polygon = EngineCalculator.pointsToPolygon(points);
            g.setColor(color);
            g.fillPolygon(polygon);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(5));
            g.drawPolygon(polygon);
            g.setStroke(new BasicStroke(1));
        } else { // Simple player rendering
            g.setColor(color);
            g.fillRect(x - size / 2, y - size / 2, size, size);
            g.setFont(new Font("Sanserif", Font.PLAIN, 20));
            g.setColor(Color.BLACK);
        }
    }
    public void renderPlayerName(int x, int y, String playerDisplayName, Direction4 playerDirection, Graphics2D g) {
        FontMetrics fm = g.getFontMetrics();
        int nameWidth = fm.stringWidth(playerDisplayName);

        g.setColor(Color.BLACK);
        g.setFont(ConstantLibrary.GamePlay.PLAYER_DISPLAY_NAME_FONT);
        g.drawString(playerDisplayName, x - nameWidth / 2, y - 40);
        //g.drawString(playerDirection.toString(), x, y);
    }
}
