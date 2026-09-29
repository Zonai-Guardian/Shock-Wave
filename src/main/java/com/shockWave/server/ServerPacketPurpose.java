package com.shockWave.server;

import com.shockWave.networking.PacketManager.PacketPurpose;

public class ServerPacketPurpose {
    public PacketPurpose packetPurpose; // An ordinary packet purpose
    public Integer[] targetedClientIDs; // This holds the ID of the targeted client (-1 is everyone, null caused an error)

    public ServerPacketPurpose(PacketPurpose packetPurpose, int targetedClientID) {
        this.packetPurpose = packetPurpose;
        targetedClientIDs = new Integer[]{targetedClientID};
    }

    public ServerPacketPurpose(PacketPurpose packetPurpose, Integer[] targetedClientIDs) {
        this.packetPurpose = packetPurpose;
        this.targetedClientIDs = targetedClientIDs;
    }
}
