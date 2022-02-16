package haveric.stackableItems;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import haveric.stackableItems.config.Config;
import haveric.stackableItems.config.FurnaceXPConfig;
import haveric.stackableItems.listeners.SIBlockListener;
import haveric.stackableItems.listeners.SIHopperListener;
import haveric.stackableItems.listeners.SIPlayerJoinQuitListener;
import haveric.stackableItems.listeners.SIPlayerListener;
import haveric.stackableItems.util.FurnaceUtil;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.PermissionsUtil;
import haveric.stackableItems.util.SIItems;
import net.milkbowl.vault.permission.Permission;

public class StackableItems extends JavaPlugin {

    public Logger log;
    private static StackableItems plugin;
    public boolean supportsInventoryStackSize = true;

    private Commands commands = new Commands(this);

    public void onEnable() {
        log = getLogger();
        plugin = this;

        PluginManager pm = getServer().getPluginManager();

        // Register the plugin events
        pm.registerEvents(new SIPlayerListener(this), this);
        pm.registerEvents(new SIHopperListener(), this);
        pm.registerEvents(new SIBlockListener(), this);
        pm.registerEvents(new SIPlayerJoinQuitListener(), this);

        Config.init(this);
        FurnaceXPConfig.init(this);

        // Vault
        setupVault(pm);

        SIItems.init(this);
        InventoryUtil.init(this);
        FurnaceUtil.init(this);

        Config.setup();
        FurnaceXPConfig.setup();

        getCommand(Commands.getMain()).setExecutor(commands);

        Updater.init(this, 37175, null);
    }

    public void onDisable() {

    }

    private void setupVault(PluginManager pm) {
        if (pm.getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
            if (permProvider != null) {
                PermissionsUtil.init(this, permProvider.getProvider());
            }
        }
    }

    public static StackableItems getPlugin() {
        return plugin;
    }
}
