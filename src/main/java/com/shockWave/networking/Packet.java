package com.shockWave.networking;

import java.net.InetAddress;
import java.util.ArrayList;

public class Packet {
    public short packetPurpose;
    public ArrayList<PacketType> types = new ArrayList<>();

    public int toFromID;
    
    public Packet(short packetPurpose, int toFromID) {
        this.packetPurpose = packetPurpose;
        this.toFromID = toFromID;
    }

    public <T> void writeData(T type) {
        types.add(new PacketType(type));
    }
    public <T> void writeData(byte packetDataType, T type) {
        types.add(new PacketType(packetDataType, type));
    }
    public PacketType readData() {
        int index = 0;
        PacketType data = types.get(index);
        types.remove(index);
        return data;
    }

    @Override 
    public String toString() {
        String string = "Packet{packetPurpose=" + packetPurpose + ", toFromID=" + toFromID + ", types=[";
        for (PacketType type : types) {
            string += "\nPacketType[" + type.toMinimalString() + "]";
        }
        string += "\n}";
        return string;
    }
}
