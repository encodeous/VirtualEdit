package ca.encodeous.virtualedit;

import ca.encodeous.virtualedit.Data.ViewQueue;
import ca.encodeous.virtualedit.Protocol.BukkitListener;
import ca.encodeous.virtualedit.Protocol.ChunkListener;
import ca.encodeous.virtualedit.Protocol.VirtualEditViewThread;
import ca.encodeous.virtualedit.Utils.StubConfig;
import ca.encodeous.virtualedit.World.VirtualWorldView;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.imprex.orebfuscator.NmsInstance;
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
    public ChunkListener chunkListener;
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
                VirtualEditViewThread thread = new VirtualEditViewThread(plugin);
                thread.setDaemon(true);
                thread.start();
                Instance.threads[i] = thread;
            }
        }
        else return;
        NmsInstance.initialize(new StubConfig());
        Instance.chunkListener = new ChunkListener(plugin);
    }

    public static VirtualWorldView GetPlayerView(UUID p){
        return Instance.playerViews.get(p);
    }

    public void AddPlayer(Player p){
        playerViews.put(p.getUniqueId(), new VirtualWorldView(p));
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
        NmsInstance.close();
        for (VirtualEditViewThread thread : this.threads) {
            if (thread != null) {
                thread.close();
            }
        }
        Instance = null;
    }
}
