package com.shockWave.input;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JFrame;

import com.shockWave.Game;
import com.shockWave.Game.ControlType;
import com.shockWave.Game.MouseButton;

public class Mouse {
    //this holds variables that are related to the mouse
    private Point cursorCoordinates = new Point(0, 0); //based on gameResolution
    public ArrayList<MouseButton> heldButtons = new ArrayList<>(); //can hold "rmb", "lmb", "mmb"
    
    public Mouse() {}
    
    public void setupButtonHandleing(Game game, JFrame frame) {
        // Add mouse listener here
        frame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                //handle pressing
                setCursorCoords(e.getX(), e.getY());
                
                handleButtonPress(game, getButton(e));
            }
            public void mouseReleased(MouseEvent e) {
                //haldle releasing
                setCursorCoords(e.getX(), e.getY());
                handleButtonRelease(game, getButton(e));
            }
        });
        //handles mouse wheele scrolling
        frame.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                //handle what to do when the mouse wheel is scrolled
            }
        });
        //handles mouse moving and dragging
        frame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Track mouse position for hover effects
                setCursorCoords(e.getX(), e.getY());
                //moveSliders(); maybe later???
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                // Handle dragging
                setCursorCoords(e.getX(), e.getY());
                //moveSliders(); maybe later???
            }
        });
    }
    //handlers
    public void handleButtonPress(Game game, MouseButton button) {
        if (heldButtons.contains(button) == false) {
            heldButtons.add(button);
            game.handleButtonPress(button, true);
        }
    }
    public void handleButtonRelease(Game game, MouseButton button) {
        if (heldButtons.contains(button)) {
            heldButtons.remove(button);
            game.handleButtonPress(button, false);
        }
    }

    //setters
    public void setCursorCoords(int x, int y) {
        if (Game.controllType != ControlType.KEYBOARD_AND_MOUSE) {return;} // Do not move cursor with mouse if controllType is not correct

        cursorCoordinates = new Point(
            (int)((x - Game.displayOffset.x - (Game.isFullScreen ? 0 : Game.getFrameInsets().left)) / Game.displayScale.x),
            (int)((y - Game.displayOffset.y - (Game.isFullScreen ? 0 : Game.getFrameInsets().top)) / Game.displayScale.y)
        );
    }
    
    //getters
    public Point getCursorDisplayCoords() {return new Point(cursorCoordinates);}
    public MouseButton getButton(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                return MouseButton.LEFT;
            case MouseEvent.BUTTON2:
                return MouseButton.MIDDLE;
            case MouseEvent.BUTTON3:
                return MouseButton.RIGHT;
            default:
                System.out.println("Unrecognized mouse button pressed: " + e.getButton());
                return null;
        }
    }
}
