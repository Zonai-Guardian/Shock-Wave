package com.shockWave.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.shockWave.Game;
import com.shockWave.Game.Direction5;
import com.shockWave.Game.Direction8;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.graphics.g_components.GButton;
import com.shockWave.graphics.g_components.GButtonTemplate;
import com.shockWave.graphics.g_components.GComponent;
import com.shockWave.graphics.g_components.GComponent.ActivationType;
import com.shockWave.graphics.g_components.GComponent.GComponentSizes;

public class MenuManager {
    // Text Manager (it manages typing-related things)
    public TextManager textManager = new TextManager();

    // Menu path
    private ArrayList<String> menuPath = new ArrayList<>(); //this will hold the path of menus that was used to get to the current menu. The current menu is menuPath.get(menuPath.size() - 1);

    /* Menu Name Prefixes:
                        [none] = menu
                        s_ = settings (important because this will make it render differently)
                        c_ = client   (important because this must be accessed at a different location or only at specific times)
                        v_ = server (might not use)
    */
    // Map of menu names and Menus
    private Map<String, Menu> menuMap = new TreeMap<String, Menu>();
    private static int menuID = 0; // This enables unique GComponent references by assigning one unique value to each GComponent
    
    // Vars for fading and animations
    private String previousMenu = "none";
    private final Direction5 defaultToAnimationDirection = Direction5.LEFT;
    private final Direction5 defaultChangeAnimationDirection = Direction5.CENTER;
    private final Direction5 defaultBackAnimationDirection = Direction5.RIGHT;
    private Direction5 animationDirection = Direction5.LEFT; //This stores the current direction that the menu is moving in
    private Point maxAnimationOffset = new Point(Game.gameResolution.width / 2, Game.gameResolution.height / 2);
    private int framesToChangeMenu = EngineCalculator.secondsToFrames(0.45); //The animation to change menus will take half a second.
    private int animationFrame = framesToChangeMenu + 2; //prevents fading at beginning and trying to access a non-existant menu

    // Vars for selecting objects (includes doing so while using a controller)
    private Integer selectedComponentID = null;
    private int maxTimeOverComponent = EngineCalculator.secondsToFrames(5);
    private int timeOverComponent = 0;


    // Starters
    public MenuManager() {
        menuPath.add("start");
    }

    
    // Updaters
    public void update() {
        updateTimers();
        updateSelections();
        updateMenus();
        textManager.updateTimers();
    }
    private void updateTimers() {
        if (timeOverComponent < maxTimeOverComponent) {timeOverComponent++;}
    }
    private void updateSelections() {
        deselectAllComponents(getActiveMenuName());
        // This method updates what components that are selected by accessing the cursor position in the mouse
        // Mouse Selection
        GComponent selectedByMouse = getMenu(getActiveMenuName()).getGComponentUnderCursor(Game.mouse.getCursorDisplayCoords(), getNextMenuAnimationOffset(), 0);
        if (selectedByMouse != null) {
            // A Component is/should be selected
            selectedByMouse.selected = true;
            if (selectedComponentID != null && selectedComponentID == selectedByMouse.id) {
                // Do Nothing
            } else {
                // It is a different/new component
                selectedComponentID = selectedByMouse.id;
                timeOverComponent = 0;
            }
        } else {
            // No component is/should be selected
            selectedComponentID = null;
            timeOverComponent = 0;
        }
    }
    
    private void deselectAllComponents(String menuName) {
        for (GComponent c : getMenu(menuName).getGComponentList()) {
            c.selected = false;
        }
    }
    private void updateMenus() {
        if (animationFrame <= framesToChangeMenu) {
            animationFrame++; //will stop when animationFrame is 1 more than framesToChangeMenu
        }
    }
    private void updateControllerSelectionRects() {
        Point x = new Point(0, 0); // min, max
        Point y = new Point(0, 0); // min, max
        for (GComponent c : getMenu(getActiveMenuName()).getGComponentList()) {
            if (c.rect.x < x.x) {
                x.x = c.rect.x;
            }
            if (c.rect.x + c.rect.width > x.y) {
                x.y = c.rect.x + c.rect.width;
            }
            if (c.rect.y < y.x) {
                y.x = c.rect.y;
            }
            if (c.rect.y + c.rect.height < y.y) {
                y.y = c.rect.y + c.rect.height;
            }
        }
        getMenu(getActiveMenuName()).controllerSelectionRectsBounds = new Rectangle(x.x, y.x, x.y - x.x, y.y - y.x);
    }

    // Handlers
    public void handleMenuMouseClick() {
        if (animationFrame <= framesToChangeMenu) {return;} // Don't do anything if switching Menus
        Point cc = Game.mouse.getCursorDisplayCoords(); // Cursor Coords
        getMenu(getActiveMenuName()).handleMouseClick(cc, 0);
    }
    public void handleMenuControllerClick() {
        if (animationFrame <= framesToChangeMenu) {return;} // Don't do anything if switching Menus
        updateControllerSelectionRects();
        getMenu(getActiveMenuName()).handleControllerClick();
    }
    public Point handleMenuControllerMove(Point originalSelectionPoint, Direction5 moveDirection) {
        return getMenu(getActiveMenuName()).handleControllerMove(originalSelectionPoint, moveDirection);
    }


