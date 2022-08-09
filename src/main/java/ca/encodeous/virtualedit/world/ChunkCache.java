package ca.encodeous.virtualedit.world;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.concurrent.ConcurrentHashMap;

public class ChunkCache {
    public final ConcurrentHashMap<ChunkPos, LevelChunkSection[]> chunks = new ConcurrentHashMap<>();
}
