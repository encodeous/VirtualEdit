package ca.encodeous.virtualedit.world;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.data.IntervalTree2D;
import ca.encodeous.virtualedit.VirtualWorld;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Objects;

import static ca.encodeous.virtualedit.utils.DataUtils.convertToChunk;

public class VirtualWorldView {
    private ArrayDeque<VirtualWorldLayer> layers;
    private final IntervalTree2D updates;
    private VirtualWorldChangeNotifier notifier;
    public World world;
    private Player player;
    private int updateId = 0;
    public VirtualWorldView(Player p, World world){
        layers = new ArrayDeque<>();
        player = p;
        this.world = world;
        updates = new IntervalTree2D(0, Constants.MAX_CHUNKS_AXIS, 0, Constants.MAX_CHUNKS_AXIS);
        notifier = new VirtualWorldChangeNotifier() {
            @Override
            public void updateViewport(VirtualWorldLayer layer) {
                refreshWorldView();
            }

            @Override
            public void markForChange(int x1, int x2, int z1, int z2) {
                markFor(convertToChunk(x1), convertToChunk(x2), convertToChunk(z1), convertToChunk(z2), Constants.UPDATE);
            }
        };
    }
    public void markWorldForChange(){
        markFor(0, Constants.MAX_CHUNKS_AXIS, 0, Constants.MAX_CHUNKS_AXIS, Constants.UPDATE);
    }
    public void markFor(int x1, int x2, int z1, int z2, int value) {
        synchronized (updates){
            updates.Update(value, convertToChunk(x1), convertToChunk(x2), convertToChunk(z1), convertToChunk(z2), updateId++);
        }
    }
    public void pushLayer(VirtualWorldLayer layer){
        layer.subscribe(notifier);
        layers.push(layer);
        markWorldForChange();
        refreshWorldView();
    }
    public VirtualWorldLayer peekLayer(){
        return layers.peekFirst();
    }
    public VirtualWorldLayer popLayer(){
        var layer = layers.pop();
        layer.unsubscribe(notifier);
        markWorldForChange();
        refreshWorldView();
        return layer;
    }
    public void close(){
        for(VirtualWorldLayer layer : layers){
            layer.unsubscribe(notifier);
        }
        layers.clear();
    }

    public void refreshWorldView(){
        VirtualWorld.Instance.PlayerUpdateQueue.offerAndLock(player);
    }

    public BlockState processWorldView(Vector loc){
        for(VirtualWorldLayer layer : layers){
            BlockState block = layer.getBlock(loc.getBlockX() - layer.xOffset, loc.getBlockY() - layer.yOffset, loc.getBlockZ() - layer.zOffset);
            if(block != null){
                return block;
            }
        }
        return null;
    }

    public BlockState processWorldView(int x, int y, int z, ResourceCache cache){
        for(VirtualWorldLayer layer : layers){
            var block = layer.getBlock(x - layer.xOffset, y - layer.yOffset, z - layer.zOffset, cache);
            if(block != null){
                return block;
            }
        }
        return null;
    }

    public LevelChunk processChunk(int x, int z){
        var level = ((CraftWorld)world).getHandle();
        var chunk = level.getChunk(x, z);
        return new LevelChunk(chunk.level, chunk.getPos(), chunk.getUpgradeData(), new LevelChunkTicks<>(), new LevelChunkTicks<>(),
                chunk.getInhabitedTime(), transformSections(chunk, level), levelChunk -> {}, chunk.getBlendingData());
    }

    private LevelChunkSection[] transformSections(LevelChunk chunk, Level world){
        var sec = chunk.getSections();
        LevelChunkSection[] arr = new LevelChunkSection[world.getSectionsCount()];
        assert world.getSectionsCount() == sec.length;
        var cache = new ResourceCache();
        for(int i = 0; i < sec.length; i++){
            var section = getSection(chunk.getPos(), i, world.getMinBuildHeight(), cache);
            if(requireTransformations(section)){
                var x = sec[i];
                arr[i] = applySectionTransformations(x, section);
            }else{
                arr[i] = sec[i];
            }
        }
        return arr;
    }
    public BlockState[][][] getSection(ChunkPos pos, int secId, int mh, ResourceCache cache){
        var cached = new BlockState[16][16][16];
        var chunkX = pos.x;
        var chunkZ = pos.z;
        int mx = chunkX << 4, my = mh + (secId << 4), mz = chunkZ << 4;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    cached[i][j][k] = processWorldView(i + mx, j + my, k + mz, cache);
                }
            }
        }
        return cached;
    }
    public boolean requireTransformations(BlockState[][][] view){
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    var state = view[i][j][k];
                    if (state != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public LevelChunkSection applySectionTransformations(LevelChunkSection section, BlockState[][][] view) {
        boolean isSingleValuePalette = true;
        BlockState svpv = null;
        svpCheck:
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    var state = view[i][j][k];
                    if(i == j && j == k && k == 0){
                        svpv = state;
                    }else if(!Objects.equals(svpv, state)){
                        isSingleValuePalette = false;
                        if(state != null){
                            svpv = state;
                            break svpCheck;
                        }
                    }
                }
            }
        }
        var pc = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, svpv, PalettedContainer.Strategy.SECTION_STATES);
        if(isSingleValuePalette){
            return new LevelChunkSection(section.bottomBlockY() >> 4, pc, (PalettedContainer<Holder<Biome>>) section.getBiomes());
        }
        var biomes = (PalettedContainer<Holder<Biome>>)section.getBiomes();
        var newState = new LevelChunkSection(section.bottomBlockY() >> 4, pc, biomes.copy());
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    var state = view[i][j][k];
                    if (state != null) {
                        newState.states.getAndSetUnchecked(i, j, k, state);
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
