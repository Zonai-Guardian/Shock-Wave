package com.shockWave.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.shockWave.Game;
import com.shockWave.Game.Direction5;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.graphics.g_components.GComponent;

public class Menu {
    // Name of menu used for all referencing. Must be the same as in menuMap in MenuManager.java!!!!
    private String name;
    // What direction the shading should be rendered at, if null then render no shading
    public Direction5 shadingDirection = null;
    // Point that the controller will have as its selection point when this menu is desplayed
    private Point startControllerSelectionPoint = new Point(0, 0); // Default is 0, 0
    //list of GComponents that will be in this Menu
    private Map<Integer, GComponent> components = new TreeMap<Integer, GComponent>();
    public Rectangle controllerSelectionRectsBounds = new Rectangle(0, 0);

    public Menu(String name) {
        this.name = name;
    }
    public void init() {
        // This method should be called after all GComponents have been added
    }

    // Loaders
    public Integer addGComponent(GComponent original) {
        GComponent gcom = original.getCopy();
        if (gcom == null) {
            System.out.println("gcom (its a GComponent) is null in Menu.addGComponent()!");
            EngineCalculator.printStackTrace();
            return null;
        }
        gcom.id = MenuManager.getGComponentID();
        components.put(gcom.id, gcom);
        return gcom.id;
    }

    // Handlers
    public void handleMouseClick(Point cc, int scrollOffset) {
        GComponent gcomp = getGComponentUnderCursor(cc, new Point(0, 0), scrollOffset);
        if (gcomp != null) {
            gcomp.activateComponent();
        }
    }
    public void handleControllerClick() {
        GComponent gcomp = getGComponenetUnderController(Game.controller.getSelectionPoint());
        if (gcomp != null) {
            gcomp.activateComponent();
        }
    }
    public Point handleControllerMove(Point selectionPoint, Direction5 direction) {
        if (direction == Direction5.CENTER) {return selectionPoint;}

        Point offset = new Point(0, 0);
        int maxTries = 0;

        switch (direction) {
            case TOP:
                offset.y = -1;
                maxTries = Math.abs(controllerSelectionRectsBounds.height);
                break;
            case BOTTOM:
                offset.y = 1;
                maxTries = Math.abs(controllerSelectionRectsBounds.height);
                break;
            case LEFT:
                offset.x = -1;
                maxTries = Math.abs(controllerSelectionRectsBounds.width);
                break;
            case RIGHT:
                offset.x = 1;
                maxTries = Math.abs(controllerSelectionRectsBounds.width);
                break;
            case CENTER:
                return selectionPoint;
        }
        return new Point(selectionPoint.x + offset.x, selectionPoint.y + offset.y);
        //return moveControllerSelectionPoint(selectionPoint, offset, maxTries);
    }
    public Point moveControllerSelectionPoint(Point sp, Point offset, int maxTries) { // sp is SelectionPoint, maxTries should be the width or height of the menu bounds rect.
        if (offset.x == 0 && offset.y == 0) {return sp;}

        Point tp = new Point(sp); // Stands for "Test Point"
        for (int i = 0; i < maxTries; i++) {
            tp.x += offset.x;
            tp.y += offset.y;
            if (isControllerSelectingGComponent(tp)) {
                return tp;
            }
        }
        return new Point(sp);
    }

    // Getters
    public String getName() {
        return name;
    }
    public GComponent getGComponentByID(int id) {
        if (components.containsKey(id)) {
            return components.get(id);
        } else {
            return null;
        }
    }
    public Collection<GComponent> getGComponentList() {
        return components.values();
    }
    public Point getStartControllerSelectionPoint() {
        return startControllerSelectionPoint;
    }
    private GComponent getGComponenetUnderController(Point sp) { // sp is for Selection Point
        for (GComponent c : getGComponentList()) {
            if (c.rect.contains(sp)) {
                return c;
            }
        }
        return null;
    }
    public GComponent getGComponentUnderCursor(Point cc, Point animationOffset, int scrollOffset) {
        for (GComponent gcomp : components.values()) {
            if (gcomp.rect == null) {continue;}
            Rectangle rect = new Rectangle(gcomp.rect.x + animationOffset.x, gcomp.rect.y + animationOffset.y + scrollOffset, gcomp.rect.width, gcomp.rect.height);
            if (rect.contains(cc)) {
                return gcomp;
            }
        }
        return null;
    }
    private boolean isControllerSelectingGComponent(Point sp) { // sp is for Selection Point
        for (GComponent c : getGComponentList()) {
            if (c.rect.contains(sp)) {
                return true;
            }
        }
        return false;
    }

    // Rendering
    public void render(Point offset, Graphics2D g) {
        for (GComponent gc : components.values()) {
            gc.render(offset, g);
        }
    }
    public void renderDebug(Point offset, Graphics2D g) {
        for (GComponent gc : components.values()) {
            gc.renderDebug(offset, g);
        }
    }
}
