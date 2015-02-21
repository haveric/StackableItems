package haveric.stackableItems.listeners;

import haveric.stackableItems.util.SIItems;
import haveric.stackableItems.uuidFetcher.UUIDFetcher;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SIPlayerJoinQuitListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        UUID uuid = player.getUniqueId();
        String uuidString = uuid.toString();

        SIItems.updateUUIDName(uuidString, name);
        UUIDFetcher.addPlayerToCache(name, uuid);
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        UUIDFetcher.removePlayerFromCache(name);
    }
}
