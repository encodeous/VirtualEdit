package ca.encodeous.virtualedit.World;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.Data.PlayerCache;
import ca.encodeous.virtualedit.Utils.MaterialUtils;
import ca.encodeous.virtualedit.Utils.Vector2;
import ca.encodeous.virtualedit.VirtualWorld;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChunkCoordinate;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.TreeMap;

public class VirtualWorldView {
    private ArrayDeque<VirtualWorldLayer> layers;

    public PlayerCache getFlaggedUpdates() {
        return flaggedUpdates;
    }

    private PlayerCache flaggedUpdates;
    private VirtualWorldChangeNotifier notifier;
    private Player player;
    public VirtualWorldView(Player p){
        flaggedUpdates = new PlayerCache(p);
        layers = new ArrayDeque<>();
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
    public void pushLayer(VirtualWorldLayer layer){
        layer.AddChangeNotifier(notifier);
        layers.add(layer);
        RecalculateViewport();
    }
    public VirtualWorldLayer popLayer(){
        var layer = layers.pop();
        layer.RemoveChangeNotifier(notifier);
        return layer;
    }
    public void close(){
        for(VirtualWorldLayer layer : layers){
            layer.RemoveChangeNotifier(notifier);
        }
        flaggedUpdates.Clear();
        layers.clear();
    }
    public void CullChunks(){
        flaggedUpdates.CullDistance();
    }

    public void RefreshViewport(){
        VirtualWorld.Instance.PlayerUpdateQueue.offerAndLock(player);
    }

    public void RecalculateViewport(){
        flaggedUpdates.Clear();
        VirtualWorld.Instance.PlayerUpdateQueue.offerAndLock(player);
    }

    public Material ProcessWorldView(Vector loc){
        for(VirtualWorldLayer layer : layers){
            Material mat = layer.GetMaterialAt(loc);
            if(mat != null){
                return mat;
            }
        }
        return null;
    }

    public int ProcessWorldViewId(int x, int y, int z){
        for(VirtualWorldLayer layer : layers){
            int id = layer.GetBlockIdAt(x, y, z);
            if(id != Constants.DS_NULL_VALUE){
                return id;
            }
        }
        return Constants.DS_NULL_VALUE;
    }
    public int ProcessWorldViewId(Vector loc){
        for(VirtualWorldLayer layer : layers){
            int id = layer.GetBlockIdAt(loc);
            if(id != Constants.DS_NULL_VALUE){
                return id;
            }
        }
        return Constants.DS_NULL_VALUE;
    }

    public LevelChunk ProcessChunk(LevelChunk x){
        return new LevelChunk(x.level, x.getPos(), x.getUpgradeData(), new LevelChunkTicks<>(), new LevelChunkTicks<>(),
                x.getInhabitedTime(), transformSections(x), levelChunk -> {}, x.getBlendingData());
    }

    private LevelChunkSection[] transformSections(LevelChunk chunk){
        var sec = chunk.getSections();
        LevelChunkSection[] arr = new LevelChunkSection[sec.length];
        for(int i = 0; i < sec.length; i++){
            var x = sec[i];
//            var start = System.currentTimeMillis();
            var biomes = (PalettedContainer<Holder<Biome>>)x.getBiomes();
            arr[i] = new LevelChunkSection(x.bottomBlockY() >> 4, x.states.copy(), biomes.copy());
//            System.out.println("COPY:" + (System.currentTimeMillis() - start));
            applySectionTransformations(arr[i], chunk.getPos(), i);
        }
        return arr;
    }

    public void applySectionTransformations(LevelChunkSection section, ChunkPos pos, int secId) {
        VirtualWorldView view = VirtualWorld.GetPlayerView(player.getUniqueId());

        var chunkX = pos.x;
        var chunkZ = pos.z;

        int mx = chunkX << 4, my = section.bottomBlockY(), mz = chunkZ << 4;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
//                        var state = Blocks.REDSTONE_BLOCK.defaultBlockState();
//                        section.states.getAndSetUnchecked(i, j, k, state);
                    int id = view.ProcessWorldViewId(i + mx, j + my, k + mz);
                    if (id != Constants.DS_NULL_VALUE) {
                        var state = Blocks.REDSTONE_BLOCK.defaultBlockState();
                        section.states.getAndSetUnchecked(i, j, k, state);
                    }
                }
            }
        }
    }
}
