package ca.encodeous.virtualedit;

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
    private int cnt = 0;
    @Override
    public void onEnable() {
        // Plugin startup logic
        VirtualWorld.initialize(this);
        layer = new VirtualWorldLayer(201, 181, 201);
        layer.translate(-100, -64, -100);
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

        layer.setBlock(0, 200, 50, 50, 0, 200, Material.WHITE_CONCRETE);

//        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ()->{
//            layer.setBlock(0, 200, cnt, cnt, 0, 200, Material.AIR);
//            cnt++;
//            cnt %= 181;
//            layer.setBlock(0, 200, cnt, cnt, 0, 200, Material.WHITE_CONCRETE);
//            layer.syncToClient();
//        }, 0, 20);

        System.out.println("Took " + (System.currentTimeMillis() - start) + " ms");

        System.out.println(layer.getNode(-10, 0, -10));
        System.out.println(layer.getNode(-10, 4, -10));
        System.out.println(layer.getNode(0, 0, -10));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        VirtualWorld.cleanup();
    }

    @EventHandler
    public void PlayerLoginEvent(PlayerLoginEvent event){
        var p = event.getPlayer();
        var world = VirtualWorld.of(p.getWorld());
        world.getView(p).pushLayer(layer);
    }
}
