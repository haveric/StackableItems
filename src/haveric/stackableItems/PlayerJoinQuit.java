package haveric.stackableItems;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuit implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        SIItems.addItemFiles(event.getPlayer().getName());
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (player != null) {
            SIItems.removeItemFiles(player.getName());
        }
    }
}
