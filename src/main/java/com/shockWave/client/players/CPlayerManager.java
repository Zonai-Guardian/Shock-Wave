package com.shockWave.client.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.shockWave.engine.EngineCalculator;

public class CPlayerManager { // This actually manages CSimplePlayers
    private Map<Short, CSimplePlayer> playerMap = new HashMap<Short, CSimplePlayer>();

    public CPlayerManager() {}


    public void registerPlayer(CSimplePlayer player) {
        if (player == null) {
            System.out.println("player is null in CPlayerManager.registerPlayer()!");
            EngineCalculator.printStackTrace();
            return;
        }

        playerMap.put(player.id, player);
    }

    public boolean containsPlayer(Short id) {return playerMap.containsKey(id);}
    public CSimplePlayer getPlayer(Short id) {
        return playerMap.get(id);
    }
    public void removePlayer(Short id) {
        if (playerMap.containsKey(id)) {playerMap.remove(id);}
    }
}
