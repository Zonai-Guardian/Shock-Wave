package com.shockWave.notification;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.shockWave.Game;

public class NotificationManager {
    private ArrayList<Notification> notifications = new ArrayList<>();

    private int notificationSpacing = 30;

    public NotificationManager() {}

    public void update() {
        boolean didDeleteNotification = false;
        for (int i = 0; i < notifications.size(); i++) {
            notifications.get(i).update();
            if (notifications.get(i).shouldDeleteNow) {
                notifications.remove(i);
                i--;
                didDeleteNotification = true;
                continue;
            }
        }
        if (didDeleteNotification) {calculatePositions(false);}
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        calculatePositions(true);
    }
    
    public void calculatePositions(boolean hasAddedNewNotification) {
        Graphics2D g = new BufferedImage(1, 1, 1).createGraphics();
        
        int nextYValue = 0;
        int xValue = Game.gameResolution.width;

        for (int i = 0; i < notifications.size(); i++) {
            boolean isLastNotification = i == notifications.size() - 1;
            Notification noti = notifications.get(i);
            Rectangle rect = noti.getRect(g);
            
            if (i == 0) {
                nextYValue += rect.height;
            }
            if (isLastNotification) {
                noti.addTargetPosition(new Point(xValue, nextYValue));
                noti.addTargetPosition(new Point(xValue - rect.width, nextYValue));
            } else {
                noti.addTargetPosition(new Point(xValue - rect.width, nextYValue));
                nextYValue += notificationSpacing + rect.height;
            }
        }
    }
    public void render(Graphics2D g) {
        for (Notification noti : notifications) {
            noti.render(g);
        }
    }
}
