package hydrin.signedit;

import hydrin.signedit.commands.SignEditCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SignEdit extends JavaPlugin {
    private static final String PLOTSQUARED_NAME = "PlotSquared";
    private static final String COREPROTECT_NAME = "CoreProtect";

    private static boolean plotSquared = false;
    private static boolean coreProtect = true;

    private static CoreProtectAPI coreProtectAPI;

    @Override
    public void onEnable() {
        PluginManager plugins = Bukkit.getServer().getPluginManager();

        if (plugins.isPluginEnabled(PLOTSQUARED_NAME)) { plotSquared = true; }
        if (plugins.isPluginEnabled(COREPROTECT_NAME)) {
            coreProtect = true;

            coreProtectAPI = ((CoreProtect) plugins.getPlugin(COREPROTECT_NAME)).getAPI();
        }

        System.out.println("P²: " + plotSquared);
        System.out.println("CO: " + coreProtect);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar()
                    .register(
                            SignEditCommand.register(), "Modify certain properties and fetch information of the targeted sign"
                    );
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static boolean hasPlotSquared() {
        return plotSquared;
    }

    public static boolean hasCoreProtect() {
        return coreProtect;
    }

    public static CoreProtectAPI getCoreProtectAPI() {
        return coreProtectAPI;
    }
}
