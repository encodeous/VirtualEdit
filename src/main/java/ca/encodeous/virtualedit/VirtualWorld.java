package ca.encodeous.virtualedit;

import ca.encodeous.virtualedit.data.ViewQueue;
import ca.encodeous.virtualedit.protocol.BukkitListener;
import ca.encodeous.virtualedit.protocol.ProtocolListener;
import ca.encodeous.virtualedit.protocol.VirtualEditViewThread;
import ca.encodeous.virtualedit.world.VirtualWorldView;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualWorld implements Closeable {
    public static VirtualWorld Instance;
    public static ProtocolManager Protocol;
    public ProtocolListener chunkListener;
    private ConcurrentHashMap<UUID, VirtualWorldView> playerViews = new ConcurrentHashMap<>();
    public ViewQueue PlayerUpdateQueue;
    private VirtualEditViewThread[] threads;

    public static void Initialize(Plugin plugin){
        if(Instance == null){
            Instance = new VirtualWorld();
            Instance.PlayerUpdateQueue = new ViewQueue();
            Protocol = ProtocolLibrary.getProtocolManager();
            Bukkit.getPluginManager().registerEvents(new BukkitListener(), plugin);
            Instance.threads = new VirtualEditViewThread[Constants.VIEW_UPDATE_THREADS];
            for (int i = 0; i < Constants.VIEW_UPDATE_THREADS; i++) {
                VirtualEditViewThread thread = new VirtualEditViewThread();
                thread.setDaemon(true);
                thread.start();
                Instance.threads[i] = thread;
            }
        }
        else return;
        Instance.chunkListener = new ProtocolListener(plugin);
    }

    public static VirtualWorldView GetPlayerView(UUID p){
        return Instance.playerViews.get(p);
    }

    public void AddPlayer(Player p){
        VirtualWorldView vw = new VirtualWorldView(p, p.getWorld());
        playerViews.put(p.getUniqueId(), vw);
//        Bukkit.getScheduler().scheduleSyncDelayedTask(chunkListener.getPlugin(), vw::RefreshViewport, 1);
    }

    public void RemovePlayer(Player p){
        VirtualWorldView view = GetPlayerView(p.getUniqueId());
        if(view != null){
            view.close();
            playerViews.remove(p.getUniqueId());
        }
    }

    @Override
    public void close() throws IOException {
        chunkListener.unregister();
        for (VirtualEditViewThread thread : this.threads) {
            if (thread != null) {
                thread.close();
            }
        }
        Instance = null;
    }
}
