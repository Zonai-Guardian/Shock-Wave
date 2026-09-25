package com.spiritOfEldervine.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.net.InetAddress;
import java.util.ArrayList;

import com.spiritOfEldervine.Game;

public class Client {
    public ClientSocketManager socketManager;

    // enums
    public static enum ClientState {STARTUP, LOADING, CONNECTING, RUNNING, EXITING, LOST_CONNECTION} // I don't know if I'll actually use all of these...
    
    //all variables that can/should be deleted when the player exits the world
    public ClientState clientState = ClientState.STARTUP;
    public String playerDisplayName;
    public ArrayList<ClientPlayer> players = new ArrayList<>();

    public Client(String displayName, InetAddress serverAddress, int serverPort) { // serverPort does not accept null
        socketManager = new ClientSocketManager(serverAddress, serverPort);
        playerDisplayName = displayName;
    }
    public Client(String displayName) {
        socketManager = new ClientSocketManager();
        playerDisplayName = displayName;
    }

    public void loadAllData() {
        //load all required data to run the client
    }
    public void handleInput(ArrayList<String> buttonInputs, ArrayList<String> actionInputs) {
        //handle what to do when buttons are pressed and actions are activated

    }
    public void updateClient() {
        
    }

    //Rendering
    public void render(Graphics2D g) {
        //render everything the client should
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, Game.gameResolution.width, Game.gameResolution.height);
        
    }
}
