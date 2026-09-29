package com.shockWave.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.net.InetAddress;
import java.util.ArrayList;

import com.shockWave.Game;
import com.shockWave.client.players.CPlayer;
import com.shockWave.client.players.CPlayerManager;
import com.shockWave.client.players.CSimplePlayer;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.graphics.RenderEngine;
import com.shockWave.networking.Packet;
import com.shockWave.networking.PacketManager.PacketPurpose;

public class Client {
    public ClientSocketManager socketManager;

    // enums
    public static enum ClientState {STARTUP, LOADING, CONNECTING, RUNNING, EXITING, LOST_CONNECTION} // I don't know if I'll actually use all of these...
    
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

    public void loadAllData() {
        //load all required data to run the client

        socketManager.packetsToGenerate.add(PacketPurpose.REGISTER_PLAYER_TO_SERVER_S1);
        clientState = ClientState.CONNECTING;
    }
    public void handleInput(ArrayList<String> buttonInputs, ArrayList<String> actionInputs) {
        //handle what to do when buttons are pressed and actions are activated

    }
    public void updateClient() {
        handleReceivedPackets();
        
        // update game logic simulation
        switch (clientState) {
            case STARTUP:
                // It just needs to change ClientState to Loading
                clientState = ClientState.LOADING;
                new Thread(() -> {loadAllData();}).start(); // Start loading data on new Thread
                break;
            case LOADING:
                // Do nothing as the client's assets are being loaded...
                break;
            case CONNECTING:
                // Do nothing...
                break;
            case RUNNING:
                // Do nothing...
                break;
            case EXITING:
                // Do nothing...
                break;
            case LOST_CONNECTION:
                // Do nothing...
                break;
        }
        
        generatePackets();
        socketManager.handleSendingPackets();
    }

    private void handleReceivedPackets() {
        for (Packet packet : socketManager.getReceivedPackets()) {
            switch (EngineCalculator.shortToPacketPurpose(packet.packetPurpose)) {
                case DISCONNECT:
                    socketManager.shutDownClient();
                    break;
                case REGISTER_PLAYER_TO_SERVER_S2:
                    // bookmark
                    // Create new player-type (??) object in the server
                    player = new CPlayer(
                        playerDisplayName,
                        packet.readData().shortVar, //short id
                        packet.readData().integerVar, // int posX
                        packet.readData().integerVar // int posY
                    );
                    System.out.println("Success 4!");
                    break;
                case REGISTER_PLAYER_TO_CLIENT:
                    // String name, Short id, Int posX, Int posY, Byte Direction4
                    
                    playerManager.registerPlayer(new CSimplePlayer(
                        packet.readData().stringVar, // string name
                        packet.readData().shortVar, //short id
                        packet.readData().integerVar, // int posX
                        packet.readData().integerVar, // int posY
                        EngineCalculator.byteToDirection4(packet.readData().byteVar)
                    ));
                    break;
                case PACKET_TEST:
                    System.out.println("Int: " + packet.readData().integerVar + ", String: " + packet.readData().stringVar + ", Double: " + packet.readData().doubleVar);
                    break;
            }
        }
    }
    private void generatePackets() {
        ArrayList<PacketPurpose> purposes = new ArrayList<>();
        socketManager.packetsToGenerate.drainTo(purposes);

        for (PacketPurpose purpose : purposes) {
            Packet packet = new Packet(EngineCalculator.enumToShort(PacketPurpose.class, purpose), -1);;
            switch (purpose) {
                case REGISTER_PLAYER_TO_SERVER_S1:
                    // string displayName
                    packet.writeData(playerDisplayName);
                    System.out.println("Success 1!");
                    break;
                case PACKET_TEST:
                    packet.writeData((2026));
                    packet.writeData("This is a packet string!");
                    packet.writeData(0.123456789);
                    break;
            }
            socketManager.packetsToSend.add(packet);
        }
    }

    //Rendering
    public void render(Graphics2D g) {
        //render everything the client should
        //g.fillRect(0, 0, Game.gameResolution.width, Game.gameResolution.height);
        
        switch (clientState) {
            case STARTUP:
                // Starting the client
                g.setColor(Color.BLACK);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Starting Client...", g);
                break;
            case LOADING:
                // Loading client assets
                g.setColor(Color.BLACK);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Loading Client Assets...", g);
                break;
            case CONNECTING:
                // Starting the client
                g.setColor(Color.BLACK);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Connecting to Server...", g);
                break;
            case RUNNING:
                // draw players, objects, scene, etc.
                break;
            case EXITING:
                // idk when this would be used
                g.setColor(Color.BLACK);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Exiting...", g);
                break;
            case LOST_CONNECTION:
                // When the client lost connection with the server and has not stopped the client yet to go back to the main menus
                g.setColor(Color.BLACK);
                RenderEngine.drawTextCenteredInRect(new Rectangle(0, 0, Game.gameResolution.width, Game.gameResolution.height), "Lost Connection...", g);
                break;
        }
    }
}
