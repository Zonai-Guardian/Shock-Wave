package com.shockWave.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import com.shockWave.Game;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.networking.PacketManager.PacketPurpose;
import com.shockWave.server.Server;
import com.shockWave.server.ServerPacketPurpose;

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
        while (Game.server.shouldStopServer == false) {
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
        while (Game.server.shouldStopServer == false) {
            try {
                Socket newClientSocket = serverSocket.accept();
                int playerID = Server.getNewPlayerID();
                sockets.add(new ServerSocketHandler(newClientSocket, playerID));
            } catch (IOException ignore) {}
        }
    }

    public void handleSendingPackets() {
        // Create packet lists
        ArrayList<Packet> packets = new ArrayList<>();
        Map<Integer, ArrayList<Packet>> datagramPackets = new LinkedHashMap<Integer, ArrayList<Packet>>();
        Map<Integer, ArrayList<Packet>> socketPackets =  new LinkedHashMap<Integer, ArrayList<Packet>>();

        packetsToSend.drainTo(packets);

        // Separate packets into different lists and assign targeted client IDs
        for (Packet packet : packets) {
            if (EngineCalculator.enumToShort(PacketPurpose.class, EngineCalculator.shortToPacketPurpose(packet.packetPurpose)) <= EngineCalculator.enumToShort(PacketPurpose.class, PacketPurpose.DISCONNECT)) {
                if (socketPackets.containsKey(packet.toFromID) == false) {
                    socketPackets.put(packet.toFromID, new ArrayList<>());
                }
                socketPackets.get(packet.toFromID).add(packet);
            } else {
                if (datagramPackets.containsKey(packet.toFromID) == false) {
                    datagramPackets.put(packet.toFromID, new ArrayList<>());
                }
                datagramPackets.get(packet.toFromID).add(packet);
            }
        }

        // Sends each set of packets to the right client using Sockets
        for (ServerSocketHandler handler : sockets) {
            if (socketPackets.containsKey(handler.clientID)) {
                if (handler.isLinkedToClientOnDevice) {
                    packetsForDevice.addAll(socketPackets.get(handler.clientID));
                } else {
                    PacketManager.writePacketsToOutputStream(sockets.get(handler.clientID).outputStream, socketPackets.get(handler.clientID));
                }
            }
        }


        // Sends each set of packets to the right client using Datagrams
        for (ServerSocketHandler handler : sockets) {
            if (datagramPackets.containsKey(handler.clientID)) {
                if (handler.isLinkedToClientOnDevice) {
                    packetsForDevice.addAll(datagramPackets.get(handler.clientID));
                } else {
                    // Sends with datagram
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

                    try {
                        datagramStreamOut = new DataOutputStream(byteStream);

                        PacketManager.writePacketsToOutputStream(datagramStreamOut, datagramPackets.get(handler.clientID));

                        byte[] buffer = byteStream.toByteArray();
                        
                        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, handler.clientAddress, handler.clientPort);
                        datagramSocket.send(datagramPacket);
                    } catch (IOException e) {
                        System.out.println("Failed to fill and send Datagram Packet in ServerSocketManager.handleSendingPackets()! Info: ");
                        EngineCalculator.printExceptionInfo(e);
                    }
                    PacketManager.writePacketsToOutputStream(sockets.get(handler.clientID).outputStream, socketPackets.get(handler.clientID));
                }
            }
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
    public ArrayList<Packet> getReceivedPackets() {
        ArrayList<Packet> receivedPackets = new ArrayList<>();

        if (Game.client == null) {return receivedPackets;} //stop if client has not been started yet

        packetsReceived.drainTo(receivedPackets);
        Game.client.socketManager.packetsForDevice.drainTo(receivedPackets);
        return receivedPackets;
    }
}