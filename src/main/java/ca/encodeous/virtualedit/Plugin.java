package ca.encodeous.virtualedit;

import ca.encodeous.virtualedit.world.VirtualWorldLayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin implements Listener {
    private VirtualWorld world;
    private VirtualWorldLayer layer;
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Initializing VirtualEdit");
        VirtualWorld.initialize(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        world = VirtualWorld.of(Bukkit.getWorlds().get(0));
        layer = new VirtualWorldLayer(2001, 101, 2001);
        layer.setBlock(0, 2000, 0, 100, 0, 2000, Material.WHITE_STAINED_GLASS);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        world.addPlayer(e.getPlayer());
        var view = world.getView(e.getPlayer());
        view.pushLayer(layer);
        view.refreshWorldView();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        VirtualWorld.cleanup();
    }
}
