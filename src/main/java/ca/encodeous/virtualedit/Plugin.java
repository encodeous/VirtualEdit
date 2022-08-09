package ca.encodeous.virtualedit;

import ca.encodeous.virtualedit.data.nodestorage.PointerNode;
import ca.encodeous.virtualedit.world.VirtualWorldLayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Plugin extends JavaPlugin implements Listener {
    VirtualWorldLayer layer = null;
    @Override
    public void onEnable() {
        // Plugin startup logic
        VirtualWorld.Initialize(this);
        layer = new VirtualWorldLayer(200001, 181, 200001);
        layer.translate(-100000, -64, -100000);
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
        layer.setBlock(0, 200000, 0, 20, 0, 200000, Material.WHITE_CONCRETE);
        layer.setPointer(Bukkit.getWorld("world_nether"), 0, 20, 0, 0, 0, 0, 200000, 80, 200000);
        layer.setPointer(Bukkit.getWorld("world_the_end"), 0, 100, 0, 0, 40, 0, 200000, 80, 200000);
//        for(int i = 0; i < 48; i++){
//            for(int j = 0; j < 24000; j++) {
//                layer.setBlock(0, 200000, i, i, j, j, Material.WHITE_CONCRETE);
//                cnt++;
//            }
//        }

        System.out.println("Took " + (System.currentTimeMillis() - start) + " ms");

        System.out.println(layer.getNode(-10, 0, -10));
        System.out.println(layer.getNode(-10, 4, -10));
        System.out.println(layer.getNode(0, 0, -10));
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
