package ca.encodeous.virtualedit.world;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.data.IntervalTree2D;
import ca.encodeous.virtualedit.VirtualWorld;
import ca.encodeous.virtualedit.utils.DataUtils;
import io.papermc.paper.chunk.PlayerChunkLoader;
import io.papermc.paper.configuration.GlobalConfiguration;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MCUtil;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ca.encodeous.virtualedit.utils.DataUtils.convertToSafeVal;
import static ca.encodeous.virtualedit.utils.DataUtils.convertToSafeValChunk;

public class VirtualWorldView {
    private ArrayDeque<VirtualWorldLayer> layers;
    private final IntervalTree2D updates;
    public final ConcurrentHashMap<ChunkPos, Object> queuedChunkUpdates;
    private VirtualWorldChangeNotifier notifier;
    private ThreadedLevelLightEngine lightEngine;
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
                markFor(x1, x2, z1, z2, Constants.UPDATE);
            }
        };
        queuedChunkUpdates = new ConcurrentHashMap<>();
    }
    public void markWorldForChange(){
        markFor(0, Constants.MAX_WORLD_SIZE, 0, Constants.MAX_WORLD_SIZE, Constants.UPDATE);
    }
    public void markFor(int x1, int x2, int z1, int z2, int value) {
        synchronized (updates){
            updates.Update(value, convertToSafeVal(x1), convertToSafeVal(x2), convertToSafeVal(z1), convertToSafeVal(z2), updateId++);
        }
    }
    public void markForChunk(int x1, int x2, int z1, int z2, int value) {
        synchronized (updates){
            updates.Update(value, convertToSafeValChunk(x1), convertToSafeValChunk(x2), convertToSafeValChunk(z1), convertToSafeValChunk(z2), updateId++);
        }
    }
    public int getChunkMark(int x, int y){
        synchronized (updates){
            return DataUtils.TGb(updates.Query(x + Constants.MAX_CHUNKS_AXIS / 2, y + Constants.MAX_CHUNKS_AXIS / 2));
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

    protected static boolean triangleIntersects(double p1x, double p1z, double p2x, double p2z, double p3x, double p3z, double targetX, double targetZ) {
        double d = (p2z - p3z) * (p1x - p3x) + (p3x - p2x) * (p1z - p3z);
        double a = ((p2z - p3z) * (targetX - p3x) + (p3x - p2x) * (targetZ - p3z)) / d;
        if (!(a < 0.0) && !(a > 1.0)) {
            double b = ((p3z - p1z) * (targetX - p3x) + (p1x - p3x) * (targetZ - p3z)) / d;
            if (!(b < 0.0) && !(b > 1.0)) {
                double c = 1.0 - a - b;
                return c >= 0.0 && c <= 1.0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void sendChunksInRange(){
        var sp = ((CraftPlayer) player).getHandle();
        ChunkMap chunkMap = (((CraftWorld)world).getHandle().getLevel().getChunkSource()).chunkMap;
        PlayerChunkLoader.PlayerLoaderData data = chunkMap.playerChunkManager.getData(sp);
        boolean useLookPriority = GlobalConfiguration.get().chunkLoading.enableFrustumPriority && (sp.getDeltaMovement().horizontalDistanceSqr() > 0.25 || sp.getAbilities().flying);
        int sendViewDistance = data.getTargetSendViewDistance();
        int distance = Math.min(data.getTargetTickViewDistance(), sendViewDistance);
        int centerChunkX = Mth.floor(sp.getX()) >> 4;
        int centerChunkZ = Mth.floor(sp.getZ()) >> 4;
        double p1x = sp.getX();
        double p1z = sp.getZ();
        float yaw = MCUtil.normalizeYaw(sp.yRot + 90.0F);
        double p2x = 192.0 * Math.cos(Math.toRadians((double)yaw + 55.0)) + p1x;
        double p2z = 192.0 * Math.sin(Math.toRadians((double)yaw + 55.0)) + p1z;
        double p3x = 192.0 * Math.cos(Math.toRadians((double)yaw - 55.0)) + p1x;
        double p3z = 192.0 * Math.sin(Math.toRadians((double)yaw - 55.0)) + p1z;
        ArrayList<Pair<Double, ChunkPos>> sendChunks = new ArrayList<>();
        for(int dx = -distance; dx <= distance; ++dx) {
            for(int dz = -distance; dz <= distance; ++dz) {
                int chunkX = dx + centerChunkX;
                int chunkZ = dz + centerChunkZ;
                int squareDistance = Math.max(Math.abs(dx), Math.abs(dz));
                boolean sendChunk = squareDistance <= sendViewDistance && getChunkMark(chunkX, chunkZ) == Constants.UPDATE;
                if(sendChunk){
                    boolean prioritised = useLookPriority && triangleIntersects(p1x, p1z, p2x, p2z, p3x, p3z, chunkX << 4 | 8, chunkZ << 4 | 8);
                    int manhattanDistance = Math.abs(dx) + Math.abs(dz);
                    double priority;
                    if (squareDistance <= GlobalConfiguration.get().chunkLoading.minLoadRadius) {
                        priority = (-(2 * GlobalConfiguration.get().chunkLoading.minLoadRadius + 1 - manhattanDistance));
                    } else if (prioritised) {
                        priority = manhattanDistance / 6.0;
                    } else {
                        priority = manhattanDistance;
                    }
                    sendChunks.add(new Pair<>(priority, new ChunkPos(chunkX, chunkZ)));
                }
            }
        }
        markForChunk(centerChunkX - distance, centerChunkX + distance, centerChunkZ - distance, centerChunkZ + distance, Constants.NO_UPDATE);
        sendChunks.sort(Comparator.comparingDouble(Pair::getA));
        for(var chunk : sendChunks){
            queuedChunkUpdates.put(chunk.getB(), new Object());
            sendUpdatedChunk(chunk.getB().x, chunk.getB().z);
        }
    }
    public void sendUpdatedChunk(int x, int z){
        var sp = ((CraftPlayer) player).getHandle();
        if (!player.isOnline()) {
            return;
        }
        sp.connection.send(makePacketForChunk(x, z));
    }

    public ClientboundLevelChunkWithLightPacket makePacketForChunk(int x, int z){
        var cWorld = (CraftWorld)world;
        var sWorld = cWorld.getHandle();
        var processed = processChunk(x, z);
        return new ClientboundLevelChunkWithLightPacket(processed, sWorld.getLightEngine(), null, null, true, false);
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
