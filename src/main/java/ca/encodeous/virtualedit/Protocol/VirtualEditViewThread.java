package ca.encodeous.virtualedit.Protocol;

import ca.encodeous.virtualedit.Data.ViewQueue;
import ca.encodeous.virtualedit.VirtualWorld;
import ca.encodeous.virtualedit.World.VirtualWorldView;
import net.imprex.orebfuscator.NmsInstance;
import net.imprex.orebfuscator.util.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualEditViewThread extends Thread{
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Plugin cPlugin;
    public VirtualEditViewThread(Plugin plugin) {
        super("virtual-edit-view-thread");
        cPlugin = plugin;
    }

    @Override
    public void run() {
        while (this.running.get()) {
            try {
                Player player = VirtualWorld.Instance.PlayerUpdateQueue.poll();
                try {
                    if (player == null || !player.isOnline()) {
                        continue;
                    }
                    VirtualWorldView view = VirtualWorld.GetPlayerView(player.getUniqueId());

                    Location location = player.getLocation();

                    int distance = Bukkit.getViewDistance();

                    List<BlockPos> updateBlocks = new ArrayList<>();

                    int minChunkX = (location.getBlockX() - distance) >> 4;
                    int maxChunkX = (location.getBlockX() + distance) >> 4;
                    int minChunkZ = (location.getBlockZ() - distance) >> 4;
                    int maxChunkZ = (location.getBlockZ() + distance) >> 4;

                    view.CullChunks();

                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                            Set<BlockPos> blocks = view.getFlaggedUpdates().getChunk(chunkX, chunkZ);
                            if (blocks == null) {
                                continue;
                            }
                            updateBlocks.addAll(blocks);
                        }
                    }

                    Bukkit.getScheduler().runTask(cPlugin, () -> {
                        if (player.isOnline()) {
                            World ploc = player.getWorld();
                            for (BlockPos blockCoords : updateBlocks) {
                                Vector vdiff = new Vector(blockCoords.x, blockCoords.y, blockCoords.z);
                                player.sendBlockChange(vdiff.toLocation(ploc), view.ProcessWorldView(vdiff), (byte) 0);
                            }
                        }
                    });
                } finally {
                    VirtualWorld.Instance.PlayerUpdateQueue.unlock(player);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        this.running.set(false);
        this.interrupt();
    }
}
