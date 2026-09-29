package com.shockWave.input;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.shockWave.Game;


public class Keyboard {
    // keysToIgnore has keys that should not be tracked because they don't work properly or don't (or shouldn't) have any purpose. This also prevents players from binding actions to these keys.
    //   Windows Key : Gets stuck in the held keys list (also it's a system function button and should not be tracked or used.)
    public ArrayList<String> keysToIgnore = new ArrayList<>(List.of("windows"));
    public ArrayList<String> keysForTyping = new ArrayList<>(List.of("backspace", "left", "right")); // this allows for these keys to be rapidly activated when held and be handled differently.

    public boolean isBindingKey = false;
    public String bindingAction = null;
    public ArrayList<String> heldKeys = new ArrayList<>();

    public Keyboard() {}

    public void setupKeyHandleing(Game game) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    handleKeyPress(game, KeyEvent.getKeyText(e.getKeyCode()).toLowerCase());
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    handleKeyRelease(KeyEvent.getKeyText(e.getKeyCode()).toLowerCase(), game);
                } else if (e.getID() == KeyEvent.KEY_TYPED) {
                    String text = "" + e.getKeyChar();
                    if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {text = KeyEvent.getKeyText(e.getKeyCode());}

                    Game.menuManager.textManager.handleKey(text);
                }
                return false; // allow event to propagate
            }
        });
    }

    //handle keys
    public void handleKeyPress(Game game, String code) {
        if (keysToIgnore.contains(code)) {return;}
        if (heldKeys.contains(code) == false) {
            heldKeys.add(code);
            game.handleButtonPress(code, true);
        }
        if (keysForTyping.contains(code)) {
            Game.menuManager.textManager.handleKey(code);
        }
    }
    public void handleKeyRelease(String code, Game game) {
        if (heldKeys.contains(code)) {
            heldKeys.remove(code);
            game.handleButtonPress(code, false);
        }
    }
}
