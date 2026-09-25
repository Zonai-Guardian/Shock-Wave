package com.spiritOfEldervine.input_devices;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.Game.ControllerTypes;
import com.spiritOfEldervine.Game.Direction4;
import com.spiritOfEldervine.Game.Direction8;
import com.spiritOfEldervine.engine.EngineCalculator;
import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerUnpluggedException;

public class ConsoleController {
    //controller manager is kept in here so Controller.java can start vibrations, handle, buttons, and more.
    public ControllerManager controllerManager = new ControllerManager();
    public boolean hasBeenInitialized = false;
    public boolean controllerIsConnected = false;
    public int maxTimeSinceConnectionChange = EngineCalculator.secondsToFrames(3);
    public int timeSinceConnectionChange = 0;

    public String name = "unset";
    public ControllerTypes controllerType = null;

    private final int maxFramesSinceAction = EngineCalculator.secondsToFrames(15); // 15 seconds
    private final int updateInterval = EngineCalculator.secondsToFrames(5); // 5 seconds
    private int framesSinceUpdate = 0;

    //deadzones and sensitivity
    private double joystickMoveDeadzone = 0.2; //For when moving the player in game. The minimum amount that the joystick must be moved to reginster as moved. Should be between 0.01 and 0.4
    private double joystickSelectDeadzone = 0.4; //For when selecting GComponents in menus. The minimum amount that the joystick must be moved to reginster as moved. Should be between 0.01 and 0.4
    private double joystickSensitivity = 1.0; //This number is multiplied with the double that the joystick is moved and that number is what the cursor will be moved across the screen per frame.

    //joystick movement ranges from -1.0 to 1.0 with 0.0 being no movement.
    private Point2D.Double leftJoystick = new Point2D.Double(0.0, 0.0);
    private Point2D.Double rightJoystick = new Point2D.Double(0.0, 0.0);

    private Direction4 leftJoystickZone = Direction4.CENTER;
    private Direction4 rightJoystickZone = Direction4.CENTER;

    private int maxFramesInJoystickZone = EngineCalculator.secondsToFrames(60 * 5); // 5 Minutes
    private int framesInLeftJoystickZone = 0;
    private int framesInRightJoystickZone = 0;

    private ArrayList<ControllerButton> heldButtons = new ArrayList<>();
    private Map<ControllerButton, Integer> framesSinceButtonPress = new HashMap<ControllerButton, Integer>();
    private Map<ControllerButton, Integer> framesSinceButtonRelease = new HashMap<ControllerButton, Integer>();

    //gcomponent selection
    private Point selectionPoint = new Point(0, 0);


    // Starters
    public ConsoleController() {}
    public void init() {
        controllerManager.initSDLGamepad();
        controllerManager.update();
        hasBeenInitialized = true;
    }


    // Updaters
    public void update(Game game) {
        if (hasBeenInitialized == false) {return;} //prevent using controller before controllerManager has been initialized
        framesSinceUpdate++;
        if (framesSinceUpdate >= updateInterval) {
            updateConnection();
            framesSinceUpdate = 0;
        }
        updateTimers();
        if (controllerIsConnected) {
            checkInput(game);
        }
    }
    private void updateConnection() {
        controllerManager.update();
        ControllerIndex index = controllerManager.getControllerIndex(0);
        if (index.isConnected() != controllerIsConnected) {
            timeSinceConnectionChange = 0; //reset timer
            controllerIsConnected = index.isConnected();
        }
        if (controllerIsConnected) { //Only try to get the controller name if one is connected
            try {
                name = index.getName();
            } catch (ControllerUnpluggedException e) {
                name = "unplugged";
            }
            if (name.toLowerCase().contains("sony") || (name.toLowerCase().contains("play") && name.toLowerCase().contains("station"))) { //Soney/Play Station Brand
                controllerType = ControllerTypes.PLAY_STATION;
            } else if (name.toLowerCase().contains("xbox") || name.toLowerCase().contains("x-box")) {
                controllerType = ControllerTypes.XBOX;
            } else if (name.toLowerCase().contains("nintendo")) {
                controllerType = ControllerTypes.NINTENDO;
            } else {
                System.out.println("Controller Name \"" + name + "\" does not match any major brand!");
                controllerType = null;
            }
        }
    }
    private void updateTimers() {
        //update timers
        timeSinceConnectionChange += timeSinceConnectionChange < maxTimeSinceConnectionChange ? 1 : 0; //incriment timeSinceConnectChange
        for (Entry<ControllerButton, Integer> entry : framesSinceButtonPress.entrySet()) {
            if (entry.getValue() < maxFramesSinceAction) {entry.setValue(entry.getValue() + 1);}
        }
        for (Entry<ControllerButton, Integer> entry : framesSinceButtonRelease.entrySet()) {
            if (entry.getValue() < maxFramesSinceAction) {entry.setValue(entry.getValue() + 1);}
        }
        if (framesInLeftJoystickZone < maxFramesInJoystickZone) {framesInLeftJoystickZone++;}
        if (framesInRightJoystickZone < maxFramesInJoystickZone) {framesInRightJoystickZone++;}
        if (framesInLeftJoystickZone > EngineCalculator.secondsToFrames(0.75) && framesInLeftJoystickZone % (EngineCalculator.secondsToFrames(0.1) == 0 ? 1 : EngineCalculator.secondsToFrames(0.1)) == 0) {
            selectionPoint = Game.menuManager.handleMenuControllerMove(selectionPoint, leftJoystickZone);
        }
    }
    private void checkInput(Game game) { //this is actually an update method
        //handle new button presses and releases
        ControllerIndex controller = controllerManager.getControllerIndex(0);
        if (controller.isConnected()) {
            /* Check if the A button (Xbox), X button (Nintendo), or Cross button (PlayStation) is pressed
            if (controller.isButtonPressed(ControllerButton.A)) {
                System.out.println("Jump action!");
            } */
            try {
                for (ControllerButton button : ControllerButton.values()) {
                    if (controller.isButtonPressed(button)) {
                        Game.controller.handleButtonPress(button, game);
                    } else {
                        Game.controller.handleButtonRelease(button, game);
                    }
                }
                setLeftJoystick(
                    controller.getAxisState(ControllerAxis.LEFTX),
                    controller.getAxisState(ControllerAxis.LEFTY)
                );
                setRightJoystick(
                    controller.getAxisState(ControllerAxis.RIGHTX),
                    controller.getAxisState(ControllerAxis.RIGHTY)
                );
            } catch (ControllerUnpluggedException e) {
                System.out.println("Controller is unplugged in ConsoleController.update()!");
                e.printStackTrace();
            }
        }
    }


