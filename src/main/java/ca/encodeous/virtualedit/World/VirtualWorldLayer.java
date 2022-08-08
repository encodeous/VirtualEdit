package ca.encodeous.virtualedit.World;

import ca.encodeous.virtualedit.Data.Virtual3DWorld;
import ca.encodeous.virtualedit.Utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class VirtualWorldLayer {
    private HashSet<VirtualWorldChangeNotifier> Notifiers;
    public final Virtual3DWorld vWorld;
    public VirtualWorldLayer(){
        Notifiers = new HashSet<>();
        vWorld = new Virtual3DWorld();
    }

    /**
     * Gets the block id (protocol id) located at the specified location
     * @return Returns -1 if there is no block
     */
    public int GetBlockIdAt(Vector location){
        return GetBlockIdAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Gets the block id (protocol id) located at the specified location
     * @return Returns -1 if there is no block
     */
    public int GetBlockIdAt(int x, int y, int z){
//        if(y <= 100){
//            return MaterialUtils.getId(Material.REDSTONE_BLOCK);
//        }else{
//            return -1;
//        }
        return vWorld.Query(x, y, z);
    }

    /**
     * Gets the material located at the specified location
     * @return Returns null if there is no block
     */
    public Material GetMaterialAt(Vector location){
        return GetMaterialAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Gets the material located at the specified location
     * @return Returns null if there is no block
     */
    public Material GetMaterialAt(int x, int y, int z){
//        if(y <= 100){
//            return Material.REDSTONE_BLOCK;
//        }else{
//            return null;
//        }
        return MaterialUtils.getMaterial(vWorld.Query(x, y, z));
    }

    /**
     * Gets a session class that can modify the data structure
     */
    public VirtualWorldSession GetSession(){
        return new VirtualWorldSession(this);
    }

    /**
     * Adds a notifier to the layer
     */
    public void AddChangeNotifier(VirtualWorldChangeNotifier notifier){
        Notifiers.add(notifier);
    }

    /**
     * Removes a notifier from the layer
     */
    public void RemoveChangeNotifier(VirtualWorldChangeNotifier notifier){
        Notifiers.remove(notifier);
    }

    void ApplyChanges(VirtualWorldModification[] changes){
        HashSet<VirtualWorldChangeNotifier> notifications = new HashSet<>();
        for(VirtualWorldModification change : changes){
            vWorld.Update(change.x1, change.x2, change.y1, change.y2, change.z1, change.z2, MaterialUtils.getId(change.mat));
            for(VirtualWorldChangeNotifier notifier : Notifiers){
                if(notifier.CheckOverlap(change.GetXZ1(), change.GetXZ2())){
                    notifications.add(notifier);
                }
            }
        }
        for(VirtualWorldChangeNotifier notif : notifications){
            notif.updateViewport(this);
        }
    }
}
