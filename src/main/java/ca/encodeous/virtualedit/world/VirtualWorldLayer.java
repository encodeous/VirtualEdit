package ca.encodeous.virtualedit.world;

import ca.encodeous.virtualedit.data.Virtual3DWorld;
import ca.encodeous.virtualedit.data.nodestorage.BlockNode;
import ca.encodeous.virtualedit.data.nodestorage.NodeMapper;
import ca.encodeous.virtualedit.data.nodestorage.NodeStorable;
import ca.encodeous.virtualedit.data.nodestorage.PointerNode;
import ca.encodeous.virtualedit.utils.DataUtils;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class VirtualWorldLayer {
    private HashSet<VirtualWorldChangeNotifier> subscribers;
    public final Virtual3DWorld world;
    public final NodeMapper mapper;
    public int xSize, ySize, zSize;
    public int xOffset, yOffset, zOffset;
    public VirtualWorldLayer(NodeMapper mapper, int xSize, int ySize, int zSize){
        subscribers = new HashSet<>();
        world = new Virtual3DWorld(xSize, ySize, zSize);
        this.mapper = mapper;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    public VirtualWorldLayer(int xSize, int ySize, int zSize){
        this(NodeMapper.GLOBAL_MAPPER, xSize, ySize, zSize);
    }

    public void translate(int newX, int newY, int newZ){
        for(VirtualWorldChangeNotifier notifier : subscribers){
            notifier.markForChange(xOffset, xOffset + xSize, zOffset, zOffset + zSize);
        }
        xOffset = newX;
        yOffset = newY;
        zOffset = newZ;
        for(VirtualWorldChangeNotifier notifier : subscribers){
            notifier.markForChange(xOffset, xOffset + xSize, zOffset, zOffset + zSize);
        }
    }

    /**
     * Gets the node value located at the specified location
     * @return A node value
     */
    public int getNode(Vector location){
        return getNode(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Gets the node value located at the specified location
     * @return A node value
     */
    public int getNode(int x, int y, int z){
        return world.Query(x, y, z);
    }

    public BlockState getBlock(Vector location){
        return getBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockState getBlock(int x, int y, int z){
        var result = mapper.getNode(world.Query(x, y, z));
        if(result == null) return null;
        if(result instanceof BlockNode bn){
            return bn.getValue();
        }
        else if(result instanceof PointerNode pn){
            return pn.resolvePointer(x - pn.worldX, y - pn.worldY, z - pn.worldZ);
        }
        return null;
    }

    public BlockState getBlock(int x, int y, int z, ResourceCache cache){
        var result = mapper.getNode(world.Query(x, y, z));
        if(result == null) return null;
        if(result instanceof BlockNode bn){
            return bn.getValue();
        }
        else if(result instanceof PointerNode pn){
            return pn.resolvePointer(x - pn.worldX, y - pn.worldY, z - pn.worldZ, cache);
        }
        return null;
    }

    public void setNode(int x1, int x2, int y1, int y2, int z1, int z2, NodeStorable node){
        world.Update(x1, x2, y1, y2, z1, z2, mapper.getId(node));
    }
    public void setBlock(int x1, int x2, int y1, int y2, int z1, int z2, Material material){
        world.Update(x1, x2, y1, y2, z1, z2, mapper.getId(material));
    }
    public void setPointer(World world, int x, int y, int z, int pointerX, int pointerY, int pointerZ, int xSize, int ySize, int zSize){
        setNode(x, x + xSize - 1, y, y + ySize - 1, z, z + zSize - 1, new PointerNode(world, x, y, z, pointerX, pointerY, pointerZ, xSize, ySize, zSize));
    }

    public void subscribe(VirtualWorldChangeNotifier notifier){
        subscribers.add(notifier);
    }

    public void unsubscribe(VirtualWorldChangeNotifier notifier){
        subscribers.remove(notifier);
    }

    public void syncToClient(){
        for(VirtualWorldChangeNotifier notifier : subscribers){
            notifier.updateViewport(this);
        }
    }
}
