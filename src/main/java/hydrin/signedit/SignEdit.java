package hydrin.signedit;

import hydrin.signedit.commands.SignEditCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SignEdit extends JavaPlugin {
    private static boolean plotsquared = false;

    @Override
    public void onEnable() {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlotSquared")) {
            plotsquared = true;
            System.out.println(plotsquared);
        }

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
        return plotsquared;
    }
}
