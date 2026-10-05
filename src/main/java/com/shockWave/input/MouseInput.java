package com.shockWave.input;

import com.shockWave.Game.MouseButton;

public class MouseInput {
    
    public MouseButton mouseButton;
    public boolean isUrgent = false;

    public MouseInput(MouseButton mouseButton) {
        this.mouseButton = mouseButton;
        this.isUrgent = false;
    }
    
    public MouseInput(MouseButton mouseButton, boolean isUrgent) {
        this.mouseButton = mouseButton;
        this.isUrgent = isUrgent;
    }
}
