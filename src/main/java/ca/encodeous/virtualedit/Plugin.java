package ca.encodeous.virtualedit;

import ca.encodeous.virtualedit.Utils.MaterialUtils;
import ca.encodeous.virtualedit.World.VirtualWorldLayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Plugin extends JavaPlugin implements Listener {
    VirtualWorldLayer layer = null;
    @Override
    public void onEnable() {
        // Plugin startup logic
        VirtualWorld.Initialize(this);
        layer = new VirtualWorldLayer();
        Bukkit.getPluginManager().registerEvents(this, this);

        layer.vWorld.Update(-20, 20, 10, 20, -20, 20, MaterialUtils.getId(Material.REDSTONE_BLOCK));

        System.out.println(layer.GetBlockIdAt(-10, 0, -10));
        System.out.println(layer.GetBlockIdAt(-10, 4, -10));
        System.out.println(layer.GetBlockIdAt(0, 0, -10));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            VirtualWorld.Instance.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        VirtualWorld.GetPlayerView(event.getPlayer().getUniqueId()).pushLayer(layer);
    }
}
