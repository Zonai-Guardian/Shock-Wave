package com.shockWave.client.players;

import com.shockWave.Game.Direction4;

public class CPlayer { // This is the player that this client controls. There will be one of these in each Client
    
    public String displayName;
    public short id;
    public double posX;
    public double posY;
    public double velocityX = 0.0;
    public double velocityY = 0.0;
    public Direction4 direction = Direction4.SOUTH;

    public CPlayer(String displayName, short id, int posX, int posY) {
        this.displayName = displayName;
        this.id = id;
        this.posX = posX;
        this.posY = posY;
    }
}
