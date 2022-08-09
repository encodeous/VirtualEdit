package ca.encodeous.virtualedit.data.nodestorage;

import net.minecraft.core.BlockPos;
import org.bukkit.World;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

public class PointerNode implements NodeStorable<PointerNode>{
    public PointerNode(World world, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize) {
        this.world = world;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    public World world;
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
    @Override
    public PointerNode getValue() {
        return null;
    }
}
