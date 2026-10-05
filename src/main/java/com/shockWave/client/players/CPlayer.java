package com.shockWave.client.players;

import com.shockWave.Game;
import com.shockWave.Game.Direction4;
import com.shockWave.engine.EngineCalculator;

public class CPlayer { // This is the player that this client controls. There will be one of these in each Client
    
    public String displayName;
    public short id;
    public double posX;
    public double posY;
    public double velocityX = 0.0;
    public double velocityY = 0.0;
    public Direction4 direction = Direction4.SOUTH;
    public final float colorFloat;

    public CPlayer(String displayName, short id, float colorFloat, int posX, int posY) {
        this.displayName = displayName;
        this.id = id;
        this.colorFloat = colorFloat;
        this.posX = posX;
        this.posY = posY;
    }
    public void update() {
        if (((Game.keyBindings.actionsHeld.contains("move_north") == false) && (Game.keyBindings.actionsHeld.contains("move_south") == false)) || (Game.keyBindings.actionsHeld.contains("move_north") && Game.keyBindings.actionsHeld.contains("move_south"))) {
            velocityY = 0.0;
        }
        if (((Game.keyBindings.actionsHeld.contains("move_east") == false) && (Game.keyBindings.actionsHeld.contains("move_west") == false)) || (Game.keyBindings.actionsHeld.contains("move_east") && Game.keyBindings.actionsHeld.contains("move_west"))) {
            velocityX = 0.0;
        }
    }
    public void testMove(Direction4 direction, double delta) {
        double testSpeed = 5.0;

        switch(direction) {
            case SOUTH:
                posY += testSpeed * delta;
                velocityY = testSpeed;
                break;
            case NORTH:
                posY -= testSpeed * delta;
                velocityY = -testSpeed;
                break;
            case EAST:
                posX += testSpeed * delta;
                velocityX = testSpeed;
                break;
            case WEST:
                posX -= testSpeed * delta;
                velocityX = -testSpeed;
                break;
        }

        // Confine player to screen, ish
        posX = Math.max(0, Math.min(Game.gameResolution.width, posX));
        posY = Math.max(0, Math.min(Game.gameResolution.height, posY));
    }
}
