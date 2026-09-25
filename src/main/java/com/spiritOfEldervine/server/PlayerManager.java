package com.spiritOfEldervine.server;

import java.util.LinkedHashMap;
import java.util.Map;

import com.spiritOfEldervine.engine.EngineCalculator;

public class PlayerManager {
    private Map<Integer, Player> players = new LinkedHashMap<>();
    
    public PlayerManager() {}

    public Player getPlayer(int id) {
        if (players.containsKey(id)) {
            return players.get(id);
        } else {
            System.out.println("Player with id " + id + " does not exist in PlayerManager.getPlayer()!");
            EngineCalculator.printStackTrace();
            return null;
        }
    }
    public void createPlayer(int id) {
        players.put(id, new Player(id));
    }
    public void setPlayerName(int id, String name) {
        getPlayer(id).name = name;
    }
}
