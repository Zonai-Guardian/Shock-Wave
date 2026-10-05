package com.shockWave.input;

import com.studiohartman.jamepad.ControllerButton;

public class ControllerInput {
    
    public ControllerButton controllerButton;
    public boolean isUrgent = false;

    public ControllerInput(ControllerButton controllerButton) {
        this.controllerButton = controllerButton;
        this.isUrgent = false;
    }
    
    public ControllerInput(ControllerButton controllerButton, boolean isUrgent) {
        this.controllerButton = controllerButton;
        this.isUrgent = isUrgent;
    }
}
