package com.shockWave.server.player;

import java.util.LinkedHashMap;
import java.util.Map;

import com.shockWave.engine.EngineCalculator;

public class SPlayerManager {
    private Map<Short, SPlayer> players = new LinkedHashMap<>();
    
    public SPlayerManager() {}

    public SPlayer getPlayer(short id) {
        if (players.containsKey(id)) {
            return players.get(id);
        } else {
            System.out.println("Player with id " + id + " does not exist in PlayerManager.getPlayer()!");
            EngineCalculator.printStackTrace();
            return null;
        }
    }
    public void createPlayer(SPlayer player) {
        if (player == null) {System.out.println("player is null in SPlayerManager.createPlayer()!");}
        players.put(player.id, player);
    }
    public void setPlayerName(short id, String displayName) {
        getPlayer(id).displayName = displayName;
    }
    public Map<Short, SPlayer> getPlayerMap() {
        return players;
    }
}
