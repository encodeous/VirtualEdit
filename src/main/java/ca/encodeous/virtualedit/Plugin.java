package ca.encodeous.virtualedit;

import ca.encodeous.virtualedit.Utils.MaterialUtils;
import ca.encodeous.virtualedit.World.VirtualWorldLayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
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

        Material[] colors = new Material[]{
                Material.PINK_STAINED_GLASS,
                Material.RED_STAINED_GLASS,
                Material.ORANGE_STAINED_GLASS,
                Material.YELLOW_STAINED_GLASS,
                Material.LIME_STAINED_GLASS,
                Material.GREEN_STAINED_GLASS,
                Material.LIGHT_BLUE_STAINED_GLASS,
                Material.CYAN_STAINED_GLASS,
                Material.BLUE_STAINED_GLASS,
                Material.PURPLE_STAINED_GLASS,
                Material.MAGENTA_STAINED_GLASS
        };

        var start = System.currentTimeMillis();

        int cnt = 0;
        for(int i = 0; i < 24; i+=2){
            for(int j = -12000; j < 12000; j+=2) {
                layer.vWorld.Update(-200000, 200000, i, i, j, j, MaterialUtils.getId(colors[cnt % colors.length]));
                cnt++;
            }
        }

        System.out.println("Took " + (System.currentTimeMillis() - start) + " ms");

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
    public void PlayerLoginEvent(PlayerLoginEvent event){
        VirtualWorld.GetPlayerView(event.getPlayer().getUniqueId()).pushLayer(layer);
    }
}
