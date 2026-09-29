package com.shockWave.server;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shockWave.Game.Direction4;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.networking.Packet;
import com.shockWave.networking.ServerSocketHandler;
import com.shockWave.networking.ServerSocketManager;
import com.shockWave.networking.PacketManager.PacketPurpose;
import com.shockWave.server.player.SPlayerManager;
import com.shockWave.server.player.SPlayer;

public class Server implements Runnable {
    // For Networking
    private int port;
    public ServerSocketManager socketManager = null;

    // For Game Loop
    public boolean shouldStopServer = false;
    private int tps = 0; // Ticks Per Second
    public static final int TARGET_TPS = 20;
    private final double TIME_BETWEEN_TICKS = 1000000000 / TARGET_TPS;

    // For Inner Workings Of Server
    public enum ServerState{STARTUP, LOADING, RUNNING, STOPPING}
    public enum GameMode{STORY_MODE, BATTLE_MODE}
    public static ServerState serverState = ServerState.STARTUP;

    //Other variables for the server to run:
    private static int nextPlayerID = 0;

    public SPlayerManager playerManager = new SPlayerManager();


    // Constructor
    public Server(int port) {
        this.port = port;
        socketManager = new ServerSocketManager();

        int id = getNewPlayerID();
        socketManager.sockets.add(new ServerSocketHandler(id));
        
        new Thread(this).start();
    }
    public void shutDown() {
        shouldStopServer = true;
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
                socketManager.openPort(port);
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
                socketManager.handleSendingPackets();
                break;
        }
    }
    
    // Loading
    public void loadAllData() {
        //Load Data

        System.out.println("Finished loading Server assets");
        serverState = ServerState.RUNNING;
    }

    // Packet Management
    private void handleReceivedPackets() {
        for (Packet packet : socketManager.getReceivedPackets()) {
            switch (EngineCalculator.shortToPacketPurpose(packet.packetPurpose)) {
                case DISCONNECT:
                    socketManager.disconnectSocket(packet.toFromID);
                    break;
                case REGISTER_PLAYER_TO_SERVER_S1:
                    // bookmark
                    // Create new player-type (??) object in the server
                    System.out.println("Received Name: " + packet.readData().stringVar);
                    socketManager.packetsToGenerate.add(new ServerPacketPurpose(PacketPurpose.REGISTER_PLAYER_TO_SERVER_S2, packet.toFromID));
                    socketManager.packetsToGenerate.add(new ServerPacketPurpose(PacketPurpose.REGISTER_PLAYER_TO_CLIENT, packet.toFromID));
                    System.out.println("Success 2!");
                    break;
                case PACKET_TEST:
                    System.out.println("Int: " + packet.readData().integerVar + ", String: " + packet.readData().stringVar + ", Double: " + packet.readData().doubleVar);
                    break;
            }
        }
    }
    public void generatePackets() {
        ArrayList<ServerPacketPurpose> packetPurposes = new ArrayList<>();
        socketManager.packetsToGenerate.drainTo(packetPurposes);

        // Iterate through packet purposes (packet creation requests with extra data)
        for (ServerPacketPurpose purpose : packetPurposes) {
            // Checks if the packet should just be sent to everyone
            boolean sendPacketToEveryone = false;
            for (Integer i : purpose.targetedClientIDs) {
                if (i < 0) {
                    sendPacketToEveryone = true;
                    break;
                }
            }

            // Assembles Packet
            for (Integer i : purpose.targetedClientIDs) {
                int targetedClientID = sendPacketToEveryone ? -1 : i;

                int extraInt1 = 0;
                if (purpose.packetPurpose == PacketPurpose.REGISTER_PLAYER_TO_CLIENT) {
                    extraInt1 = targetedClientID;
                    targetedClientID = -1;
                }

                Packet packet = new Packet(EngineCalculator.enumToShort(PacketPurpose.class, purpose.packetPurpose), targetedClientID);;
                switch (purpose.packetPurpose) {
                    case REGISTER_PLAYER_TO_SERVER_S2:
                        // string displayName
                        packet.writeData((Short)(short)(targetedClientID));  // Short id
                        packet.writeData(0);  // Int posX
                        packet.writeData(0);  // Int posY

                        System.out.println("Success 3!");
                        break;
                    case REGISTER_PLAYER_TO_CLIENT:
                        short id = (short)extraInt1;
                        SPlayer player = playerManager.getPlayer(id);
                        
                        // String name, Short id, Int posX, Int posY, Byte Direction4
                        packet.writeData(player.displayName);
                        packet.writeData(id);
                        packet.writeData(player.posX);
                        packet.writeData(player.posY);
                        packet.writeData(EngineCalculator.enumToByte(Direction4.class, player.direction));

                        break;
                    case PACKET_TEST:
                        packet.writeData((2026));
                        packet.writeData("This is a packet string!");
                        packet.writeData(0.123456789);
                        break;
                }
                socketManager.packetsToSend.add(packet);

                if (i <= 1) {
                    break; // Stop sending this packet to multiple people if 
                }
            }
        }
    }

}