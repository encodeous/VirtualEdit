package ca.encodeous.virtualedit.data.nodestorage;

import net.minecraft.world.level.block.Block;
import org.bukkit.Material;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;

import java.util.Objects;

public class BlockNode implements NodeStorable<BlockState>{
    private BlockState state;
    public BlockNode(Material mat) {
        this.state = CraftMagicNumbers.getBlock(mat).defaultBlockState();
    }
    public BlockNode(BlockState data){
        this.state = data;
    }

    @Override
    public BlockState getValue() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockNode blockNode = (BlockNode) o;
        return state.equals(blockNode.state);
    }

    @Override
    public int hashCode() {
        return Block.getId(state);
    }
}
