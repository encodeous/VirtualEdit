package ca.encodeous.virtualedit.World;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.Data.IntervalTree2D;
import ca.encodeous.virtualedit.Data.PlayerCache;
import ca.encodeous.virtualedit.Utils.MaterialUtils;
import ca.encodeous.virtualedit.Utils.Vector2;
import ca.encodeous.virtualedit.VirtualWorld;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;

public class VirtualWorldView {
    private ArrayDeque<VirtualWorldLayer> layers;
    private final IntervalTree2D updates;
    private VirtualWorldChangeNotifier notifier;
    private Player player;
    private int updateId = 0;
    public VirtualWorldView(Player p){
        layers = new ArrayDeque<>();
        player = p;
        updates = new IntervalTree2D(0, Constants.MAX_CHUNKS_AXIS, 0, Constants.MAX_CHUNKS_AXIS);
        notifier = new VirtualWorldChangeNotifier() {
            @Override
            public Vector2 getViewCenter() {
                return new Vector2(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
            }

            @Override
            public int getViewDistance() {
                return Bukkit.getServer().getViewDistance() * 16;
            }

            @Override
            public void updateViewport(VirtualWorldLayer layer) {
                refreshViewport();
            }

            @Override
            public void markForChange(int x1, int x2, int z1, int z2) {
                synchronized (updates){
                    updates.Update(1, convert(x1), convert(x2), convert(z1), convert(z2), updateId++);
                }
            }

            private int convert(int x){
                return (x >> 4) + Constants.MAX_CHUNKS_AXIS / 2;
            }
        };
    }

    /**
     * Layers with smaller precedence values take priority
     */
    public void pushLayer(VirtualWorldLayer layer){
        layer.AddChangeNotifier(notifier);
        layers.add(layer);
        recalculateViewport();
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
        layers.clear();
    }

    public void refreshViewport(){
        VirtualWorld.Instance.PlayerUpdateQueue.offerAndLock(player);
    }

    public void recalculateViewport(){
        VirtualWorld.Instance.PlayerUpdateQueue.offerAndLock(player);
    }

    public Material processWorldView(Vector loc){
        for(VirtualWorldLayer layer : layers){
            Material mat = layer.GetMaterialAt(loc);
            if(mat != null){
                return mat;
            }
        }
        return null;
    }

    public Material processWorldView(int x, int y, int z){
        for(VirtualWorldLayer layer : layers){
            Material mat = layer.GetMaterialAt(x, y, z);
            if(mat != null){
                return mat;
            }
        }
        return null;
    }

    public int processWorldViewId(int x, int y, int z){
        for(VirtualWorldLayer layer : layers){
            int id = layer.GetBlockIdAt(x, y, z);
            if(id != Constants.DS_NULL_VALUE){
                return id;
            }
        }
        return Constants.DS_NULL_VALUE;
    }
    public int processWorldViewId(Vector loc){
        for(VirtualWorldLayer layer : layers){
            int id = layer.GetBlockIdAt(loc);
            if(id != Constants.DS_NULL_VALUE){
                return id;
            }
        }
        return Constants.DS_NULL_VALUE;
    }

    public LevelChunk processChunk(LevelChunk x, Level world){
        return new LevelChunk(x.level, x.getPos(), x.getUpgradeData(), new LevelChunkTicks<>(), new LevelChunkTicks<>(),
                x.getInhabitedTime(), transformSections(x, world), levelChunk -> {}, x.getBlendingData());
    }

    private LevelChunkSection[] transformSections(LevelChunk chunk, Level world){
        var sec = chunk.getSections();
        LevelChunkSection[] arr = new LevelChunkSection[world.getSectionsCount()];
        assert world.getSectionsCount() == sec.length;
        for(int i = 0; i < sec.length; i++){
            if(requireTransformations(chunk.getPos(), i, world.getMinBuildHeight())){
                var x = sec[i];
                arr[i] = applySectionTransformations(x, chunk.getPos(), i, world.getMinBuildHeight());
            }else{
                arr[i] = sec[i];
            }
        }
        return arr;
    }

    public boolean requireTransformations(ChunkPos pos, int secId, int mh){
        VirtualWorldView view = VirtualWorld.GetPlayerView(player.getUniqueId());
        var chunkX = pos.x;
        var chunkZ = pos.z;
        int mx = chunkX << 4, my = mh + (secId << 4), mz = chunkZ << 4;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    int id = view.processWorldViewId(i + mx, j + my, k + mz);
                    if (id != Constants.DS_NULL_VALUE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public LevelChunkSection applySectionTransformations(LevelChunkSection section, ChunkPos pos, int secId, int mh) {
        VirtualWorldView view = VirtualWorld.GetPlayerView(player.getUniqueId());

        var chunkX = pos.x;
        var chunkZ = pos.z;

        int mx = chunkX << 4, my = mh + (secId << 4), mz = chunkZ << 4;
        boolean isSingleValuePalette = true;
        int svpv = -2;
        svpCheck:
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    int id = view.processWorldViewId(i + mx, j + my, k + mz);
                    if(svpv == -2){
                        svpv = id;
                    }else if(svpv != id){
                        isSingleValuePalette = false;
                        if(id != -1){
                            svpv = id;
                            break svpCheck;
                        }
                    }
                }
            }
        }
        var cBlockState = (CraftBlockData)MaterialUtils.getMaterial(svpv).createBlockData();
        var pc = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, cBlockState.getState(), PalettedContainer.Strategy.SECTION_STATES);
        if(isSingleValuePalette){
            return new LevelChunkSection(section.bottomBlockY() >> 4, pc, (PalettedContainer<Holder<Biome>>) section.getBiomes());
        }
        var biomes = (PalettedContainer<Holder<Biome>>)section.getBiomes();
        var newState = new LevelChunkSection(section.bottomBlockY() >> 4, pc, biomes.copy());
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    int id = view.processWorldViewId(i + mx, j + my, k + mz);
                    if (id != Constants.DS_NULL_VALUE) {
                        var state = (CraftBlockData)MaterialUtils.getMaterial(id).createBlockData();
                        newState.states.getAndSetUnchecked(i, j, k, state.getState());
                    }else{
                        newState.states.getAndSetUnchecked(i, j, k, section.states.get(i, j, k));
                    }
                }
            }
        }
        newState.recalcBlockCounts();
        return newState;
    }
}
