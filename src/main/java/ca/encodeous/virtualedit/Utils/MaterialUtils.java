package ca.encodeous.virtualedit.Utils;

import ca.encodeous.virtualedit.Constants;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;

import java.util.concurrent.ConcurrentHashMap;

public class MaterialUtils {
    private static final ConcurrentHashMap<Material, Integer> materialMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Material> idMap = new ConcurrentHashMap<>();
    private static int idCnt = 0;
    public static int getId(Material mat) {
        if (mat == null) return Constants.DS_NULL_VALUE;
        int id = materialMap.getOrDefault(mat, -1);
        if (id == -1) {
            synchronized (materialMap) {
                id = idCnt++;
            }
        }
        materialMap.putIfAbsent(mat, id);
        idMap.putIfAbsent(id, mat);
        return id;
    }
    public static Material getMaterial(int id){
        Material mat = idMap.getOrDefault(id, null);
        return mat;
    }
}
