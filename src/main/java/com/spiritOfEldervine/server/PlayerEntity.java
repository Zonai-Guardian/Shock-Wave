package com.spiritOfEldervine.server;

import java.awt.Color;

public class PlayerEntity extends Entity {
    public Color playerColor;
    public int linkedPlayerID;
    
    public PlayerEntity(int linkedPlayerID) {
        this.linkedPlayerID = linkedPlayerID;
    }
}
