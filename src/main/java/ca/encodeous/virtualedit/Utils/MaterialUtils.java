package ca.encodeous.virtualedit.Utils;

import ca.encodeous.virtualedit.Constants;
import net.imprex.orebfuscator.NmsInstance;
import org.bukkit.Material;

import java.util.concurrent.ConcurrentHashMap;

public class MaterialUtils {
    private static final ConcurrentHashMap<Material, Integer> materialMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Material> idMap = new ConcurrentHashMap<>();
    public static int GetId(Material mat){
        if(mat == null) return Constants.DS_NULL_VALUE;
        int id = materialMap.getOrDefault(mat, NmsInstance.getFirstBlockId(mat).get());
        materialMap.putIfAbsent(mat, id);
        idMap.putIfAbsent(id, mat);
        return id;
    }
    public static Material GetMaterial(int id){
        Material mat = idMap.getOrDefault(id, null);
        return mat;
    }
}
