package ca.encodeous.virtualedit.Data;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.World.VirtualWorldView;
import net.imprex.orebfuscator.NmsInstance;
import net.imprex.orebfuscator.util.BlockPos;
import net.imprex.orebfuscator.util.ChunkPosition;
import net.imprex.orebfuscator.util.HeightAccessor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache {
    private final Map<ChunkPosition, Set<BlockPos>> chunks = new ConcurrentHashMap<>();
    private final Player player;
    public PlayerCache(Player p) {
        this.player = p;
    }

    public void flagBlockChange(Vector loc){
        Set<BlockPos> chunk = getChunk(loc.getBlockX() / 16, loc.getBlockZ() / 16);
        if(chunk == null){
            chunk = addChunk(loc.getBlockX() / 16, loc.getBlockZ() / 16, new HashSet<>());
        }
        chunk.add(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    public void unflagBlockChange(Vector loc){
        Set<BlockPos> chunk = getChunk(loc.getBlockX() / 16, loc.getBlockZ() / 16);
        if(chunk == null){
            return;
        }
        chunk.remove(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        if(chunk.isEmpty()){
            unflagChunk(loc.getBlockX() / 16, loc.getBlockZ() / 16);
        }
    }

    public boolean isBlockFlagged(Vector loc){
        Set<BlockPos> chunk = getChunk(loc.getBlockX() / 16, loc.getBlockZ() / 16);
        if(chunk == null){
            return false;
        }
        return chunk.contains(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    public void flagChunk(int chunkX, int chunkZ, World world, VirtualWorldView vwView){
        int x = chunkX * 16, z = chunkZ * 16;
        HeightAccessor heightAccessor = HeightAccessor.get(world);
        Set<BlockPos> pos = new HashSet<>();
        for(int i = 0; i < 16; i++){
            for(int j = 0; j < 16; j++){
                for(int k = heightAccessor.getMinBuildHeight(); k <= heightAccessor.getMaxBuildHeight(); k++){
                    if(vwView.QueryLayers(new Vector(i, j, k)) != Constants.DS_NULL_VALUE){
                        pos.add(new BlockPos(i + x, k, j + z));
                    }
                }
            }
        }
        if(pos.isEmpty()) return;
        ChunkPosition key = new ChunkPosition(Bukkit.getWorlds().get(0), chunkX, chunkZ);
        chunks.put(key, pos);
    }

    public Set<BlockPos> addChunk(int chunkX, int chunkZ, Set<BlockPos> blocks) {
        ChunkPosition key = new ChunkPosition(Bukkit.getWorlds().get(0), chunkX, chunkZ);
        chunks.put(key, blocks);
        return blocks;
    }

    public boolean isChunkFlagged(int chunkX, int chunkZ) {
        ChunkPosition key = new ChunkPosition(Bukkit.getWorlds().get(0), chunkX, chunkZ);
        return chunks.containsKey(key);
    }

    public Set<BlockPos> getChunk(int chunkX, int chunkZ) {
        ChunkPosition key = new ChunkPosition(Bukkit.getWorlds().get(0), chunkX, chunkZ);
        return this.chunks.get(key);
    }

    public void cullDistance(){
        int d = Bukkit.getViewDistance();
        int x = player.getLocation().getChunk().getX(), z = player.getLocation().getChunk().getZ();
        for(ChunkPosition val : chunks.keySet()){
            if (Math.abs(val.getX() - x) > d || Math.abs(val.getZ() - z) > d) {
                unflagChunk(val.getX(), val.getZ());
            }
        }
    }

    public void cull(){
        chunks.clear();
    }


    public void unflagChunk(int chunkX, int chunkZ) {
        ChunkPosition key = new ChunkPosition(Bukkit.getWorlds().get(0), chunkX, chunkZ);
        this.chunks.remove(key);
    }
}
