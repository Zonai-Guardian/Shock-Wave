package com.shockWave.server;

import java.util.ArrayList;

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
    public ServerSocketManager socketManager = null;

    // For Game Loop
    public boolean shouldStopServer = false;
    private int tps = 0; // Ticks Per Second
    public static final int TARGET_TPS = 20; // Ticks Per Second
    private final double TIME_BETWEEN_TICKS = 1000000000 / TARGET_TPS;

    private int packetGenerationTick = 0;

    // For Inner Workings Of Server
    public enum ServerState{STARTUP, LOADING, RUNNING, STOPPING}
    public enum GameMode{STORY_MODE, BATTLE_MODE}
    public ServerState serverState = ServerState.STARTUP;

    //Other variables for the server to run:
    private static int nextPlayerID = 0;

    public SPlayerManager playerManager = new SPlayerManager();


    // Constructor
    public Server(int port) {
        socketManager = new ServerSocketManager(port);

        int id = getNewPlayerID();
        socketManager.sockets.add(new ServerSocketHandler(id));
        new Thread(() -> {run();}).start();
    }
    public void shutDown() {
        shouldStopServer = true;
        socketManager.shutDown();
    }
    public void shutDownGently() {
        serverState = ServerState.STOPPING;
        shouldStopServer = true;
        socketManager.startShutDown();
    }
    public boolean isReadyToShutDown() {
        return socketManager.isReadyToShutDown();
    }
    public void finishShutDown() {
        socketManager.finishShutDown();
    }
    public static int getNewPlayerID() {
        int id = nextPlayerID;
        nextPlayerID++;
        return id;
    }

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
        // System.out.println("Number: " + EngineCalculator.enumToInteger(PacketDataType.class, PacketDataType.PURPOSE));
        // Game.exitGame(1);

        switch (serverState) {
            case STARTUP:
                socketManager.openPort();
                serverState = ServerState.LOADING;
                new Thread(() -> {loadAllData();}).start();
                break;
            case LOADING:
                // Do nothing special
                break;
            case RUNNING:
                socketManager.receiveDevicePackets();
                handleReceivedPackets();
                socketManager.update();
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
            //System.out.println("    Receiving packet in Server with purpose: " + EngineCalculator.shortToPacketPurpose(packet.packetPurpose));

            switch (EngineCalculator.shortToPacketPurpose(packet.packetPurpose)) {
                case DISCONNECT:
                    socketManager.disconnectSocket(packet.toFromID);
                    break;
                case REGISTER_PLAYER_TO_SERVER_S1:
                    // bookmark
                    // Create new player-type (??) object in the server
                    playerManager.createPlayer(new SPlayer(packet.readData().stringVar, (short) packet.toFromID, 0, 0, Direction4.SOUTH));
                    
                    socketManager.packetsToGenerate.add(new ServerPacketPurpose(PacketPurpose.REGISTER_PLAYER_TO_SERVER_S2, packet.toFromID));
                    socketManager.packetsToGenerate.add(new ServerPacketPurpose(PacketPurpose.REGISTER_NEW_PLAYER_TO_CLIENT, packet.toFromID));
                    System.out.println("Success 2!");
                    break;
                case UPDATE_PLAYER_TRANSFORM:
                    SPlayer player = playerManager.getPlayer((short)packet.toFromID);
                    player.posX = packet.readData().integerVar;
                    player.posY = packet.readData().integerVar;
                    player.velocityX = (int)(double)packet.readData().doubleVar;
                    player.velocityY = (int)(double)packet.readData().doubleVar;
                    player.direction = EngineCalculator.byteToDirection4(packet.readData().byteVar);
                    player.updatedRecently = true;
                    System.out.println("                         Boolean:" + playerManager.getPlayer((short)packet.toFromID).updatedRecently);
                    break;
            }
        }
    }
    public void generatePackets() {
        // Add last minute purposes
        packetGenerationTick++;
        if (packetGenerationTick > 1) {
            packetGenerationTick = 0;
            socketManager.addPacketToGenerate(new ServerPacketPurpose(PacketPurpose.UPDATE_PLAYERS_TRANSFORM, -1));
        }

        ArrayList<ServerPacketPurpose> packetPurposes = new ArrayList<>();
        socketManager.packetsToGenerate.drainTo(packetPurposes);

        // Iterate through packet purposes (packet creation requests with extra data)
        for (ServerPacketPurpose purpose : packetPurposes) {
            //System.out.println("    Generating packet in Server with purpose: " + purpose.packetPurpose);

            // Checks if the packet should just be sent to everyone
            //boolean sendPacketToEveryone = false;
            //for (Integer i : purpose.targetedClientIDs) {
            //    if (i < 0) {
            //        sendPacketToEveryone = true;
            //        break;
            //    }
            //}

            // Assembles Packet
            for (Integer i : purpose.targetedClientIDs) {
            
                int targetedClientID = i;

                int extraInt1 = 0;
                if (purpose.packetPurpose == PacketPurpose.REGISTER_NEW_PLAYER_TO_CLIENT || purpose.packetPurpose == PacketPurpose.DELETE_PLAYER_IN_CLIENT) {
                    extraInt1 = targetedClientID;
                    targetedClientID = -1;
                }

                Packet packet = new Packet(EngineCalculator.enumToShort(PacketPurpose.class, purpose.packetPurpose), targetedClientID);;
                switch (purpose.packetPurpose) {
                    case REGISTER_PLAYER_TO_SERVER_S2:
                        // string displayName
                        packet.writeData((short)(targetedClientID));  // Short id
                        packet.writeData((double)playerManager.getPlayer((short)targetedClientID).colorFloat);
                        packet.writeData(0);  // Int posX
                        packet.writeData(0);  // Int posY

                        // Loop of existing players:
                        for (SPlayer player : playerManager.getPlayerMap().values()) {
                            if (player.id != packet.toFromID && player.displayName != null) { // Not player that this packet is being sent to AND the player has finished being registered
                                packet.writeData(player.id); // short data type
                                packet.writeData(player.displayName);
                                packet.writeData((double)player.colorFloat);
                                packet.writeData(player.posX);
                                packet.writeData(player.posY);
                                packet.writeData(EngineCalculator.enumToByte(Direction4.class, player.direction));
                            }
                        }
                        packet.writeData((short) -2);

                        System.out.println("Success 3!");
                        break;
                    case REGISTER_NEW_PLAYER_TO_CLIENT:
                        short id = (short)extraInt1;
                        SPlayer player = playerManager.getPlayer(id);
                        
                        // Short id, String name, Int posX, Int posY, Byte Direction4
                        packet.writeData(id);
                        packet.writeData(player.displayName);
                        packet.writeData((double)player.colorFloat);
                        packet.writeData(player.posX);
                        packet.writeData(player.posY);
                        packet.writeData(EngineCalculator.enumToByte(Direction4.class, player.direction));
                        break;
                    case DELETE_PLAYER_IN_CLIENT:
                        packet.writeData((short)extraInt1);
                        break;
                    case UPDATE_PLAYERS_TRANSFORM:
                        for (SPlayer sPlayer : playerManager.getPlayerMap().values()) {
                            if (sPlayer.updatedRecently == false) {continue;}
                            System.out.println("Broadcasting player position id: " + sPlayer.id);
                            packet.writeData(sPlayer.id);
                            packet.writeData(sPlayer.posX);
                            packet.writeData(sPlayer.posY);
                            packet.writeData(sPlayer.velocityX);
                            packet.writeData(sPlayer.velocityY);
                            packet.writeData(EngineCalculator.enumToByte(Direction4.class, sPlayer.direction));
                            sPlayer.updatedRecently = false;
                        }
                        packet.writeData((short) -2);

                        //System.out.println(packet);
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