    // Path Opperations
    public void addToPath(String menuName, Direction5 newAnimationDirection) {
        deselectAllComponents(getActiveMenuName());
        if (menuName.equals(getActiveMenuName()) == false) { // new menu is not the same as the active menu
            previousMenu = getActiveMenuName();
            menuPath.add(menuName);
            resetMenuVars();
            animationDirection = newAnimationDirection == null ? defaultToAnimationDirection : newAnimationDirection; //prevent direction being null by going to default
        }
    }
    public void changePath(String menuName, Direction5 newAnimationDirection) {
        deselectAllComponents(getActiveMenuName());
        if (menuName.equals(getActiveMenuName()) == false) {
            previousMenu = getActiveMenuName();
            menuPath.add(menuName);
            resetMenuVars();
            animationDirection = newAnimationDirection == null ? defaultChangeAnimationDirection : newAnimationDirection;
        }
    }
    public void goBackPath(Direction5 newAnimationDirection) {
        deselectAllComponents(getActiveMenuName());
        if (menuPath.size() > 1) {
            previousMenu = getActiveMenuName();
            menuPath.remove(menuPath.size() - 1);
            resetMenuVars();
            animationDirection = newAnimationDirection == null ? defaultBackAnimationDirection : newAnimationDirection;
        }
    }
    private void resetMenuVars() {
        // This method resets certain things when the menu is changed. I couldn't find something better to name it.
        resetAnimationFrame();

        // Cancel action/key binding

        // Stop targetting text fiels
        textManager.retargetTextField(null);

    }
    private void resetAnimationFrame() {
        if (animationFrame < framesToChangeMenu) {
            animationFrame = framesToChangeMenu - animationFrame;
        } else {
            animationFrame = 0;
        }
    }

    // Getters
    public Menu getMenu(String menuName) {
        if (menuName.startsWith("c_")) { // Client Menu
            //access at a different location or through instance of Client.java
            System.out.println("Could not find menu \"" + menuName + "\" with prefix \"c_\" due to #InDev in MenuManager.getMenu()");
            return null; //  #InDev  needs to return a menu from an instance of Client.java if gameStatus is PLAY
        } else {
            if (menuMap.containsKey(menuName)) {
                return menuMap.get(menuName);
            } else {
                System.out.println("There is not a Menu in menuMap for menuName \"" + menuName + "\" in MenuManager.getMenu()");
                EngineCalculator.printStackTrace();
                return null;
            }
        }
    }
    public String getActiveMenuName() {
        if (menuPath.size() != 0) {
            return menuPath.get(menuPath.size() - 1);
        } else {
            System.out.println("MenuPath is empyt in MenuManager.getActiveMenuName()!");
            EngineCalculator.printStackTrace();
            return "none";
        }
    }
    public static int getGComponentID() {
        return menuID++; //returns menuID and then increases it
    }
    public Integer getSelectionID() {return selectedComponentID;}
    public ArrayList<String> getListOfDebugValues(String headerCode) {
        ArrayList<String> list = new ArrayList<>();

        int max = 0;
        for (int i = 0; i < max; i++) {
            list.add(headerCode + "");
        }

        list.add(headerCode + "Menu Animation Info");
        list.add("Animation Multiplier (mult): " + getMenuAnimationMultiplier());
        list.add("maxAnimationOffse: " + EngineCalculator.toString(maxAnimationOffset));
        list.add("previousAnimationOffset: " + getPreviousMenuAnimationOffset());
        list.add("nextAnimationOffset: " + getNextMenuAnimationOffset());
        list.add("timeOverComponent: " + timeOverComponent + " / " + maxTimeOverComponent);
        list.add("animationDirection: " + animationDirection);

        return list;
    }
    public ArrayList<String> getMenuPath() {return menuPath;}
    private double getMenuAnimationMultiplier() {
        return EngineCalculator.tweenValue((double) animationFrame / (double) framesToChangeMenu, null);
        // That is smoothed animations. This is liniar animations:
        //return (double) animationFrame / (double) framesToChangeMenu;
    }

    // Loaders
    public void addMenu(Menu menu) {
        if (menu == null) {
            System.out.println("menu is null in MenuManager.addMenu()");
            EngineCalculator.printStackTrace();
            return;
        }
        menuMap.put(menu.getName(), menu);
    }

