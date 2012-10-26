package haveric.stackableItems;

import haveric.stackableItems.mcstats.Metrics;
import haveric.stackableItems.mcstats.Metrics.Graph;
import haveric.stackableItems.vanish.Vanish;

import java.io.IOException;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.VanishPlugin;

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
        VirtualItemConfig.init(this);

        // Vault
        setupVault(pm);

        // VanishNoPacket
        setupVanish(pm);

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
            return;
        }

        RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permProvider != null) {
            Perms.setPerm(permProvider.getProvider());
        }
    }

    private void setupVanish(PluginManager pm) {
        Plugin vanish = pm.getPlugin("VanishNoPacket");
        if (vanish == null || !(vanish instanceof VanishPlugin)) {
            // No VanishNoPacket
        } else {
            Vanish.setVanish((VanishPlugin) vanish);
        }
    }

    private void setupMetrics() {
        try {
            metrics = new Metrics(this);

            // Custom data
            Graph javaGraph = metrics.createGraph("Java Version");
            String javaVersion = System.getProperty("java.version");
            javaGraph.addPlotter(new Metrics.Plotter(javaVersion) {
                @Override
                public int getValue() {
                    return 1;
                }
            });
            metrics.addGraph(javaGraph);
            // End Custom data

            metrics.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
