package com.spiritOfEldervine.savers;

import java.util.ArrayList;

public class Settings {
    public static enum DebugEnum { // THE ORDER OF THIS ENUM MATTERS A LOT! The debug options at the front (top) of the list will be rendered behind and the things at the bottom of the list will be rendered in front.
        // First all of the other info should be rendered
        WHITE_OUT,
        WINDOW_RINGS,
        GCOMPONENT_BACKGROUNDS,
        CONTROLLER_INFO,
        KEYBOARD_INFO,
        MOUSE_INFO,
        CURSOR_INFO,
        // Then the text debug info should be rendered (will actually be rendered last)
        FPS,
        MENU_INFO,
        TYPING,
        TYPING_TEST,
        MENU_ANIMATION_INFO
    }
    private ArrayList<DebugEnum> debugList = new ArrayList<>();
    public boolean isDebugEnabled = false;

    public double textSizeMultiplier = 1.0; // This should be multiplied by the text whenever it is drawn in the GUI

    public Settings() {}
    public void init() {
        //debugList.add(DebugEnum.CURSOR_INFO);
        debugList.add(DebugEnum.MENU_INFO);
        debugList.add(DebugEnum.TYPING);
        //debugList.add(DebugEnum.TYPING_TEST);
        //debugList.add(DebugEnum.FPS);
        //debugList.add(DebugEnum.KEYBOARD_INFO);
        //debugList.add(DebugEnum.MENU_ANIMATION_INFO);
    }

    public boolean isDebugActive(DebugEnum debugKey) {
        return isDebugEnabled && debugList.contains(debugKey);
    }
    public ArrayList<DebugEnum> getDebugList() {
        return debugList;
    }
}