    // Rendering Getters
    public Point getPreviousMenuAnimationOffset() {
        //if (true) {return new Point(0, 0);}
        double mult = getMenuAnimationMultiplier();
        Point absoluteOffset = new Point((int)(maxAnimationOffset.x * mult), (int)(maxAnimationOffset.y * mult));
        Point offset = new Point();

        switch(animationDirection) {
            case TOP:
                offset.y = -absoluteOffset.y;
                break;
            case RIGHT:
                offset.x = absoluteOffset.x;
                break;
            case BOTTOM:
                offset.y = absoluteOffset.y;
                break;
            case LEFT:
                offset.x = -absoluteOffset.x;
                break;
            case CENTER:
                offset = new Point(0, 0);
                break;
        }
        return offset;
    }
    private Point getNextMenuAnimationOffset() {
        Point newOffset = getPreviousMenuAnimationOffset();
        switch(animationDirection) {
            case LEFT:
                newOffset.x += maxAnimationOffset.x;
                break;
            case TOP:
                newOffset.y += maxAnimationOffset.y;
                break;
            case RIGHT:
                newOffset.x -= maxAnimationOffset.x;
                break;
            case BOTTOM:
                newOffset.y += -maxAnimationOffset.y;
                break;
            //nothing for the other directions
        }
        return newOffset;
    }

    public GComponent getGComponentByID(int id, boolean onlySearchActiveMenu) {
        if (onlySearchActiveMenu) {
            return getMenu(getActiveMenuName()).getGComponentByID(id);
        } else {
            for (Entry<String, Menu> menuEntry : menuMap.entrySet()) {
                GComponent gcomp = menuEntry.getValue().getGComponentByID(id);
                if (gcomp != null) {return gcomp;}
            }
        }
        System.out.println("Could not find GComponent with id " + id + " with onlySearchActiveMenu " + onlySearchActiveMenu + " in MenuManager.getGComponentByID()! Returning null");
        return null;
    }

    // Rendering
    private void render(boolean shouldRenderDebug, Graphics2D g) {
        if (animationFrame <= framesToChangeMenu) {

            double previousMenuAlpha = 1 - getMenuAnimationMultiplier();
            double activeMenuAlpha = getMenuAnimationMultiplier();

            // Render Shading
            if (getMenu(previousMenu).shadingDirection != null) {
                RenderEngine.setGraphicsAlpha(previousMenuAlpha, g);
                renderShading(previousMenu, g);
            }
            if (getMenu(getActiveMenuName()).shadingDirection != null) {
                RenderEngine.setGraphicsAlpha(activeMenuAlpha, g);
                renderShading(getActiveMenuName(), g);
            }

            //render fading out menu
            RenderEngine.setGraphicsAlpha(previousMenuAlpha, g);
            getMenu(previousMenu).render(getPreviousMenuAnimationOffset(), g);

            //render fading in menu
            RenderEngine.setGraphicsAlpha(activeMenuAlpha, g);
            Point pointTwo = getNextMenuAnimationOffset();
            if (shouldRenderDebug) {
                getMenu(getActiveMenuName()).renderDebug(pointTwo, g);
            } else {
                getMenu(getActiveMenuName()).render(pointTwo, g);
            }

            //reset graphics alpha
            RenderEngine.setGraphicsAlpha(1.0, g);
        } else {
            //don't need to fade because it is not changing menus
            renderShading(getActiveMenuName(), g);
            if (shouldRenderDebug) {
                getMenu(getActiveMenuName()).renderDebug(new Point(0, 0), g);
            } else {
                getMenu(getActiveMenuName()).render(new Point(0, 0), g);
            }
        }
    }
    private void renderShading(String menuName, Graphics2D g) {
        boolean shouldRenderShading = false;

        if (shouldRenderShading) {
            String shadingDirection = getMenu(menuName).shadingDirection.toString();
            String imageName = "menuShading" + shadingDirection.charAt(0) + shadingDirection.substring(1).toLowerCase();
            g.drawImage(Game.images.get(imageName), 0, 0, null);
        }
    }
    public void render(Graphics2D g) {
        render(false, g);
    }
    public void renderDebug(Graphics2D g) {
        render(true, g);
    }

    public static void assembleMenuOfButtons(String menuName, GButtonTemplate[] templates, Point firstComponentPoint, Direction8 directionOfPoint, Point offsetPerComponent, GComponentSizes buttonSize, Direction8 textDirection, Direction5 shadingDirection) {
        Menu menu = new Menu(menuName);
        menu.shadingDirection = shadingDirection;
        GButton button = new GButton();
        Point nextPosition = new Point(firstComponentPoint);
        Rectangle nextControllerSelectionRect = new Rectangle(0, 0, 1, 1);
        Point controllerSelectionOffset = (offsetPerComponent.x > offsetPerComponent.y) ? new Point(0, 1) : new Point(1, 0);

        for (int i = 0; i < templates.length; i++) {
            GButtonTemplate template = templates[i];
            button.setCommons(template.name, template.tooltip);
            button.setDisplayRect(nextPosition, buttonSize, directionOfPoint);
            button.displayTextDirection = textDirection;
            button.controllerSelectionRect = new Rectangle(nextControllerSelectionRect);

            button.activationType = template.activationType;
            button.actionValue = template.actionValue;
            button.actionMethod = template.actionMethod;
            button.updateMethod = template.updateMethod;

            menu.addGComponent(button);

            nextPosition.x += offsetPerComponent.x;
            nextPosition.y += offsetPerComponent.y;
            nextControllerSelectionRect.x += controllerSelectionOffset.x;
            nextControllerSelectionRect.y += controllerSelectionOffset.y;
        }
        Game.menuManager.addMenu(menu);
    }
}
