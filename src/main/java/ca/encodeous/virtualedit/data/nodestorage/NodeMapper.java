package ca.encodeous.virtualedit.data.nodestorage;

import ca.encodeous.virtualedit.Constants;
import org.bukkit.Material;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class NodeMapper {
    public static final NodeMapper GLOBAL_MAPPER = new NodeMapper();
    public final ConcurrentHashMap<NodeStorable, Integer> toId = new ConcurrentHashMap<>();
    public final ArrayList<NodeStorable> toNode = new ArrayList<>();
    private volatile int idCnt;

    public int getId(Material mat) {
        if (mat == null) return Constants.DS_NULL_VALUE;
        return getId(new BlockNode(mat));
    }
    public int getId(BlockState state) {
        if (state == null) return Constants.DS_NULL_VALUE;
        return getId(new BlockNode(state));
    }
    public int getId(NodeStorable node){
        int id = toId.getOrDefault(node, -1);
        if (id == -1) {
            synchronized (toId) {
                id = idCnt++;
                toNode.add(node);
            }
        }
        toId.putIfAbsent(node, id);
        return id;
    }
    public Material getMaterial(int id){
        if(id < 0) return null;
        return ((BlockNode)toNode.get(id)).getValue().getBukkitMaterial();
    }
    public NodeStorable getNode(int id){
        if(id < 0) return null;
        return toNode.get(id);
    }
}
