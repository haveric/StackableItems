package haveric.stackableItems;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuit implements Listener {

    StackableItems plugin;
    public PlayerJoinQuit(StackableItems stackableItems) {
        plugin = stackableItems;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        SIItems.addItemFiles(event.getPlayer().getName());
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            if (Config.isDebugging()) plugin.log.warning("[DEBUG] Player is null in PlayerQuitEvent.");
        } else {
            SIItems.removeItemFiles(player.getName());
        }
    }
}
