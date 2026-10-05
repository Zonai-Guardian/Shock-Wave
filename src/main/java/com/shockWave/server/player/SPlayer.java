package com.shockWave.server.player;

import com.shockWave.Game.Direction4;
import com.shockWave.engine.EngineCalculator;

public class SPlayer {
    public String displayName;
    public short id;
    public int posX;
    public int posY;
    public int velocityX = 0;
    public int velocityY = 0;
    public Direction4 direction;
    public final float colorFloat = EngineCalculator.randomRange(0.0, 1.0).floatValue();
    public volatile boolean updatedRecently = false;

    // The position and velocity variables would usually be doubles but they are ints here because this does not need to be very precise.

    public SPlayer(String displayName, short id, int posX, int posY, Direction4 direction) {
        this.displayName = displayName;
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
    }
}
