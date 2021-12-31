package ca.encodeous.virtualedit.World;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.Data.PlayerCache;
import ca.encodeous.virtualedit.Utils.Vector2;
import ca.encodeous.virtualedit.VirtualWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.TreeMap;

public class VirtualWorldView {
    private TreeMap<Integer, VirtualWorldLayer> layers;

    public PlayerCache getFlaggedUpdates() {
        return flaggedUpdates;
    }

    private PlayerCache flaggedUpdates;
    private VirtualWorldChangeNotifier notifier;
    private Player player;
    public VirtualWorldView(Player p){
        flaggedUpdates = new PlayerCache(p);
        layers = new TreeMap<>();
        player = p;
        notifier = new VirtualWorldChangeNotifier() {
            @Override
            public Vector2 GetViewCenter() {
                return new Vector2(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
            }

            @Override
            public int GetViewDistance() {
                return Bukkit.getServer().getViewDistance() * 16;
            }

            @Override
            public void UpdateViewport(VirtualWorldLayer layer) {
                RefreshViewport();
            }
        };
    }

    /**
     * Layers with smaller precedence values take priority
     */
    public void AddLayer(VirtualWorldLayer layer, int precedence){
        layer.AddChangeNotifier(notifier);
        layers.put(precedence, layer);
        RefreshViewport();
    }
    public void close(){
        for(VirtualWorldLayer layer : layers.values()){
            layer.RemoveChangeNotifier(notifier);
        }
        flaggedUpdates.cull();
        layers.clear();
    }
    public void CullChunks(){
        flaggedUpdates.cullDistance();
    }

    public void RefreshViewport(){
//        int vd = Bukkit.getViewDistance() + 1;
//        int x = player.getLocation().getChunk().getX(), z = player.getLocation().getChunk().getZ();
//        for(int i = -vd; i <= vd; i++){
//            for(int j = -vd; j <= vd; j++){
//                flaggedUpdates.flagChunk(x + i, z + i, player.getWorld(), this);
//            }
//        }
        VirtualWorld.Instance.PlayerUpdateQueue.offerAndLock(player);
    }

    public Material ProcessWorldView(Vector loc){
        for(VirtualWorldLayer layer : layers.values()){
            Material mat = layer.GetMaterialAt(loc);
            if(mat != null){
                return mat;
            }
        }
        return null;
    }
    public int ProcessWorldViewId(Vector loc){
        for(VirtualWorldLayer layer : layers.values()){
            int id = layer.GetBlockIdAt(loc);
            if(id != Constants.DS_NULL_VALUE){
                return id;
            }
        }
        return Constants.DS_NULL_VALUE;
    }

    public int QueryLayers(Vector loc){
        for(VirtualWorldLayer layer : layers.values()){
            int id = layer.GetBlockIdAt(loc);
            if(id != Constants.DS_NULL_VALUE){
                return id;
            }
        }
        return Constants.DS_NULL_VALUE;
    }
}
