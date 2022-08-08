package ca.encodeous.virtualedit.Data;

import ca.encodeous.virtualedit.Utils.DataUtils;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache {
    private final ConcurrentHashMap<Long, Integer> chunks = new ConcurrentHashMap<>();
    private final Player player;
    public PlayerCache(Player p) {
        this.player = p;
    }

    public boolean IsSChunkLoaded(int x, int y, int z){
        synchronized (chunks){
            long k = DataUtils.GetIntTuple(x, z);
            if(!chunks.containsKey(k)) return false;
            int cg = chunks.get(k);
            return ((cg >> y) & 1) == 1;
        }
    }

    public void LoadSChunk(int x, int y, int z){
        synchronized (chunks){
            long k = DataUtils.GetIntTuple(x, z);
            int v = chunks.getOrDefault(k, 0);
            v |= (1 << y);
            chunks.put(k, v);
        }
    }

    public void UnloadSChunk(int x, int y, int z){
        synchronized (chunks){
            long k = DataUtils.GetIntTuple(x, z);
            if(chunks.containsKey(k)){
                int v = chunks.getOrDefault(k, 0);
                v &= ~(1 << y);
                chunks.put(k, v);
            }
        }
    }

    public boolean IsChunkLoaded(int x, int z){
        synchronized (chunks){
            long k = DataUtils.GetIntTuple(x, z);
            if(!chunks.containsKey(k)) return false;
            return Long.bitCount(k) >= 1;
        }
    }

    public void UnloadChunk(int x, int z){
        chunks.remove(DataUtils.GetIntTuple(x, z));
    }

    public void CullDistance(){
        synchronized (chunks){
            int d = Bukkit.getViewDistance();
            int x = player.getLocation().getChunk().getX(), z = player.getLocation().getChunk().getZ();
            for(long val : chunks.keySet()){
                int cx = DataUtils.TGa(val), cz = DataUtils.TGb(val);
                if (Math.abs(cx - x) >= d || Math.abs(cz - z) >= d) {
                    UnloadChunk(cx, cz);
                }
            }
        }
    }
    public void Clear(){
        chunks.clear();
    }
}
