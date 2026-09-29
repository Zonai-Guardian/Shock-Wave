package com.shockWave.client.players;

import com.shockWave.Game.Direction4;
import com.shockWave.Game.Direction5;

public class CSimplePlayer { // This is a player that that this client does not control

    public String displayName;
    public short id;
    public int posX;
    public int posY;
    public int velocityX = 0;
    public int velocityY = 0;
    public Direction4 direction;

    // The position and velocity variables would usually be doubles but they are ints here because this does not need to be very precise.

    public CSimplePlayer(String displayName, short id, int posX, int posY, Direction4 direction) {
        this.displayName = displayName;
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
    }
}
