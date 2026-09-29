package com.shockWave.graphics.g_components;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shockWave.Game;
import com.shockWave.Game.Direction5;
import com.shockWave.Game.Direction8;
import com.shockWave.engine.EngineCalculator;

public class GComponent implements Cloneable{
    //GComponent stands for Game Component
    //this class should be extended by several different classes/objects to have buttons, sliders, text boxes, etc.
    //This is pretty much a copy of java's JComponent but I can have more control over this.

    public static enum ActivationType {
        CODE, // runs the runnable
        BACK, // goes back in the menu path if possible/applicable
        CHANGE_MENU, // removes the current menu and adds the new menu
        GO_TO_MENU, // goes foreward to another menu
        OPEN_LINK, // opens a link in a browser
        TARGET, // Set this GComponent as targeted (currently only works for GTextFields)
        EXIT, // stops the program (use carefully!)
        NONE
    }
    public static enum GComponentSizes { // I need to add a size for every GComponent type
        SMALL(
            new Dimension(100, 60),
            new Dimension(200, 60)
        ),
        MEDIUM(
            new Dimension(150, 60),
            new Dimension(400, 60)
        ),
        LARGE(
            new Dimension(200, 60),
            new Dimension(800, 60)
        ),
        X_LARGE(
            new Dimension(250, 60),
            new Dimension(1000, 60)
        );

        public Dimension buttonSize;
        public Dimension textFieldSize;

        private GComponentSizes(Dimension buttonSize, Dimension textFieldSize) {
            this.buttonSize = buttonSize;
            this.textFieldSize = textFieldSize;
        }
    }
    public static enum ComponentStyle {SIMPLE, SETTINGS}

    // for creating GComponents
    private Point creationPosition;
    private GComponentSizes creationSize;
    private Direction8 creationDirection;

    public Direction5 animationDirection = null; // leaves null for default direction (right to left)

    public Rectangle rect = new Rectangle(0, 0, 100, 100); // Best to have defaults. Less errors
    public Rectangle controllerSelectionRect = new Rectangle(0, 0, 1, 1); // This is used for controller selection. It is never displayed
    public String displayText = ""; // Not used in every GComponent class
    public String tooltip = ""; // Not used in all instances of components
    public int id;
    public ComponentStyle componentStyle = ComponentStyle.SIMPLE;
    public Direction8 displayTextDirection = Direction8.CENTER_CENTER;
    public ActivationType activationType = ActivationType.NONE;

    public Runnable updateMethod = () -> {}; // Not always used
    public Runnable actionMethod = () -> {}; // CODE
                                             // BACK
    public String actionValue = null;        // MENU Or OPEN_LINK  (actionValue is the menu that the user will be taken to when the GComponent is activated)
                                             // TARGET
                                             // NONE

    public ArrayList<String> dataArray = new ArrayList<>(); // this is an extra place for data that the runnable needs to access.
    
    public boolean selected = false; // This is true when either the mouse is hovering over it or the Controller selection is on it
    public boolean scrolls = false; // Whether this GComponent is affected when the user scrolls down a menu (Vertical is the only direction)


    // Setters & Creation Methods
    public void setCommons(String displayText, String tooltip) {
        this.displayText = displayText;
        this.tooltip = tooltip;
    }
    public void setDisplayRect(Point position, GComponentSizes size, Direction8 positionDirection) {
        creationPosition = position;
        creationSize = size;
        creationDirection = positionDirection;

        Dimension sizeDimension = size.buttonSize;
        if (this instanceof GButton) {
            sizeDimension = size.buttonSize;
        } else if (this instanceof GTextField) {
            sizeDimension = size.textFieldSize;
        }

        rect = calculateRect(position, sizeDimension, positionDirection);
    }
    public void changeDisplayRect(Point offset) {
        setDisplayRect(new Point(creationPosition.x + offset.x, creationPosition.y + offset.y), creationSize, creationDirection);
    }
    public void setControllerSelectionRect(Rectangle rect) {
        controllerSelectionRect = rect;
    }
    public Rectangle calculateRect(Point point, Dimension size, Direction8 direction) {
        Point offset = new Point(0, 0);
        switch(direction) {
            case CENTER_CENTER:
                offset = new Point(-size.width / 2, -size.height / 2);
                break;
            case RIGHT_CENTER:
                offset = new Point(-size.width, -size.height / 2);
                break;
            case RIGHT_BOTTOM:
                offset = new Point(-size.width, -size.height);
                break;
            case CENTER_BOTTOM:
                offset = new Point(-size.width / 2, -size.height);
                break;
            case LEFT_BOTTOM:
                offset = new Point(0, -size.height);
                break;
            case LEFT_CENTER:
                offset = new Point(0, -size.height / 2);
                break;
            case LEFT_TOP:
                offset = new Point(0, 0);
                break;
            case CENTER_TOP:
                offset = new Point(-size.width / 2, 0);
                break;
            case RIGHT_TOP:
                offset = new Point(-size.width, 0);
                break;
        }
        return new Rectangle(point.x + offset.x, point.y + offset.y, size.width, size.height);
    }

    // Updaters
    public void updateTick() {}
    public void updateComponent() {}
    public GComponent getCopy() {
        //System.out.println("GComponent.getCopy() fell through to GComponent and was not extended by another type of GComponent!");
        //EngineCalculator.printStackTrace();
        try {
            return (GComponent) this.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Couldn't clone this in GComponent.getCopy()");
            EngineCalculator.printStackTrace();
            return null;
        }
    }
    // Activators
    public void activateComponent() {
        switch (activationType) {
            case CODE:
                actionMethod.run();
                break;
            case BACK:
                Game.menuManager.goBackPath(EngineCalculator.getOppositeDirection(animationDirection));
                break;
            case CHANGE_MENU:
                Game.menuManager.changePath(actionValue, animationDirection);
                break;
            case GO_TO_MENU:
                Game.menuManager.addToPath(actionValue, animationDirection);
                break;
            case OPEN_LINK:
                // If opening a link is supported, do so
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI(actionValue));
                        System.out.println("Successfully opened link: " + actionValue);
                    } catch (URISyntaxException | IOException e) {
                        System.out.println("Failed to open link in browser in GComponent.activateComponent()-OPEN_LINK! Link/URL: " + actionValue);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Desktop is not supported when trying to open link in brower in GComponent.activateComponent()-OPEN_LINK! Link/URL: " + actionValue);
                }
                break;
            case TARGET:
                if (this instanceof GTextField) {
                    Game.menuManager.textManager.retargetTextField(id);
                }
                break;
            case EXIT:
                Game.exitGame(0);
                break;
            case NONE:
                // Do Nothing...
                break;
        }
    }
    public void render(Point offset, Graphics2D g) {
        //render will do nothing in GComponent and must be overridden
        System.out.println("GComponent.render() was called!\nThat probabley means that render() was called on an object that extends GComponent but does not override render()!");
    }
    public void renderDebug(Point offset, Graphics2D g) {
        //render will do nothing in GComponent and must be overridden
        System.out.println("GComponent.renderDebug() was called!\nThat probabley means that renderDebug() was called on an object that extends GComponent but does not override renderDebug()!");
    }
}
