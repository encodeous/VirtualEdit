package ca.encodeous.virtualedit.Protocol;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.Utils.MCVersion;
import ca.encodeous.virtualedit.Utils.MaterialUtils;
import ca.encodeous.virtualedit.VirtualWorld;
import ca.encodeous.virtualedit.World.VirtualWorldView;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualEditViewThread extends Thread{
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Plugin cPlugin;
    private int mcv = MCVersion.QueryVersion().getValue();
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
                    World bw = player.getWorld();
//                    ClientboundLevelChunkPacketData packet = new ClientboundLevelChunkPacketData();
//                    net.minecraft.world.level.World mcWorld = (net.minecraft.world.level.World) bw;
//                    com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
//                    boolean isOverworld = bw.getEnvironment() == World.Environment.NORMAL;
//                    com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bw);
//                    VirtualWorldView view = VirtualWorld.GetPlayerView(player.getUniqueId());
//
//                    Location location = player.getLocation();
//
//                    int distance = Bukkit.getViewDistance();
//
//                    int minChunkX = (location.getBlockX() - distance) >> 4;
//                    int maxChunkX = (location.getBlockX() + distance) >> 4;
//                    int minChunkZ = (location.getBlockZ() - distance) >> 4;
//                    int maxChunkZ = (location.getBlockZ() + distance) >> 4;
//
//
//                    int minChunkY = (world.getMinY()) >> 4;
//                    int maxChunkY = (world.getMaxY()) >> 4;
//
//                    view.CullChunks();
//
//                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
//                        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
//                            if(!view.getFlaggedUpdates().IsChunkLoaded(chunkX, chunkZ)){
//                                FastByteArrayOutputStream fbao = new FastByteArrayOutputStream();
//                                FaweOutputStream fos = new FaweOutputStream(fbao);
//                                ChunkWriter cw = new ChunkWriter(fos, minChunkY, maxChunkY);
//                                IChunkGet cg = world.get(chunkX, chunkZ);
//                                for(int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++){
//                                    if(!view.getFlaggedUpdates().IsSChunkLoaded(chunkX, chunkY, chunkZ)){
//                                        int mx = chunkX << 4, my = chunkY << 4, mz = chunkZ << 4;
//                                        for(int i = 0; i < 16; i++){
//                                            for(int j = 0; j < 16; j++){
//                                                for(int k = 0; k <= 16; k++){
//                                                    int id = view.ProcessWorldViewId(new Vector(i + mx, j + my, k + mz));
//                                                    if(id == Constants.DS_NULL_VALUE){
//                                                        cw.SetBlock(i, j + my, k, MaterialUtils.GetId(
//                                                                MaterialUtils.FromWe(cg.getBlock(i, j + my, k)
//                                                                        .getBlockType())));
//                                                    }
//                                                    else{
//                                                        cw.SetBlock(i, j + my, k, id);
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        view.getFlaggedUpdates().LoadSChunk(chunkX, chunkY, chunkZ);
//                                    }
//                                }
//                                cw.Finish(chunkX, chunkZ, isOverworld);
//                                byte[] data = fbao.toByteArray();
//
//                                VirtualWorld.Protocol.sendWirePacket(player, new WirePacket(PacketType.Play.Server.MAP_CHUNK, data));
//                            }
//                        }
//                    }
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
