package ca.encodeous.virtualedit.World;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class VirtualWorldSession implements Closeable {
    private HashSet<VirtualWorldModification> Changes;
    private VirtualWorldLayer worldLayer;
    VirtualWorldSession(VirtualWorldLayer layer){
        Changes = new HashSet<>();
        worldLayer = layer;
    }

    /**
     * Sets a single block
     */
    public void SetBlock(Vector location, Material mat){
        int x1 = location.getBlockX(), x2 = location.getBlockX(), y1 = location.getBlockY(),
                y2 = location.getBlockY(), z1 = location.getBlockZ(), z2 = location.getBlockZ();
        Changes.add(new VirtualWorldModification(x1, x2, y1, y2, z1, z2, mat));
    }

    /**
     * Sets a 3d cuboid region to a specific material
     */
    public void SetRange(int x1, int x2, int y1, int y2, int z1, int z2, Material mat){
        Changes.add(new VirtualWorldModification(x1, x2, y1, y2, z1, z2, mat));
    }

    /**
     * Sets a 3d cuboid region to a specific material
     */
    public void SetRange(Vector location1, Vector location2, Material mat){
        Changes.add(new VirtualWorldModification(location1.getBlockX(), location2.getBlockX(), location1.getBlockY(),
                location2.getBlockY(), location1.getBlockZ(), location2.getBlockZ(), mat));
    }

    /**
     * Clears a 3d cuboid region
     */
    public void ClearRange(int x1, int x2, int y1, int y2, int z1, int z2){
        Changes.add(new VirtualWorldModification(x1, x2, y1, y2, z1, z2, null));
    }
    /**
     * Clears a 3d cuboid region
     */
    public void ClearRange(Vector location1, Vector location2){
        Changes.add(new VirtualWorldModification(location1.getBlockX(), location2.getBlockX(), location1.getBlockY(),
                location2.getBlockY(), location1.getBlockZ(), location2.getBlockZ(), null));
    }

    /**
     * Get all changes made in the current session
     */
    public VirtualWorldModification[] GetChanges(){
        return Changes.toArray(new VirtualWorldModification[Changes.size()]);
    }

    /**
     * Removes a specific change returned from GetChanges
     */
    public void RemoveChange(VirtualWorldModification change){
        Changes.remove(change);
    }

    @Override
    public void close() throws IOException {
        worldLayer.ApplyChanges(GetChanges());
        Changes.clear();
    }
}
