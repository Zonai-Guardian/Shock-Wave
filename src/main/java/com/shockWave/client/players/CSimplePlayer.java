package com.shockWave.client.players;

import com.shockWave.Game.Direction4;
import com.shockWave.Game.Direction5;
import com.shockWave.engine.EngineCalculator;

public class CSimplePlayer { // This is a player that that this client does not control

    public String displayName;
    public short id;
    public int posX;
    public int posY;
    public int velocityX = 0;
    public int velocityY = 0;
    public Direction4 direction;
    public final float colorFloat;

    // The position and velocity variables would usually be doubles but they are ints here because this does not need to be very precise.

    public CSimplePlayer(short id, String displayName, float colorFloat, int posX, int posY, Direction4 direction) {
        this.displayName = displayName;
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
        this.colorFloat = colorFloat;
    }
    public void interpolateMovement() {
        posX += velocityX;
        posY += velocityY;
    }
}