    // Handlers
    public void handleButtonPress(ControllerButton button, Game game) {
        if (heldButtons.contains(button) == false) {
            heldButtons.add(button);
            framesSinceButtonPress.put(button, 0); //reset timer

            game.handleButtonPress(button, true); // handle action activation
        }
    }
    public void handleButtonRelease(ControllerButton button, Game game) {
        if (heldButtons.contains(button)) {
            heldButtons.remove(button);
            framesSinceButtonRelease.put(button, 0);
            
            game.handleButtonPress(button, false); // handle action activation
        } 
    }


    // Getters
    public Point2D.Double getLeftMoveJoystick() {
        return getLimitedJoystick(leftJoystick, joystickMoveDeadzone);
    }
    public Point2D.Double getRightMoveJoystick() {
        return getLimitedJoystick(rightJoystick, joystickMoveDeadzone);
    }
    public Point2D.Double getLeftSelectJoystick() {
        return getLimitedJoystick(leftJoystick, joystickSelectDeadzone);
    }
    public Point2D.Double getRightSelectJoystick() {
        return getLimitedJoystick(rightJoystick, joystickSelectDeadzone);
    }
    private Point2D.Double getLimitedJoystick(Point2D.Double joystick, double limit) {
        if (EngineCalculator.getDistanceBetweenPoints(new Point2D.Double(0, 0), joystick) < limit) {
            return new Point2D.Double(0, 0);
        } else {
            return new Point2D.Double(joystick.x, joystick.y);
        }
    }
    private boolean isJoystickPastLimit(Point2D.Double joystick, double limit) {
        if (EngineCalculator.getDistanceBetweenPoints(new Point2D.Double(0, 0), joystick) < limit) {
            return false;
        } else {
            return true;
        }
    }
    public int getFramesSinceButtonPress(ControllerButton button) {
        if (framesSinceButtonPress.containsKey(button)) {
            return framesSinceButtonPress.get(button);
        }
        return maxFramesSinceAction;
    }
    public int getFramesSinceButtonRelease(ControllerButton button) {
        if (framesSinceButtonRelease.containsKey(button)) {
            return framesSinceButtonRelease.get(button);
        }
        return maxFramesSinceAction;
    }
    public boolean buttonIsHeld(ControllerButton button) {
        return heldButtons.contains(button);
    }
    public boolean shouldChangeControlType() {
        boolean disconnected = controllerIsConnected == false; // No Controller is conneted
        boolean longSinceChange = timeSinceConnectionChange >= maxTimeSinceConnectionChange; // Hasn't been disconnected within 3 seconds

        return disconnected && longSinceChange;
    }
    public Point getSelectionPoint() {return selectionPoint;}
    public Direction4 getJoystickZone(Point2D.Double stick) {
        double max = 0;
        Direction4 direction = Direction4.CENTER;
        if (isJoystickPastLimit(stick, joystickSelectDeadzone)) {
            // Right
            if (Math.abs(stick.x) > Math.abs(max)) {
                if (stick.x < 0) {
                    direction = Direction4.LEFT;
                } else {
                    direction = Direction4.RIGHT;
                }
                max = stick.x;
            }
            if (Math.abs(stick.y) > Math.abs(max)) {
                if (stick.y < 0) {
                    direction = Direction4.BOTTOM;
                } else {
                    direction = Direction4.TOP;
                }
                max = stick.y;
            }
        }

        return direction;
    }
    public int getFramesInLeftJoystickZone() {return framesInLeftJoystickZone;}
    public int getFramesInRightJoystickZone() {return framesInRightJoystickZone;}

    // Setters
    public void setLeftJoystick(double x, double y) {
        leftJoystick.x = x;
        leftJoystick.y = y;

        if (leftJoystickZone == getJoystickZone(leftJoystick) == false) {
            framesInLeftJoystickZone = 0;
            leftJoystickZone = getJoystickZone(leftJoystick);
            selectionPoint = Game.menuManager.handleMenuControllerMove(selectionPoint, leftJoystickZone);
        }
    }
    public void setRightJoystick(double x, double y) {
        rightJoystick.x = x;
        rightJoystick.y = y;

        if (rightJoystickZone == getJoystickZone(rightJoystick) == false) {
            framesInRightJoystickZone = 0;
            rightJoystickZone = getJoystickZone(rightJoystick);
        }
    }


    //Commands
    public void startVibration(double leftMagnitude, double rightMagnitude, int milliseconds) {
        ControllerIndex index = controllerManager.getControllerIndex(0);
        try {
            index.doVibration((float)leftMagnitude, (float)rightMagnitude, milliseconds);
        } catch (ControllerUnpluggedException e) {
            //do nothing
        }
    }
}
