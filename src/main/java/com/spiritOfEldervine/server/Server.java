package com.spiritOfEldervine.server;

import com.spiritOfEldervine.networking.Packet;
import com.spiritOfEldervine.networking.ServerSocketHandler;
import com.spiritOfEldervine.networking.ServerSocketManager;
import com.spiritOfEldervine.networking.PacketManager.PacketPurpose;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.spiritOfEldervine.engine.EngineCalculator;

public class Server implements Runnable {
    // For Networking
    public boolean isServerOnline = false;
    public ServerSocketManager socketManager = null;

    // For Game Loop
    public boolean shouldStopServer = false;
    private int tps = 0; // Ticks Per Second
    public static final int TARGET_TPS = 20;
    private final double TIME_BETWEEN_TICKS = 1000000000 / TARGET_TPS;

    // For Inner Workings Of Server
    public enum ServerState{STARTUP, LOADING, RUNNING, STOPPING}
    public enum GameMode{STORY_MODE, BATTLE_MODE}
    public static ServerState serverState = null;

    //Other variables for the server to run:
    private static int nextPlayerID = 0;

    public PlayerManager playerManager = new PlayerManager();
    public Map<Integer, PlayerEntity> playerEntities = new LinkedHashMap<Integer, PlayerEntity>();


    // Constructor
    public Server() {
        new Thread(this).start();
        socketManager = new ServerSocketManager();

        int id = getNewPlayerID();
        playerManager.createPlayer(id);
        socketManager.sockets.add(new ServerSocketHandler(id));

    }
    public void shutDown() {
        shouldStopServer = true;
        isServerOnline = false;
        socketManager.stop();
    }
    public static int getNewPlayerID() {
        int id = nextPlayerID;
        nextPlayerID++;
        return id;
    }

    @Override
    public void run() {
        long lastUpdateTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        int ticks = 0;
        
        while (shouldStopServer == false) {
            long now = System.nanoTime();

            // Update game logic
            if (now - lastUpdateTime >= TIME_BETWEEN_TICKS) {
                updateServer((now - lastUpdateTime) / TIME_BETWEEN_TICKS);
                ticks++;
                lastUpdateTime += TIME_BETWEEN_TICKS;
            }

            // Print FPS and UPS every second
            if (System.currentTimeMillis() - timer >= 1000) {
                if (ticks < 15) {System.out.println("FPS: " + ticks);} // Prints if tps drops below 15
                tps = ticks;
                ticks = 0;
                timer += 1000;
            }
            
        }        
    }
    
    public void updateServer(double delta) {
        switch (serverState) {
            case STARTUP:
                serverState = ServerState.LOADING;
                new Thread(() -> {loadAllData();}).start();
                break;
            case LOADING:
                // Do nothing special
                break;
            case RUNNING:
                socketManager.receiveDevicePackets();
                handleReceivedPackets();
                // update game physics, logic, etc. (Make sure to use delta!!!!)
                generatePackets();
                sendPackets();
                break;
        }
    }
    
    // Loading
    public void loadAllData() {
        //Load Data

    }
    
    public void createPlayer(int playerID) {
        playerManager.createPlayer(playerID);
    }

    // Packet Management
    private void handleReceivedPackets() {
        for (Packet packet : socketManager.packetsReceived) {
            switch (EngineCalculator.shortToPacketPurpose(packet.packetPurpose)) {
                case DISCONNECT:
                    socketManager.disconnectSocket(packet.toFromID);
                    break;
                case PLAYER_UPDATE:
                    playerEntities.get(packet.toFromID).position = new Point2D.Double(
                            packet.readData().doubleVar,
                            packet.readData().doubleVar
                        );
                    break;
                case PACKET_TEST:
                    System.out.println("Int: " + packet.readData().integerVar + ", String: " + packet.readData().stringVar + ", Double: " + packet.readData().doubleVar);
                    break;
            }
        }
    }
    public void generatePackets() {
        for (ServerPacketPurpose purpose : socketManager.packetsToGenerate) {
            switch (purpose.packetPurpose) {
                case REGISTER_PLAYER:
                    // int id, string displayName, double posistionX, double positionY, double velocityX, double velocityY
                    
            }
        }
    }


    public void openToLan(int port) {
        isServerOnline = true;
        socketManager.openPort(port);
    }
}