package ca.encodeous.virtualedit;

import ca.encodeous.virtualedit.data.ViewQueue;
import ca.encodeous.virtualedit.protocol.BukkitListener;
import ca.encodeous.virtualedit.protocol.ProtocolListener;
import ca.encodeous.virtualedit.protocol.VirtualEditViewThread;
import ca.encodeous.virtualedit.world.VirtualWorldView;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualWorld {
    public static ConcurrentHashMap<World, VirtualWorld> views;
    public static ProtocolManager Protocol;
    private static ProtocolListener chunkListener;
    private static VirtualEditViewThread[] threads;
    public static ViewQueue PlayerUpdateQueue;
    private ConcurrentHashMap<UUID, VirtualWorldView> playerViews = new ConcurrentHashMap<>();
    private World world;

    public static void initialize(Plugin plugin){
        if(views == null){
            views = new ConcurrentHashMap<>();
            chunkListener = new ProtocolListener(plugin);
            Bukkit.getPluginManager().registerEvents(new BukkitListener(), plugin);
            Protocol = ProtocolLibrary.getProtocolManager();
            threads = new VirtualEditViewThread[Constants.VIEW_UPDATE_THREADS];
            for (int i = 0; i < Constants.VIEW_UPDATE_THREADS; i++) {
                VirtualEditViewThread thread = new VirtualEditViewThread();
                thread.setDaemon(true);
                thread.start();
                threads[i] = thread;
            }
            PlayerUpdateQueue = new ViewQueue();
        }
        else {
            throw new RuntimeException("VirtualEdit has already been initialized");
        }
    }
    public static VirtualWorld of(World world){
        if(views == null){
            throw new RuntimeException("Please call the VirtualWorld.initialize() function before calling this one.");
        }
        if(views.containsKey(world)){
            return views.get(world);
        }
        var instance = new VirtualWorld();
        instance.world = world;
        views.put(world, instance);
        return instance;
    }
    public static void cleanup(){
        chunkListener.unregister();
        for (VirtualEditViewThread thread : threads) {
            if (thread != null) {
                thread.close();
            }
        }
        views.clear();
    }

    public VirtualWorldView getView(Player p){
        return playerViews.get(p.getUniqueId());
    }
    public VirtualWorldView getView(UUID p){
        return playerViews.get(p);
    }

    public void addPlayer(Player p){
        VirtualWorldView vw = new VirtualWorldView(p, world);
        playerViews.put(p.getUniqueId(), vw);
    }
    public static void removePlayerFromAll(Player p){
        for(var val : views.values()){
            val.removePlayer(p);
        }
    }
    public void removePlayer(Player p){
        VirtualWorldView view = getView(p.getUniqueId());
        if(view != null){
            view.close();
            playerViews.remove(p.getUniqueId());
        }
    }
}
