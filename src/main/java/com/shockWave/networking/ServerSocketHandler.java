package com.shockWave.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import com.shockWave.Game;
import com.shockWave.engine.EngineCalculator;

public class ServerSocketHandler {
    // For when the connected to the client that's on this device
    public boolean isLinkedToClientOnDevice = false; // This indicates whether this is linked to a client that is on this device, which needs to be accessed differently.

    public int clientID; // as soon as this socket is accepted it will have a clientID
    public InetAddress clientAddress;
    public int clientPort;

    private Socket clientSocket;
    public DataInputStream inputStream;
    public DataOutputStream outputStream;

    private boolean shouldShutDown = false;
    public boolean isReceivingThreadRunning = false;

    public long lastPacketReceiveTime = 0; // 0 means that it has not received a packet yet
    public PacketWrapper splitPacketWrapper = new PacketWrapper(null);
    
    public ServerSocketHandler(int clientID) {
        isLinkedToClientOnDevice = true;
        this.clientID = clientID;
    }
    public ServerSocketHandler(Socket clientSocket, int clientID) {
        this.clientSocket = clientSocket;
        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Failed to create DataInputStream or DataOutputStream in ServerSocketHandler constructor!");
            EngineCalculator.printExceptionInfo(e);
            Game.exitGame(1);
        }
        clientAddress = clientSocket.getInetAddress();
        clientPort = clientSocket.getPort();

        this.clientID = clientID;
        
        new Thread(() -> {receivePackets();}).start();
    }
    public void startShutDown() {
        shouldShutDown = true;
    }
    public boolean isReadyToShutDown() {
        return isReceivingThreadRunning;
    }
    public void finishShutDown() {
        closeStreams();
    }
    public void closeStreams() {
        // Close inputStream
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ignore) {}
        // Close outputStream
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException ignore) {}
        // Close clientSocket
        try {
            if (clientSocket != null && clientSocket.isClosed() == false) {
                clientSocket.close();
            }
        } catch (IOException ignore) {}
    }

    private void receivePackets() { // !!! !WARNING! this method occupies the thread until the server goes offline or stops!!!
        if (isLinkedToClientOnDevice) {
            return; // Don't keep the thread, stop method
        } else {
            isReceivingThreadRunning = true;
            while (Game.server.shouldStopServer == false && shouldShutDown == false) { // If either of them becomes true it will stop
                if (clientSocket.isConnected()) {
                    ArrayList<Packet> packets = PacketManager.readPacketsFromInputStream(inputStream, Game.server.socketManager.getClientID(clientAddress, clientPort), splitPacketWrapper);
                    for (Packet packet : packets) {
                        System.out.println("Received a packet via a Socket!");
                        Game.server.socketManager.packetsReceived.add(packet);
                        lastPacketReceiveTime = System.currentTimeMillis();
                    }
                }
            }
            isReceivingThreadRunning = false;
        }
    }
    public void receiveDevicePackets() { // This will get called every tick to receive packets meant for the server from the client on the same device
        if (isLinkedToClientOnDevice) {
            if (Game.client == null) {return;}
            // Get packets from client
            ArrayList<Packet> packets = new ArrayList<>();
            Game.client.socketManager.packetsForDevice.drainTo(packets);

            for (Packet packet : packets) {
                packet.toFromID = clientID;
                Game.server.socketManager.packetsReceived.add(packet);
            }

            // This drains the packets for the server from the client to the server's received packets list
        } else {
            // Do nothing
        }
    }
}
