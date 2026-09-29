package com.shockWave.networking;

import com.shockWave.Game;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.networking.PacketManager.PacketDataType;

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
        }
        if (value instanceof Double) {
            doubleVar = (Double) value;
        }
        if (value instanceof Integer) {
            integerVar = (Integer) value;
        }
        if (value instanceof Short) {
            shortVar = (Short) value;
        }
        if (value instanceof Byte) {
            byteVar = (Byte) value;
        }
        //if (false) {
        //    System.out.println("value's class \"" + value.getClass() + "\" was not recognized in PacketType constructor!");
        //    EngineCalculator.printStackTrace();
        //    Game.exitGame(1);
        //}
    }
    public <T> PacketType(T value) {
        if (value instanceof String) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.STRING);
            stringVar = (String) value;
        }
        if (value instanceof Double) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.DOUBLE);
            doubleVar = (Double)(double) value;
        }
        if (value instanceof Integer) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.INTEGER);
            integerVar = (Integer)(int) value;
        }
        if (value instanceof Short) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.SHORT);
            shortVar = (Short)(short) value;
        }
        if (value instanceof Byte) {
            packetDataType = EngineCalculator.enumToByte(PacketDataType.class, PacketDataType.BYTE);
            byteVar = (Byte)(byte) value;
        }
        System.out.println("PacketDataType from PacketType constructor: " + packetDataType);
        //printData();
        //if (false) {
        //    System.out.println("value's class \"" + value.getClass() + "\" was not recognized in PacketType constructor!");
        //    EngineCalculator.printStackTrace();
        //    Game.exitGame(1);
        //}
    }
    public void printData() {
        System.out.println("Printing PacketType data in PacketType.printData()...\nData : stringVar: " + stringVar + "\nData : doubleVar: " + doubleVar + "\nData : integerVar: " + integerVar + "\nData : shortVar: " + shortVar + "\nData : byteVar: " + byteVar);
    }
}
