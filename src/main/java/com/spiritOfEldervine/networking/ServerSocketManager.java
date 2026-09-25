package com.spiritOfEldervine.networking;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.engine.EngineCalculator;
import com.spiritOfEldervine.server.Server;
import com.spiritOfEldervine.server.ServerPacketPurpose;

public class ServerSocketManager {
    public ServerSocket serverSocket = null;

    public LinkedBlockingQueue<ServerPacketPurpose> packetsToGenerate = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Packet> packetsToSend = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Packet> packetsReceived = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Packet> packetsForDevice = new LinkedBlockingQueue<>(); // This will hold packets for the client that is on this device

    public ArrayList<ServerSocketHandler> sockets = new ArrayList<>();
    
    public DatagramSocket datagramSocket = null;
    public DataInputStream datagramStreamIn = null;
    public DataOutputStream datagramStreamOut = null;

    // For sending packets to individual clients
    // maybe use this:
    //public LinkedHashMap<Integer, ArrayList<Packet>> packetsForClients = new LinkedHashMap<>();

    public ServerSocketManager() {
        // Do nothing
    }
    public void stop() {
        if (datagramSocket != null) {datagramSocket.close();}
        for (ServerSocketHandler socket : sockets) {
            socket.close();
        }
    }
    public void disconnectSocket(int clientID) {
        for (int i = sockets.size() - 1; i >= 0; i--) {
            ServerSocketHandler handler = sockets.get(i);
            if (handler.isLinkedToClientOnDevice == false && handler.clientID == clientID) {
                handler.close();
                sockets.remove(i);
            }
        }
    }
    public void openPort(int port) {
        // Socket
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port);
                new Thread(() -> {acceptClients();}).start();
                System.out.println("Successfully opened port " + port + " in ServerSocketManager.openPort()");
            } catch(IOException e) {
                System.out.println("Failed to create ServerSocket with port \"" + port + "\" in Server.Communicator.ServerSocket");
            }
        } else {
            System.out.println("Port is already open in ServerSocketManager.openPort()! Previous port: " + serverSocket.getLocalPort() + ", new port: " + port);
        }

        // Datagram Socket
        if (datagramSocket == null) {
            try {
                // Create Datagram Socket
                datagramSocket = new DatagramSocket();
                // Start a new thread receiving packets
                new Thread(() -> {receiveDatagramPackets();}).start();
            } catch (SocketException e) {
                System.out.println("Socket Exception Occurred in ServerSocketManager.openPort()!");
                EngineCalculator.printExceptionInfo(e);
            }
        }
    }

    public void receiveDevicePackets() {
        for (ServerSocketHandler handler : sockets) {
            handler.receiveDevicePackets();
        }
    }
    
    private void receiveDatagramPackets() { // !!! !WARNING! this method occupies the thread until the server goes offline!!!
        while (Game.server.isServerOnline) {
            try {
                // Create byte array
                byte[] reveiveBuffer = new byte[1024];
                DatagramPacket incomingPacket = new DatagramPacket(reveiveBuffer, reveiveBuffer.length);

                // Wait for data (blocks execution)
                datagramSocket.receive(incomingPacket);

                // Get who sent the packet (what address and port)
                InetAddress clientAddress = incomingPacket.getAddress();
                int clientPort = incomingPacket.getPort();

                // Read the data out of the packet
                ByteArrayInputStream byteStream = new ByteArrayInputStream(incomingPacket.getData(), 0, incomingPacket.getLength());
                DataInputStream inputStream = new DataInputStream(byteStream);

                // Package the data into custom packets for later handling
                for (Packet packet : PacketManager.readPacketsFromInputStream(inputStream, getClientID(clientAddress, clientPort))) {
                    packetsReceived.add(packet);
                }
            } catch (IOException e) {
                System.out.println("Failed to receive packet in ServerSocketManager.receiveDatagramPackets()");
                EngineCalculator.printExceptionInfo(e);
            }
        }
    }
    
    private void acceptClients() { // !!! !WARNING! this method occupies the thread until the server goes offline!!!
        while (Game.server.isServerOnline) {
            try {
                Socket newClientSocket = serverSocket.accept();
                int playerID = Server.getNewPlayerID();
                sockets.add(new ServerSocketHandler(newClientSocket, playerID));
                Game.server.createPlayer(playerID);
            } catch (IOException ignore) {}
        }
    }

    public Integer getClientID(InetAddress clientAddress, int clientPort) {
        for (ServerSocketHandler handler : sockets) {
            if (handler.clientAddress == clientAddress && handler.clientPort == clientPort) {
                return handler.clientID;
            }
        }
        System.out.println("Could not find ServerSocketHandler in sockets with the address " + clientAddress + " and the port " + clientPort + " in ServerSocketManager.getClientID()!");
        EngineCalculator.printStackTrace();
        return null;
    }
}