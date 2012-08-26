package haveric.stackableItems;

import java.io.IOException;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class StackableItems extends JavaPlugin {

    //private final Logger log = Logger.getLogger("Minecraft");
    final Logger log = Logger.getLogger("Minecraft");

    private Commands commands = new Commands(this);

    private Metrics metrics;

    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        // Register the plugin events
        pm.registerEvents(new SIPlayerListener(this), this);
        pm.registerEvents(new SIBlockBreak(), this);
        pm.registerEvents(new PlayerJoinQuit(), this);

        Config.init(this);
        VirtualItemConfig.init(this);


        // Vault
        setupVault(pm);

        SIItems.init(this);
        InventoryUtil.init(this);
        FurnaceUtil.init(this);

        SIPlayers.init();

        Config.setup();

        getCommand(Commands.getMain()).setExecutor(commands);

        setupMetrics();
    }

    public void onDisable() {

    }

    private void setupVault(PluginManager pm) {
        if (pm.getPlugin("Vault") == null) {
            log.info(String.format("[%s] Vault not found. Permissions disabled.", getDescription().getName()));
            return;
        }
        RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permProvider != null) {
            Perms.setPerm(permProvider.getProvider());
        }
    }

    private void setupMetrics() {
        try {
            metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
