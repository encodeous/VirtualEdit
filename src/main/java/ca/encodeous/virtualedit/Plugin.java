package ca.encodeous.virtualedit;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Initializing VirtualEdit");
        VirtualWorld.initialize(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        VirtualWorld.cleanup();
    }
}
