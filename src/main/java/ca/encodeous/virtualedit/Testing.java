package ca.encodeous.virtualedit;

import ca.encodeous.virtualedit.Data.IntervalTree2D;
import ca.encodeous.virtualedit.Utils.MaterialUtils;
import ca.encodeous.virtualedit.World.VirtualWorldLayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class Testing {
    public static void main(String[] args){
        VirtualWorldLayer layer = new VirtualWorldLayer();

        layer.vWorld.Update(-10, 10, 0, 10, -10, 5, 1);

        layer.vWorld.Query(-10, 1, 1);

        System.out.println(layer.GetBlockIdAt(-10, 0, -10));
        System.out.println(layer.GetBlockIdAt(-10, 4, -10));
        System.out.println(layer.GetBlockIdAt(0, 0, -10));
    }
}
