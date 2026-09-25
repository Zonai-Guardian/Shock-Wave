package com.spiritOfEldervine.server;

import java.awt.geom.Point2D;

public class Player {
    public String name = "Undefined";
    public int clientID;

    public Player(int clientID) {
        this.clientID = clientID;
    }
}
