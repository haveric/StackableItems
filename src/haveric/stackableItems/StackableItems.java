package haveric.stackableItems;

import haveric.stackableItems.mcstats.Metrics;

import java.io.IOException;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class StackableItems extends JavaPlugin {

    public Logger log;

    private Commands commands = new Commands(this);

    private Metrics metrics;

    public void onEnable() {
        log = getLogger();
        PluginManager pm = getServer().getPluginManager();

        // Register the plugin events
        pm.registerEvents(new SIPlayerListener(this), this);
        pm.registerEvents(new SIBlockBreak(), this);
        pm.registerEvents(new PlayerJoinQuit(), this);

        Config.init(this);
        FurnaceXPConfig.init(this);
        VirtualItemConfig.init(this);

        // Vault
        setupVault(pm);

        SIItems.init(this);
        InventoryUtil.init(this);
        FurnaceUtil.init(this);

        Config.setup();
        FurnaceXPConfig.setup();

        getCommand(Commands.getMain()).setExecutor(commands);

        setupMetrics();
    }

    public void onDisable() {

    }

    private void setupVault(PluginManager pm) {
        if (pm.getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
            if (permProvider != null) {
                Perms.init(this, permProvider.getProvider());
            }
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
