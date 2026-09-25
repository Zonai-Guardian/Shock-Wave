package com.spiritOfEldervine.client;

import java.awt.geom.Point2D;

public class ClientPlayer {
    public String displayName;
    public int clientID;

    public Point2D.Double position;
    public Point2D.Double velocity;

    public ClientPlayer(String displayName, int clientID, Point2D.Double position, Point2D.Double velocity) {
        this.displayName = displayName;
        this.clientID = clientID;
        this.position = position;
        this.velocity = velocity;
    }
    public void setTransform(Point2D.Double position, Point2D.Double velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    // Updaters
    public void update(double delta) {
        updateTransform(delta);
    }
    private void updateTransform(double delta) {
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }
}
