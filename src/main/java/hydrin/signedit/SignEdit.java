package hydrin.signedit;

import hydrin.signedit.commands.SignEditCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class SignEdit extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
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
}
