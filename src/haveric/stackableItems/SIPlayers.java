package haveric.stackableItems;

import java.util.HashMap;

public final class SIPlayers {

    private static HashMap<String, PlayerClickData> playerData;

    private SIPlayers() { } // Private constructor for utility class
    public static void setup() {
        playerData = new HashMap<String, PlayerClickData>();
    }

    public static PlayerClickData getPlayerData(String name) {
        PlayerClickData clickData;
        if (playerData.containsKey(name)) {
            clickData = playerData.get(name);
        } else {
            clickData = new PlayerClickData();
            playerData.put(name, clickData);
        }

        return clickData;
    }

    public static void setPlayerData(String name, PlayerClickData clickData) {
        playerData.put(name, clickData);
    }
}
