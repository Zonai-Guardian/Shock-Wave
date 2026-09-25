package com.spiritOfEldervine.networking;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.engine.EngineCalculator;
import com.spiritOfEldervine.networking.PacketManager.PacketDataType;

public class PacketType {

    public byte packetDataType;
    public String stringVar = null;
    public Double doubleVar = null;
    public Integer integerVar = null;
    public Short shortVar = null;
    public Byte byteVar = null;

    public <T> PacketType(byte packetDataType, T value) {
        this.packetDataType = packetDataType;

        if (value instanceof String) {
            stringVar = (String) value;
        } else if (value instanceof Double) {
            doubleVar = (Double) value;
        } else if (value instanceof Integer) {
            integerVar = (Integer) value;
        } else if (value instanceof Short) {
            shortVar = (Short) value;
        } else if (value instanceof Byte) {
            byteVar = (Byte) value;
        } else {
            System.out.println("value's class \"" + value.getClass() + "\" was not recognized in PacketType constructor!");
            EngineCalculator.printStackTrace();
            Game.exitGame(1);
        }
    }
    public <T> PacketType(T value) {
        if (value instanceof String) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.STRING);
            stringVar = (String) value;
        } else if (value instanceof Double) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.DOUBLE);
            doubleVar = (Double) value;
        } else if (value instanceof Integer) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.INTEGER);
            integerVar = (Integer) value;
        } else if (value instanceof Short) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.SHORT);
            shortVar = (Short) value;
        } else if (value instanceof Byte) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.BYTE);
            byteVar = (Byte) value;
        } else {
            System.out.println("value's class \"" + value.getClass() + "\" was not recognized in PacketType constructor!");
            EngineCalculator.printStackTrace();
            Game.exitGame(1);
        }
    }
}
