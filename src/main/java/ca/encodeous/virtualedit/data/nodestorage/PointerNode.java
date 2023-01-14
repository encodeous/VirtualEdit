package ca.encodeous.virtualedit.data.nodestorage;

import ca.encodeous.virtualedit.world.ResourceCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.bukkit.World;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;

public class PointerNode implements NodeStorable<PointerNode>{

    public World world;
    public int worldX;

    public PointerNode(World world, int worldX, int worldY, int worldZ, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize) {
        this.world = world;
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    public int worldY;
    public int worldZ;
    public int xOffset, yOffset, zOffset;
    public int xSize, ySize, zSize;
    /**
     * Resolves the referenced block from relative coordinates
     */
    public BlockState resolvePointer(int x, int y, int z){
        if(x >= xSize || y >= ySize || z >= zSize){
            throw new RuntimeException("Tried to resolve a pointer coordinate that does not exist within the bounds of this pointer.");
        }
        var level = (CraftWorld)world;
        return level.getHandle().getBlockState(new BlockPos(x + xOffset, y + yOffset, z + zOffset));
    }
    /**
     * Resolves the referenced block from relative coordinates
     */
    public BlockState resolvePointer(int x, int y, int z, ResourceCache cache){
        if(x >= xSize || y >= ySize || z >= zSize){
            throw new RuntimeException("Tried to resolve a pointer coordinate that does not exist within the bounds of this pointer.");
        }
        return cache.evaluateIfNotCached((pos) ->{
            var level = ((CraftWorld)world).getHandle();
            ChunkAccess chunk = level.getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
            return chunk.getSections();
        }, world, x + xOffset, y + yOffset, z + zOffset);
    }
    @Override
    public PointerNode getValue() {
        return null;
    }
}
