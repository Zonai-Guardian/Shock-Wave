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
    }
    public PacketType(PacketType pt) {
        this.packetDataType = pt.packetDataType;
        this.stringVar = pt.stringVar;
        this.doubleVar = pt.doubleVar;
        this.integerVar = pt.integerVar;
        this.shortVar = pt.shortVar;
        this.byteVar = pt.byteVar;
    }
    private String getDataAsString() {
        return "stringVar=" + stringVar + ", doubleVar=" + doubleVar + ", integerVar=" + integerVar + ", shortVar=" + shortVar + ", byteVar=" + byteVar;
    }
    private String getMinimalDataAsString() {
        String string = "";
        string += stringVar == null ? "" : "stringVar=" + stringVar;
        string += doubleVar == null ? "" : "doubleVar=" + doubleVar;
        string += integerVar == null ? "" : "integerVar=" + integerVar;
        string += shortVar == null ? "" : "shortVar=" + shortVar;
        string += byteVar == null ? "" : "byteVar=" + byteVar;
        string += string.isEmpty() ? "ALL_ARE_NULL!" : "";
        return string;
    }
    public void printData() {
        System.out.println("Printing PacketType data in PacketType.printData()...\nData : " + getDataAsString());
    }
    @Override
    public String toString() {
        return getDataAsString();
    }
    public String toMinimalString() {
        return getMinimalDataAsString();
    }
}
