package com.shockWave.input;

public class KeyboardInput {
    
    public String keyboardKey;
    public boolean isUrgent = false;

    public KeyboardInput(String keyboardKey) {
        this.keyboardKey = keyboardKey;
        this.isUrgent = false;
    }
    
    public KeyboardInput(String keyboardKey, boolean isUrgent) {
        this.keyboardKey = keyboardKey;
        this.isUrgent = isUrgent;
    }
}
