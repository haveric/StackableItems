package haveric.stackableItems.listeners;

import haveric.stackableItems.util.SIItems;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SIPlayerJoinListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        String uuid = player.getUniqueId().toString();

        SIItems.updateUUIDName(uuid, name);
    }
}
