package com.spiritOfEldervine.networking;

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
        PacketType data = types.get(types.size() - 1);
        types.remove(types.size() - 1);
        return data;
    }
}
