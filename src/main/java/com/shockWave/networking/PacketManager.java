package com.shockWave.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import com.shockWave.Game;
import com.shockWave.engine.EngineCalculator;

public class PacketManager {
    // Each packet will have a packet type id.
    // The packet type id will indicate what information the packet should contain and how to handle it.
    
    public static enum PacketPurpose {
        // Reliable, Slower
        //   RELIABLE_IMMEDIATE_PING,
        //   RELIABLE_QUICK_PING,

        // These are for when a client wants to join the server and they must register their player with the server and get their position information back from it
        REGISTER_PLAYER_TO_SERVER_S1, // Server <- Client  String name
        REGISTER_PLAYER_TO_SERVER_S2, // Server -> Client  Short id, Int posX, Int posY

        // This is for when a new client joins the server and the server tells the other clients about it
        REGISTER_PLAYER_TO_CLIENT, // String name, Short id, Int posX, Int posY, Byte Direction4

        // Disconnect
        DISCONNECT,

        // Fast, Not as reliable
        //   FAST_IMMEDIATE_PING,
        //   FAST_QUICK_PING,
        //  PLAYER_UPDATE,
        PLAYER_POSITION,        // Server <- Client  Int posX, Int posY, double velocityX, double velocityY, byte Direction4  desc: A client tells the server this stuff for their own player that the client controlls
        PACKET_TEST
    }
    public static enum PacketDataType { // Organized from most to least space usage
        PURPOSE, // actually a short, but signals the start of a new packet and needs to be handled differently
        STRING,
        DOUBLE,
        INTEGER,
        SHORT,
        BYTE
    }

    public static void writePacketsToOutputStream(DataOutputStream outputStream, ArrayList<Packet> packets) {
        for (Packet packet : packets) {
            try {
                outputStream.writeByte(EngineCalculator.enumToInteger(PacketDataType.class, PacketDataType.PURPOSE));
                outputStream.writeShort(packet.packetPurpose);
                for (PacketType type : packet.types) {
                    outputStream.writeByte(type.packetDataType);
                    switch (EngineCalculator.byteToPacketDataType(type.packetDataType)) {
                        case PURPOSE:
                            System.out.println("PacketType contains packetDataType with value \"PURPOSE\" in PacketManager.writePacketsToOutputStream()!");
                            EngineCalculator.printStackTrace();
                            break;
                        case STRING:
                            outputStream.writeUTF(type.stringVar);
                            break;
                        case DOUBLE:
                            outputStream.writeDouble(type.doubleVar);
                            break;
                        case INTEGER:
                            outputStream.writeInt(type.integerVar);
                            break;
                        case SHORT:
                            outputStream.writeShort(type.shortVar);
                            break;
                        case BYTE:
                            outputStream.writeByte(type.byteVar);
                            break;
                    }
                }
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Failed to write data to outputStream in PacketManager.writePacketsToOutputStream()!");
                EngineCalculator.printExceptionInfo(e);
            }
        }
    }
    public static ArrayList<Packet> readPacketsFromInputStream(DataInputStream inputStream, int senderID) { // senderID should be -1 if the server sent it to the client

        ArrayList<Packet> packetsReceived = new ArrayList<>();

        try {
            Packet packet = null;
            while (inputStream.available() > 0) {
                byte dataTypeIndex = inputStream.readByte();
                switch (EngineCalculator.byteToPacketDataType(dataTypeIndex)) {
                    case PURPOSE:
                        // Each packet should start with this
                        if (packet != null) {packetsReceived.add(packet);}
                        packet = new Packet(inputStream.readShort(), senderID);
                        break;
                    case STRING:
                        packet.writeData(dataTypeIndex, inputStream.readUTF());
                        break;
                    case DOUBLE:
                        packet.writeData(dataTypeIndex, inputStream.readDouble());
                        break;
                    case INTEGER:
                        packet.writeData(dataTypeIndex, inputStream.readInt());
                        break;
                    case SHORT:
                        packet.writeData(dataTypeIndex, inputStream.readShort());
                        break;
                    case BYTE:
                        packet.writeData(dataTypeIndex, inputStream.readByte());
                        break;
                }
            }
            // Add last packet
            if (packet != null) {packetsReceived.add(packet);}
        } catch (IOException e) {
            System.out.println("Failed to receive packets with inputStream in ServerSocketHandler.receivePackets()!");
            EngineCalculator.printExceptionInfo(e);
        }
        return packetsReceived;
    }
}
