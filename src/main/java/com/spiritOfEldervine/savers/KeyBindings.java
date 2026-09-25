package com.spiritOfEldervine.savers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.Game.ControlType;
import com.spiritOfEldervine.Game.MouseButton;
import com.studiohartman.jamepad.ControllerButton;

public class KeyBindings {
    public Map<String, ArrayList<String>> shiftStrings = loadShiftStrings();

    public Map<String, String> keyboardBindings = new HashMap<String, String>(); // (gameAction, keyCode)
    public Map<String, MouseButton> mouseBindings = new HashMap<String, MouseButton>();
    public Map<String, ControllerButton> controllerBindings = new HashMap<String, ControllerButton>();

    //Controller remapping
    public Map<ControllerButton, ControllerButton> nintendoRemappings = loadNintendoRemappings();
    public Map<ControllerButton, ControllerButton> xBoxRemappings = loadXBoxRemappings();
    public Map<ControllerButton, ControllerButton> playStationRemappings = loadPlayStationRemappings();

    public KeyBindings() {}
    public void init() {
        loadRequiredKeyBindings();
    }

    public void loadDefaultKeyBinds() {
        // Keyboard Keybinds
        keyboardBindings.put("pause", "escape");
        keyboardBindings.put("freeze", "z");

        // Mouse Keybinds
        mouseBindings.put("attack", MouseButton.LEFT);
        mouseBindings.put("interact", MouseButton.RIGHT);
        //mmb does nothing right now

        // Controller Keybinds
        controllerBindings.put("pause", ControllerButton.START);
        controllerBindings.put("vibrate_up", ControllerButton.DPAD_UP);
        controllerBindings.put("vibrate_right", ControllerButton.DPAD_RIGHT);
        controllerBindings.put("vibrate_down", ControllerButton.DPAD_DOWN);
        controllerBindings.put("vibrate_left", ControllerButton.DPAD_LEFT);

        loadRequiredKeyBindings();
    }
    public void loadRequiredKeyBindings() {
        //    (Action, Button/Key)

        // Keyboard Keybinds
        keyboardBindings.put("back", "escape");
        keyboardBindings.put("accept", "enter");
        keyboardBindings.put("force_quit", "delete");
        keyboardBindings.put("copy", "c");
        keyboardBindings.put("paste", "v");

        // Mouse Keybinds
        mouseBindings.put("click", MouseButton.LEFT);

        // Controller Keybinds
        controllerBindings.put("click", ControllerButton.A);
        controllerBindings.put("back", ControllerButton.B);
    }
    public void handleButtonPress(Game game, String keyText, boolean wasPressed) {
        if (Game.controllType != ControlType.KEYBOARD_AND_MOUSE) {return;} // Do not activate actions with Keyboard if controlType is not correct

        //Keyboard key handleing
        for (Entry <String, String> e : keyboardBindings.entrySet()) {
            if (e.getValue().equals(keyText)) {
                game.activateAction(e.getKey(), wasPressed);
            }
        }
    }
    public void handleButtonPress(Game game, MouseButton button, boolean wasPressed) {
        if (Game.controllType != ControlType.KEYBOARD_AND_MOUSE) {return;} // Do not activate actions with Mouse if controlType is not correct

        //Mouse button handleing
        for (Entry <String, MouseButton> e : mouseBindings.entrySet()) {
            if (e.getValue() == button) {
                game.activateAction(e.getKey(), wasPressed);
            }
        }
    }
    public void handleButtonPress(Game game, ControllerButton button, boolean wasPressed) {
        if (Game.controllType != ControlType.CONTROLLER) {return;} // Do not activate actions with Controller if controlType is not correct
        
        //Controller button handleing
        button = remapButton(button);
        for (Entry <String, ControllerButton> e : controllerBindings.entrySet()) {
            if (e.getValue() == button) {
                game.activateAction(e.getKey(), wasPressed);
            }
        }
    }
    public void handleButtonRelease(Game game, ControllerButton button) {
        // Do nothing yet...
    }

    //public void handleButtonPress(Game game, )

    /*
    public static enum KeyboardKeys {
        ESCAPE,
        F1,
        F2,
        F3,
        F4,
        F5,
        F6,
        F7,
        F8,
        F9,
        F10,
        F11,
        F12,
        TILDE,
        N1,
        N2,
        N3,
        N4,
        N5,
        N6,
        N7,
        N8,
        N9,
        N0,
        DASH,
        EQUALS,
        BACKSPACE,
        TAB,
        CURLY_BRACKET_OPENING,
        CURLY_BRACKET_CLOSING,
        BACKSLASH,
        CAPS_LOCK,
        SEMI_COLON,
        MORE...
    } */
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
    private Map<String, ArrayList<String>> loadShiftStrings() {
        //create map
        Map<String, ArrayList<String>> shiftStrings = new HashMap<String, ArrayList<String>>();

        //fill map
        shiftStrings.put("1", toList("1", "!"));
        shiftStrings.put("2", toList("2", "@"));
        shiftStrings.put("3", toList("3", "#"));
        shiftStrings.put("4", toList("4", "$"));
        shiftStrings.put("5", toList("5", "%"));
        shiftStrings.put("6", toList("6", "^"));
        shiftStrings.put("7", toList("7", "&"));
        shiftStrings.put("8", toList("8", "*"));
        shiftStrings.put("9", toList("9", "("));
        shiftStrings.put("0", toList("0", ")"));
        shiftStrings.put("comma", toList(",", "<"));
        shiftStrings.put("period", toList(".", ">"));
        shiftStrings.put("slash", toList("/", "?"));
        shiftStrings.put("equals", toList("=", "+"));
        shiftStrings.put("quote", toList("\\", "|"));
        shiftStrings.put("open bracket", toList("[", "{"));
        shiftStrings.put("close bracket", toList("]", "}"));
        shiftStrings.put("back slash", toList("/", "?"));
        shiftStrings.put("comma", toList(",", "<"));
        shiftStrings.put("enter", toList("\n", "\n"));
        shiftStrings.put("space", toList(" ", " "));
        shiftStrings.put("tab", toList("    ", "    "));

        //return map
        return shiftStrings;
    }
    public ArrayList<String> toList(String s1, String s2) {
        ArrayList<String> list = new ArrayList<>();
        list.add(s1);
        list.add(s2);

        return list;
    }
}
