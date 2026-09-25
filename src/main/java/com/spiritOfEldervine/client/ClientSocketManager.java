package com.spiritOfEldervine.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.engine.EngineCalculator;
import com.spiritOfEldervine.networking.Packet;
import com.spiritOfEldervine.networking.PacketManager;
import com.spiritOfEldervine.networking.PacketManager.PacketPurpose;

public class ClientSocketManager {
    
    // Server Connection Stuff
    public boolean isHost = false;
    public boolean shouldStopClient = false;
    public InetAddress serverAddress = null;
    public Integer serverPort = null; // accepts null

    // Socket Stuff
    private Socket socket = null;
    private DataInputStream socketInputStream = null;
    private DataOutputStream socketOutputStream = null;

    // Datagram Socket Stuff
    private DatagramSocket datagramSocket = null;
    private DataInputStream datagramInputStream = null;
    private DataOutputStream datagramOutputStream = null;

    // Packet List Stuff
    public LinkedBlockingQueue<PacketPurpose> packetsToGenerate = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Packet> packetsToSend = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Packet> packetsReceived = new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<Packet> packetsForDevice = new LinkedBlockingQueue<>(); // This will hold packets for the server if it is on this device

    public ClientSocketManager(InetAddress serverAddress, Integer serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        startSocket();
    }
    public ClientSocketManager() {
        isHost = true;
    }

    private void startSocket() {
        try {
            socket = new Socket(serverAddress, serverPort);
            socketInputStream = new DataInputStream(socket.getInputStream());
            socketOutputStream = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {receivePackets();}).start();
        } catch (IOException e) {
            System.out.println("Failed to open socket, inputStream, or outputStream in ClientSocketManager.startSocket()!");
            EngineCalculator.printExceptionInfo(e);
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
    public void handleSendingPackets() {
        generatePackets();
        sendPackets();
    }
    private void generatePackets() {
        ArrayList<PacketPurpose> purposes = new ArrayList<>();
        packetsToGenerate.drainTo(purposes);

        for (PacketPurpose purpose : purposes) {
            Packet packet = new Packet(EngineCalculator.enumToShort(PacketPurpose.class, purpose), -1);;
            switch (purpose) {
                case REGISTER_PLAYER_TO_SERVER:
                    // string displayName
                    packet.writeData(null);
                    break;
                case PACKET_TEST:
                    packet.writeData((2026));
                    packet.writeData("This is a packet string!");
                    packet.writeData(0.123456789);
                    break;
            }
            packetsToSend.add(packet);
        }
    }
    private void sendPackets() {
        ArrayList<Packet> packets = new ArrayList<>();
        ArrayList<Packet> datagramPackets = new ArrayList<>();
        ArrayList<Packet> socketPackets = new ArrayList<>();

        packetsToSend.drainTo(packets);
        for (Packet packet : packets) {
            if (EngineCalculator.enumToShort(PacketPurpose.class, EngineCalculator.shortToPacketPurpose(packet.packetPurpose)) <= EngineCalculator.enumToShort(PacketPurpose.class, PacketPurpose.DISCONNECT)) {
                socketPackets.add(packet);
            } else {
                datagramPackets.add(packet);
            }
        }

        PacketManager.writePacketsToOutputStream(datagramOutputStream, datagramPackets);
        // #InDev  #Warning  The previous line of code needs to be checked to make sure that there is nothing else needed to send a datagram packet!!!!

        PacketManager.writePacketsToOutputStream(socketOutputStream, socketPackets);
        // This should be good...
    }

    private void receivePackets() { // !!! !WARNING! this method occupies the thread until the server goes offline!!!
        while (shouldStopClient == false) {
            if (socket.isConnected()) {
                ArrayList<Packet> packets = PacketManager.readPacketsFromInputStream(socketInputStream, -1);
                for (Packet packet : packets) {
                    packetsReceived.add(packet);
                }
            }
        }
    }
    private void receiveDatagramPackets() { // !!! !WARNING! this method occupies the thread until the server goes offline!!!
        while (shouldStopClient == false) {
            try {
                // Create byte array
                byte[] reveiveBuffer = new byte[1024];
                DatagramPacket incomingPacket = new DatagramPacket(reveiveBuffer, reveiveBuffer.length);

                // Wait for data (blocks execution)
                datagramSocket.receive(incomingPacket);

                // Get who sent the packet (what address and port)
                //InetAddress clientAddress = incomingPacket.getAddress();
                //int clientPort = incomingPacket.getPort();

                // Read the data out of the packet
                ByteArrayInputStream byteStream = new ByteArrayInputStream(incomingPacket.getData(), 0, incomingPacket.getLength());
                DataInputStream inputStream = new DataInputStream(byteStream);

                // Package the data into custom packets for later handling
                ArrayList<Packet> packets = PacketManager.readPacketsFromInputStream(inputStream, -1); // From server so senderID is -1
                
                for (Packet packet : packets) {
                    packetsReceived.add(packet);
                }
                                
            } catch (IOException e) {
                System.out.println("Failed to receive packet in ClientSocketManager.receiveDatagramPackets()");
                EngineCalculator.printExceptionInfo(e);
            }
        }
    }
    
}
