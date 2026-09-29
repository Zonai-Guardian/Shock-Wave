package com.shockWave.notification;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import com.shockWave.Game;
import com.shockWave.engine.EngineCalculator;

import aurelienribon.tweenengine.TweenEquations;

public class Notification {
    public String title = "Notification";
    public Color titleColor = Color.BLACK;
    public Font titleFont = new Font("Sanserif", Font.BOLD, 40);

    public String message = "No text was assigned to this notification message";
    public Color messageColor = Color.GRAY;
    public Font messageFont = new Font("Sanserif", Font.PLAIN, 25);

    public String tooltip = null;
    public Color backgroundColor = Color.YELLOW;
    public Color borderColor = Color.ORANGE;
    public String iconName; // optional
    public int framesToDisplayFor = 3;

    // For changing position on screen
    private ArrayList<Point> targetedPositionList = new ArrayList<>();
    private Point targetPosition = null;
    private Point currentPosition = null;
    private Point previousPosition = null;
    private int animationFrame = 0;
    private int maxAnimationFrame = 40;
    private int displayFrame = 0;
    public boolean shouldDeleteSoon = false;
    public boolean shouldDeleteNow = false;

    //    Render Vars (this should save some rendering power by calculating sizes only once for each Notification)
    // Pre Made
    private int distanceFromEdge = 30;
    private int internalBoarder = 20; // This will be the distance between the icon and title, title and message, etc.
    private int border = 15;
    private int margin = 40;
    private int edgeRoundedRadius = 15;
    private int iconSize = 64;
    // Filled Later
    private boolean hasSetRenderVars = false;
    private Rectangle titleRect = null;
    private Rectangle messageRect = null;
    private Rectangle notificationRect = null;

    public Notification(String title, String message, String iconName, double secondsToDisplayFor) {
        if (title != null) {this.title = title;}
        if (message != null) {this.message = message;}
        if (iconName != null) {this.iconName = iconName;}
        framesToDisplayFor = EngineCalculator.secondsToFrames(secondsToDisplayFor);
    }

    public void update() {
        // This sets the variables for right after this Notification was created
        if (currentPosition == null) {
            currentPosition = targetedPositionList.get(0);
            targetedPositionList.remove(0);
            previousPosition = new Point(currentPosition);
        }
        // This skips moving to positions that the notification is already at
        while(targetPosition == null && targetedPositionList.size() > 0 && currentPosition.x == targetedPositionList.get(0).x && currentPosition.y == targetedPositionList.get(0).y) {
            targetedPositionList.remove(0);
        }
        // This moves to the next target location
        if (targetPosition == null && targetedPositionList.size() > 0) {
            targetPosition = targetedPositionList.get(0);
            targetedPositionList.remove(0);
        }
        // This updates the animationFrame and smooth position changing
        if (targetPosition != null && shouldDeleteNow == false) { // The notification should be moving to somewhere else
            animationFrame++;
            if (animationFrame > maxAnimationFrame) { // Reset animationFrame
                animationFrame = 0;
                previousPosition = targetPosition;
                currentPosition = targetPosition;
                targetPosition = null;
                shouldDeleteNow = shouldDeleteSoon;
            } else {
                currentPosition = new Point(
                    (int) EngineCalculator.tweenValues(previousPosition.x, targetPosition.x, (double) animationFrame / (double) maxAnimationFrame, TweenEquations.easeInOutQuad),
                    (int) EngineCalculator.tweenValues(previousPosition.y, targetPosition.y, (double) animationFrame / (double) maxAnimationFrame, TweenEquations.easeInOutQuad)
                );
            }
        }
        // This updates displayFrame and checks if the notification should be deleted soon
        displayFrame++;
        if (displayFrame > framesToDisplayFor && targetedPositionList.size() == 0 && targetPosition == null) {
            targetedPositionList.add(new Point(currentPosition.x + notificationRect.x, currentPosition.y));
            shouldDeleteSoon = true;
        }
    }
    public void addTargetPosition(Point position) {
        targetedPositionList.add(position);
    }
    public Rectangle getRect(Graphics2D g) {
        if (hasSetRenderVars == false) {setRenderVars(g);}

        return notificationRect;
    }
    private void setRenderVars(Graphics2D g) {

        g.setFont(titleFont);
        FontMetrics fm = g.getFontMetrics();

        titleRect = new Rectangle(0, 0, fm.stringWidth(title), fm.getAscent());
        titleRect.x = -titleRect.width;
        titleRect.y = -titleRect.height;

        g.setFont(messageFont);
        fm = g.getFontMetrics();
        messageRect = new Rectangle(0, 0, fm.stringWidth(message), fm.getAscent());
        messageRect.x = -messageRect.width;
        messageRect.y = -messageRect.height;

        notificationRect = new Rectangle(0, 0,
            Math.max(
                titleRect.width + iconSize + internalBoarder,
                messageRect.width
            ) + margin * 2 + border * 2,
            Math.max(iconSize, titleRect.height) + internalBoarder + messageRect.height + margin * 2 + border * 2
        );
        notificationRect.x = -notificationRect.width; // - distanceFromEdge;
        notificationRect.y = -notificationRect.height; // - distanceFromEdge;

        hasSetRenderVars = true;
    }

    public void render(Graphics2D g) {
        if (hasSetRenderVars == false) {setRenderVars(g);}
        Rectangle mainRect = new Rectangle(notificationRect.x + currentPosition.x, notificationRect.y + currentPosition.y, notificationRect.width, notificationRect.height);
        //     Rendering
        //   Background
        // Border
        g.setColor(borderColor);
        g.fillRoundRect(mainRect.x, mainRect.y, mainRect.width, mainRect.height, edgeRoundedRadius, edgeRoundedRadius);
        // Infill
        g.setColor(backgroundColor);
        g.fillRoundRect(mainRect.x + border, mainRect.y + border, mainRect.width - border * 2, mainRect.y - border * 2, edgeRoundedRadius, edgeRoundedRadius);

        //   Icon
        if (Game.images.contains(iconName)) {
            g.drawImage(Game.images.get(iconName), mainRect.x + border + margin, mainRect.y + mainRect.height - border - margin - distanceFromEdge, iconSize, iconSize, null);
        }

        //   Title
        g.setFont(titleFont);
        g.setColor(titleColor);
        g.drawString(title, mainRect.x + border + margin + iconSize + internalBoarder, mainRect.y + mainRect.height - border - margin - distanceFromEdge - titleRect.height);

        //   Message
        g.setFont(messageFont);
        g.setColor(messageColor);
        g.drawString(message, mainRect.x + border + margin, mainRect.y + border + margin + messageRect.height);
        
        System.out.println("mainRect: " + mainRect);
    }
}
