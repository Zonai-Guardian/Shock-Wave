package com.shockWave.savers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.shockWave.Game;
import com.shockWave.Game.ControlType;
import com.shockWave.Game.MouseButton;
import com.shockWave.input.ControllerInput;
import com.shockWave.input.KeyboardInput;
import com.shockWave.input.MouseInput;
import com.studiohartman.jamepad.ControllerButton;

public class KeyBindings {

    public Map<String, KeyboardInput> keyboardBindings = new HashMap<String, KeyboardInput>(); // (gameAction, keyCode)
    public Map<String, MouseInput> mouseBindings = new HashMap<String, MouseInput>();
    public Map<String, ControllerInput> controllerBindings = new HashMap<String, ControllerInput>();

    //Controller remapping
    public Map<ControllerButton, ControllerButton> nintendoRemappings = loadNintendoRemappings();
    public Map<ControllerButton, ControllerButton> xBoxRemappings = loadXBoxRemappings();
    public Map<ControllerButton, ControllerButton> playStationRemappings = loadPlayStationRemappings();

    public ArrayList<String> actionsHeld = new ArrayList<>();

    public KeyBindings() {}
    public void init() {
        loadRequiredKeyBindings();
    }

    public void loadDefaultKeyBinds() {
        // Keyboard Keybinds
        keyboardBindings.put("pause", new KeyboardInput("escape"));
        keyboardBindings.put("freeze", new KeyboardInput("z", true));
        keyboardBindings.put("notification", new KeyboardInput("x"));

        keyboardBindings.put("move_south", new KeyboardInput("s"));
        keyboardBindings.put("move_west", new KeyboardInput("a"));
        keyboardBindings.put("move_north", new KeyboardInput("w"));
        keyboardBindings.put("move_east", new KeyboardInput("d"));

        // Mouse Keybinds
        mouseBindings.put("attack", new MouseInput(MouseButton.LEFT));
        mouseBindings.put("interact", new MouseInput(MouseButton.RIGHT));
        //mmb does nothing right now

        // Controller Keybinds
        controllerBindings.put("pause", new ControllerInput(ControllerButton.START));
        controllerBindings.put("vibrate_up", new ControllerInput(ControllerButton.DPAD_UP));
        controllerBindings.put("vibrate_right", new ControllerInput(ControllerButton.DPAD_RIGHT));
        controllerBindings.put("vibrate_down", new ControllerInput(ControllerButton.DPAD_DOWN));
        controllerBindings.put("vibrate_left", new ControllerInput(ControllerButton.DPAD_LEFT));

        loadRequiredKeyBindings();
    }
    public void loadRequiredKeyBindings() {
        //    (Action, Button/Key)

        // Keyboard Keybinds
        keyboardBindings.put("back", new KeyboardInput("escape"));
        keyboardBindings.put("accept", new KeyboardInput("enter"));
        keyboardBindings.put("force_quit", new KeyboardInput("delete", true));
        keyboardBindings.put("copy", new KeyboardInput("c", true));
        keyboardBindings.put("paste", new KeyboardInput("v", true));

        // Mouse Keybinds
        mouseBindings.put("click", new MouseInput(MouseButton.LEFT));

        // Controller Keybinds
        controllerBindings.put("click", new ControllerInput(ControllerButton.A));
        controllerBindings.put("back", new ControllerInput(ControllerButton.B));
    }
    public void handleButtonPress(Game game, String keyText, boolean wasPressed) {
        if (Game.controllType != ControlType.KEYBOARD_AND_MOUSE) {return;} // Do not activate actions with Keyboard if controlType is not correct

        //Keyboard key handleing
        for (Entry <String, KeyboardInput> e : keyboardBindings.entrySet()) {
            if (e.getValue().keyboardKey.equals(keyText)) {
                handleAction(e.getKey(), wasPressed, e.getValue().isUrgent, game);
            }
        }
    }
    public void handleButtonPress(Game game, MouseButton button, boolean wasPressed) {
        if (Game.controllType != ControlType.KEYBOARD_AND_MOUSE) {return;} // Do not activate actions with Mouse if controlType is not correct

        //Mouse button handleing
        for (Entry <String, MouseInput> e : mouseBindings.entrySet()) {
            if (e.getValue().mouseButton == button) {
                handleAction(e.getKey(), wasPressed, e.getValue().isUrgent, game);
            }
        }
    }
    public void handleButtonPress(Game game, ControllerButton button, boolean wasPressed) {
        if (Game.controllType != ControlType.CONTROLLER) {return;} // Do not activate actions with Controller if controlType is not correct
        
        //Controller button handleing
        button = remapButton(button);
        for (Entry <String, ControllerInput> e : controllerBindings.entrySet()) {
            if (e.getValue().controllerButton == button) {
                handleAction(e.getKey(), wasPressed, e.getValue().isUrgent, game);
            }
        }
    }
    public void handleAction(String action, boolean wasPressed, boolean isUrgent, Game game) {
        if (wasPressed && !actionsHeld.contains(action)) {
            actionsHeld.add(action);
        } else if (!wasPressed && actionsHeld.contains(action)) {
            actionsHeld.remove(action);
        }

        if (isUrgent) {
            game.activateAction(action, wasPressed);
        } else {
            game.handleAction(action, wasPressed);
        }
    }
    private ControllerButton remapButton(ControllerButton button) {
        Map<ControllerButton, ControllerButton> remappings;

        switch(Game.controller.controllerType) {
            case NINTENDO:
                remappings = nintendoRemappings;
                break;
            case XBOX:
                remappings = xBoxRemappings;
                break;
            case PLAY_STATION:
                remappings = playStationRemappings;
                break;
            default:
                return button; //do not remap if the controller type is unknown
        }
        if (remappings.containsKey(button)) {
            return remappings.get(button); //remap button if there is a remapping for it
        } else {
            return button;  //otherwise, return the original button
        }
    }
    private Map<ControllerButton, ControllerButton> loadNintendoRemappings() {
        Map<ControllerButton, ControllerButton> remappings = new HashMap<>();

        remappings.put(ControllerButton.A, ControllerButton.B);
        remappings.put(ControllerButton.B, ControllerButton.A);
        remappings.put(ControllerButton.X, ControllerButton.Y);
        remappings.put(ControllerButton.Y, ControllerButton.X);

        return remappings;
    }
    private Map<ControllerButton, ControllerButton> loadXBoxRemappings() {
        Map<ControllerButton, ControllerButton> remappings = new HashMap<>();
        //no remappings
        return remappings;
    }
    private Map<ControllerButton, ControllerButton> loadPlayStationRemappings() {
        Map<ControllerButton, ControllerButton> remappings = new HashMap<>();
        //no remappings
        return remappings;
    }
}
