package ca.encodeous.virtualedit.world;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ResourceCache {
    public final ConcurrentHashMap<World, ChunkCache> worlds = new ConcurrentHashMap<>();

    public BlockState evaluateIfNotCached(Function<ChunkPos, LevelChunkSection[]> func, World world, int x, int y, int z){
        if(!worlds.containsKey(world)){
            worlds.put(world, new ChunkCache());
        }
        var cache = worlds.get(world);
        var pos = new ChunkPos(x >> 4, z >> 4);
        if(!cache.chunks.containsKey(pos)){
            cache.chunks.put(pos, func.apply(pos));
        }
        var sections = cache.chunks.get(pos);
        var mh = ((CraftWorld)world).getHandle().getMinBuildHeight();
        y -= mh;
        return sections[y >> 4].getBlockState(x & 0b1111, y & 0b1111, z & 0b1111);
    }
}
