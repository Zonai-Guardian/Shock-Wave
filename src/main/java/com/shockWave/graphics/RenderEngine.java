package com.shockWave.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import com.shockWave.Game;
import com.shockWave.Game.Direction5;
import com.shockWave.Game.Direction8;
import com.shockWave.Game.MouseButton;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.input.ConsoleController;
import com.studiohartman.jamepad.ControllerButton;

public class RenderEngine {
    private static Random random = new Random();
    public RenderEngine() {}


    // Remade default rendering methods
    public static void drawCircle(Point center, int radius, Graphics g) {
        Point realCenter = new Point(center.x - radius / 2, center.y - radius / 2);
        g.drawArc(realCenter.x, realCenter.y, radius, radius, 0, 360);
    }
    public static void fillCircle(Point center, int radius, Graphics g) {
        Point realCenter = new Point(center.x - radius / 2, center.y - radius / 2);
        g.fillArc(realCenter.x, realCenter.y, radius, radius, 0, 360);
    }
    public static void drawTextInRect(Rectangle or /*Original Rect */, Point border, String displayText, Direction8 direction, boolean enabledDebug, Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        Rectangle tr = fm.getStringBounds(displayText, g).getBounds(); // text rect   // Always accessible
        Rectangle nr = new Rectangle(tr);  // change-able
        Point xy = new Point(1, 1); // each axis can range from 1 to 3
        /*
           11 21 31
           12 22 32
           13 23 33
        */
        nr.x = (int) or.getCenterX() - tr.width / 2;
        nr.y = (int) or.getCenterY() - tr.height / 2;
        nr.x += border.x;
        nr.y += border.y;
        nr.width -= border.x * 2;
        nr.height -= border.y * 2;
        
        switch (direction) {
            case LEFT_TOP:
                xy = new Point(1, 1);
                break;
            case CENTER_TOP:
                xy = new Point(2, 1);
                break;
            case RIGHT_TOP:
                xy = new Point(3, 1);
                break;
            case LEFT_CENTER:
                xy = new Point(1, 2);
                break;
            case CENTER_CENTER:
                xy = new Point(2, 2);
                break;
            case RIGHT_CENTER:
                xy = new Point(3, 2);
                break;
            case LEFT_BOTTOM:
                xy = new Point(1, 3);
                break;
            case CENTER_BOTTOM:
                xy = new Point(2, 3);
                break;
            case RIGHT_BOTTOM:
                xy = new Point(3, 3);
                break;
        }
        switch (xy.x) {
            case 1:
                nr.x = or.x;
                break;
            case 2:
                nr.x = (int) or.getCenterX() - tr.width / 2;
                break;
            case 3:
                nr.x = (int) or.x + or.width - tr.width;
                break;
        }
        switch (xy.y) {
            case 1:
                nr.y = or.y;
                break;
            case 2:
                nr.y = (int) or.getCenterY() - tr.height / 2;
                break;
            case 3:
                nr.y = (int) or.y + or.height - tr.height;
                break;
        }

        Color tempColor = g.getColor();
        if (enabledDebug) {
            System.out.println("Info: " + direction + ", " + xy.x + ", " + xy.y + "\nOriginal: " + or + "\nNew: " + nr);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(or.x, or.y, or.width, or.height);
            g.setColor(Color.GRAY);
            g.fillRect(nr.x, nr.y, nr.width, nr.height);
            g.setColor(tempColor);
        }
        drawTextCenteredInRect(nr, displayText, g);
    }
    public static Rectangle getRectAroundText(Rectangle or /*Original Rect */, Point border, String displayText, Direction8 direction, boolean enabledDebug, Graphics g) {
        or = new Rectangle(or); // prevents changing the given rectangle
        FontMetrics fm = g.getFontMetrics();
        Rectangle tr = fm.getStringBounds(displayText, g).getBounds(); // text rect   // Always accessible
        Rectangle nr = new Rectangle(tr);  // change-able
        Point xy = new Point(1, 1); // each axis can range from 1 to 3
        /*
           11 21 31
           12 22 32
           13 23 33
        */
       
        or.x = Math.max(0, or.x + border.x);
        or.y = Math.max(0, or.y + border.y);
        or.width = Math.max(0, or.width - border.x * 2);
        or.height = Math.max(0, or.height - border.y * 2);

        nr.x = (int) or.getCenterX() - tr.width / 2;
        nr.y = (int) or.getCenterY() - tr.height / 2;
        
        switch (direction) {
            case LEFT_TOP:
                xy = new Point(1, 1);
                break;
            case CENTER_TOP:
                xy = new Point(2, 1);
                break;
            case RIGHT_TOP:
                xy = new Point(3, 1);
                break;
            case LEFT_CENTER:
                xy = new Point(1, 2);
                break;
            case CENTER_CENTER:
                xy = new Point(2, 2);
                break;
            case RIGHT_CENTER:
                xy = new Point(3, 2);
                break;
            case LEFT_BOTTOM:
                xy = new Point(1, 3);
                break;
            case CENTER_BOTTOM:
                xy = new Point(2, 3);
                break;
            case RIGHT_BOTTOM:
                xy = new Point(3, 3);
                break;
        }
        switch (xy.x) {
            case 1:
                nr.x = or.x;
                break;
            case 2:
                nr.x = (int) or.getCenterX() - tr.width / 2;
                break;
            case 3:
                nr.x = (int) or.x + or.width - tr.width;
                break;
        }
        switch (xy.y) {
            case 1:
                nr.y = or.y;
                break;
            case 2:
                nr.y = (int) or.getCenterY() - tr.height / 2;
                break;
            case 3:
                nr.y = (int) or.y + or.height - tr.height;
                break;
        }

        Color tempColor = g.getColor();
        if (enabledDebug) {
            System.out.println("Info: " + direction + ", " + xy.x + ", " + xy.y + "\nOriginal: " + or + "\nNew: " + nr);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(or.x, or.y, or.width, or.height);
            g.setColor(Color.GRAY);
            g.fillRect(nr.x, nr.y, nr.width, nr.height);
            g.setColor(tempColor);
        }
        return nr;
    }
    public static void drawTextCenteredInRect(Rectangle rect, String displayText, Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        
        Rectangle tr = fm.getStringBounds(displayText, g).getBounds();
        tr.x = (int) rect.getCenterX() - tr.width / 2;
        tr.y = (int) rect.getCenterY() - tr.height / 2;

        g.drawString(displayText, tr.x, tr.y + fm.getAscent());
    }
    public static void setGraphicsStrokeWidth(int strokeWidth, Graphics2D g) {
        g.setStroke(new BasicStroke(strokeWidth));
    }
    public static void setGraphicsAlpha(double alphaMultiplier, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (alphaMultiplier > 1 ? 1 : (alphaMultiplier < 0 ? 0 : alphaMultiplier))));
        // This method needs no return because it changes g, not creating a new one
    }
    public static void drawTranslucentImage(BufferedImage image, int x, int y, int width, int height, double alphaMultiplier, Graphics g) {
        setGraphicsAlpha(alphaMultiplier, g);
        g.drawImage(image, x, y, width, height, null);
        setGraphicsAlpha(1.0, g);
    }
    public static void drawTranslucentImage(BufferedImage image, int x, int y, double alphaMultiplier, Graphics g) {
        setGraphicsAlpha(alphaMultiplier, g);
        g.drawImage(image, x, y, null);
        setGraphicsAlpha(1.0, g);
    }

    // Debug Rendering
    public static void renderControllerInfo(Color textColor, ConsoleController controller, Graphics g) {
        //vars for alignment
        Font largeFont = new Font(Font.SANS_SERIF, Font.BOLD, 35);
        Font smallFont = new Font(Font.SANS_SERIF, Font.BOLD, 30);
        int x = 30;
        int y = 200;
        int incLarge = 45; // space incremented in y axis between rendering each info piece
        int incSmall = 40;
        //configure g correctly
        g.setColor(textColor);
        g.setFont(largeFont);

        //Render Controller Info
        g.drawString("Controller is connected: " + controller.controllerIsConnected, x, y);
        y += incLarge;
        g.drawString("Controller name: " + controller.name, x, y);
        y += incLarge;
        g.drawString("Controller type: " + controller.controllerType, x, y);
        y += incLarge;
        g.drawString("Button,   Held, Since Press, Since Release", x, y);
        y += incLarge;
        
        //draw button info
        g.setFont(smallFont);
        if (true) {
            int buttonX = x + 30;
            int heldX = buttonX + 270;
            int sincePressX = heldX + 100;
            int sinceReleaseX = sincePressX + 150;
            for (ControllerButton button : ControllerButton.values()) {
                g.drawString("" + button, buttonX, y);
                g.drawString("" + controller.buttonIsHeld(button), heldX, y);
                g.drawString("" + controller.getFramesSinceButtonPress(button), sincePressX, y);
                g.drawString("" + controller.getFramesSinceButtonRelease(button), sinceReleaseX, y);
                y += incSmall;
            }
        }

        renderJoystickDegubInfo(new Point(0, 0), new Point(50, -20), "Left Move", controller.getLeftMoveJoystick(), false, controller.getJoystickZone(controller.getLeftSelectJoystick()), controller.getFramesInLeftJoystickZone(), g);
        renderJoystickDegubInfo(new Point(250, 0), new Point(-130, 20), "Right Move", controller.getRightMoveJoystick(), false, controller.getJoystickZone(controller.getRightSelectJoystick()), controller.getFramesInRightJoystickZone(), g);
        renderJoystickDegubInfo(new Point(0, 400), new Point(50, -20), "Left Select", controller.getLeftSelectJoystick(), true, controller.getJoystickZone(controller.getLeftSelectJoystick()), controller.getFramesInLeftJoystickZone(), g);
        renderJoystickDegubInfo(new Point(250, 400), new Point(-130, 20), "Right Select", controller.getRightSelectJoystick(), true, controller.getJoystickZone(controller.getRightSelectJoystick()), controller.getFramesInRightJoystickZone(), g);
    }
    private static void renderJoystickDegubInfo(Point offset, Point additionalTextOffset, String joystickSide, Point2D.Double joystick, boolean renderZones, Direction5 zone, int framesInJoystickZone, Graphics g) {
        // Draw digital joysticks

        int largeRadius = 300; //area size
        int smallRadius = 100; //joystick size
        int realLargeRadius = largeRadius + smallRadius / 2; //account for the joystick size
        int textLineOffset = 100;

        Point circleCenter = new Point(1200 + offset.x, 200 + offset.y);
        Point textCorner = new Point(circleCenter.x - largeRadius / 2 + additionalTextOffset.x, circleCenter.y + largeRadius / 2 + 50 + additionalTextOffset.y);
        
        drawCircle(circleCenter, realLargeRadius / 2, g);
        fillCircle(new Point(circleCenter.x + (int)(largeRadius / 4 * joystick.x), circleCenter.y + (int)(largeRadius / 4 * -joystick.y)), smallRadius / 2, g);

        g.drawString(joystickSide + " Joystick: " + EngineCalculator.setStringLength(true, 4,"" + joystick.x) + ", " + EngineCalculator.setStringLength(true, 4,"" + joystick.y), textCorner.x, textCorner.y);
        if (renderZones) {
            g.drawString(joystickSide + " Joystick Zone: " + zone, textCorner.x, textCorner.y + 100);
            g.drawString("Frames in zone: " + framesInJoystickZone, textCorner.x, textCorner.y + 100 + textLineOffset);
        }
    }
    public static void renderCursorDebugInfo(Graphics2D g) {
        //cursor point
        Point cursor = Game.mouse.getCursorDisplayCoords();
        int size1 = 20;
        int size2 = 16;
        int size3 = 12;
        int size4 = 8;
        
        g.setColor(Color.BLACK);
        g.fillRect(cursor.x - size1 / 2, cursor.y - size1 / 2, size1, size1);
        if (Game.mouse.heldButtons.contains(MouseButton.LEFT)) {
            g.setColor(Color.GREEN);
            g.fillRect(cursor.x - size2 / 2, cursor.y - size2 / 2, size2, size2);
        }
        if (Game.mouse.heldButtons.contains(MouseButton.RIGHT)) {
            g.setColor(Color.RED);
            g.fillRect(cursor.x - size3 / 2, cursor.y - size3 / 2, size3, size3);
        }
        if (Game.mouse.heldButtons.contains(MouseButton.MIDDLE)) {
            g.setColor(Color.WHITE);
            g.fillRect(cursor.x - size4 / 2, cursor.y - size4 / 2, size4, size4);
        }
    }
    public static void renderTextDebugInfo(ArrayList<String> textList, String headerPrefix, int scroll, Graphics2D g) {
        // "headerPrefix" should be "<header>"

        Color lineColor = Color.DARK_GRAY;
        Color headerColor = Color.BLACK;

        Font lineFont = new Font(Font.SANS_SERIF, Font.PLAIN, 30);
        Font headerFont = new Font(Font.SANS_SERIF, Font.BOLD, 40);

        int indent = 20;
        int lineSpacing = 40;
        int sectionSpacing = 30;

        int x = 50;
        int y = 0;

        for (String rawText : textList) {
            boolean isHeader = false;
            String text = rawText;
            if (rawText.startsWith(headerPrefix)) {
                isHeader = true;
                text = rawText.substring(headerPrefix.length());
                g.setColor(headerColor);
                g.setFont(headerFont);
            } else {
                g.setColor(lineColor);
                g.setFont(lineFont);
            }
            y += lineSpacing + (isHeader ? sectionSpacing : 0);
            g.drawString(
                text,
                x + (isHeader ? 0 : indent),
                y
            );
        }
    }

    // No longer used debug rendering
    public static void renderRings(Graphics g) {
        int inc = 100;
        int index = 0;
        ArrayList<Color> colors = EngineCalculator.arrayToArrayList(new Color[] {
            new Color(255, 0, 0, 255),
            new Color(0, 255, 0, 255),
            new Color(0, 0, 255, 255),
            new Color(255, 255, 0, 255),
            new Color(0, 255, 255, 255),
            new Color(255, 0, 255, 255)
        });
        int x = Game.gameResolution.width;
        int y = Game.gameResolution.height;
        for (; x > 0 && y > 0;) {
            g.setColor(colors.get(index));
            index++;
            if (index >= colors.size()) {index = 0;}
            g.fillRect((Game.gameResolution.width - x) / 2, (Game.gameResolution.height - y) / 2, x, y);
            //System.out.println("Vars: " + (gameResolution.width - x) / 2 + ", " + (gameResolution.height - y) / 2 + ", " +  x + ", " + y);
            
            x -= inc;
            y -= inc;
        }
    }
}