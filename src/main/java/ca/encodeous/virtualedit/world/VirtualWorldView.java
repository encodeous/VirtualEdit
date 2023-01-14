package ca.encodeous.virtualedit.world;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.data.IntervalTree2D;
import ca.encodeous.virtualedit.VirtualWorld;
import ca.encodeous.virtualedit.data.Pair;
import ca.encodeous.virtualedit.utils.DataUtils;
import io.papermc.paper.chunk.PlayerChunkLoader;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.util.MCUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ca.encodeous.virtualedit.utils.DataUtils.convertToSafeVal;
import static ca.encodeous.virtualedit.utils.DataUtils.convertToSafeValChunk;

public class VirtualWorldView {
    public final VirtualWorldLayer[] layers;
    private int curLayer;
    private final IntervalTree2D updates;
    public final ConcurrentHashMap<ChunkPos, Object> queuedChunkUpdates;
    public final VirtualWorldChangeNotifier notifier;
    public final World world;
    private final Player player;
    private int updateId = 0;

    public VirtualWorldView(Player p, World world, int maxLayers) {
        layers = new VirtualWorldLayer[maxLayers];
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
    public VirtualWorldView(Player p, World world) {
        this(p, world, 128);
    }

    public void markWorldForChange() {
        markFor(0, Constants.MAX_WORLD_SIZE, 0, Constants.MAX_WORLD_SIZE, Constants.UPDATE);
    }

    public void markFor(int x1, int x2, int z1, int z2, int value) {
        synchronized (updates) {
            updates.update(value, convertToSafeVal(x1), convertToSafeVal(x2), convertToSafeVal(z1), convertToSafeVal(z2), updateId++);
        }
    }

    public void markForChunk(int x1, int x2, int z1, int z2, int value) {
        synchronized (updates) {
            updates.update(value, convertToSafeValChunk(x1), convertToSafeValChunk(x2), convertToSafeValChunk(z1), convertToSafeValChunk(z2), updateId++);
        }
    }

    public int getChunkMark(int x, int y) {
        synchronized (updates) {
            return DataUtils.tGb(updates.query(x + Constants.MAX_CHUNKS_AXIS / 2, y + Constants.MAX_CHUNKS_AXIS / 2));
        }
    }

    public void pushLayer(VirtualWorldLayer layer) {
        layer.subscribe(notifier);
        layers[curLayer++] = layer;
        markWorldForChange();
    }

    public VirtualWorldLayer peekLayer() {
        return curLayer == 0 ? null : layers[curLayer - 1];
    }

    public VirtualWorldLayer popLayer() {
        var layer = layers[--curLayer];
        layer.unsubscribe(notifier);
        markWorldForChange();
        return layer;
    }

    public void close() {
        for(int i = 0; i < curLayer; i++){
            var layer = layers[i];
            if(layer == null) continue;
            layer.unsubscribe(notifier);
        }
    }

    public void refreshWorldView() {
        VirtualWorld.PlayerUpdateQueue.offerAndLock(new Pair<>(player, null));
    }
    public void refreshWorldView(Runnable whenFinished) {
        VirtualWorld.PlayerUpdateQueue.offerAndLock(new Pair<>(player, whenFinished));
    }

    public BlockState renderAt(Vector loc) {
        for(int i = 0; i < curLayer; i++){
            var layer = layers[i];
            BlockState block = layer.getBlock(loc.getBlockX() - layer.xOffset, loc.getBlockY() - layer.yOffset, loc.getBlockZ() - layer.zOffset);
            if (block != null) {
                return block;
            }
        }
        return null;
    }

    public BlockState renderAt(int x, int y, int z, ResourceCache cache) {
        for(int i = 0; i < curLayer; i++){
            var layer = layers[i];
            var block = layer.getBlock(x - layer.xOffset, y - layer.yOffset, z - layer.zOffset, cache);
            if (block != null) {
                return block;
            }
        }
        return null;
    }

    public boolean isVirtual(int x, int y, int z) {
        for(int i = 0; i < curLayer; i++){
            var layer = layers[i];
            var block = layer.getNode(x - layer.xOffset, y - layer.yOffset, z - layer.zOffset);
            if (block != Constants.DS_NULL_VALUE) {
                return true;
            }
        }
        return false;
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

    public void sendChunksInRange() {
        var sp = ((CraftPlayer) player).getHandle();
        ChunkMap chunkMap = (((CraftWorld) world).getHandle().getLevel().getChunkSource()).chunkMap;
        PlayerChunkLoader.PlayerLoaderData data = chunkMap.playerChunkManager.getData(sp);
        boolean useLookPriority = GlobalConfiguration.get().chunkLoading.enableFrustumPriority && (sp.getDeltaMovement().horizontalDistanceSqr() > 0.25 || sp.getAbilities().flying);
        int sendViewDistance = data.getTargetSendViewDistance();
        int distance = Math.min(data.getTargetTickViewDistance(), sendViewDistance);
        int centerChunkX = Mth.floor(sp.getX()) >> 4;
        int centerChunkZ = Mth.floor(sp.getZ()) >> 4;
        double p1x = sp.getX();
        double p1z = sp.getZ();
        float yaw = MCUtil.normalizeYaw(sp.getYRot() + 90.0F);
        double p2x = 192.0 * Math.cos(Math.toRadians((double) yaw + 55.0)) + p1x;
        double p2z = 192.0 * Math.sin(Math.toRadians((double) yaw + 55.0)) + p1z;
        double p3x = 192.0 * Math.cos(Math.toRadians((double) yaw - 55.0)) + p1x;
        double p3z = 192.0 * Math.sin(Math.toRadians((double) yaw - 55.0)) + p1z;
        ArrayList<Pair<Double, ChunkPos>> sendChunks = new ArrayList<>();
        for (int dx = -distance; dx <= distance; ++dx) {
            for (int dz = -distance; dz <= distance; ++dz) {
                int chunkX = dx + centerChunkX;
                int chunkZ = dz + centerChunkZ;
                int squareDistance = Math.max(Math.abs(dx), Math.abs(dz));
                boolean sendChunk = squareDistance <= sendViewDistance && getChunkMark(chunkX, chunkZ) == Constants.UPDATE;
                if (sendChunk) {
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
        for (var chunk : sendChunks) {
            queuedChunkUpdates.put(chunk.getB(), new Object());
            sendUpdatedChunk(chunk.getB().x, chunk.getB().z);
        }
    }

    public void sendUpdatedChunk(int x, int z) {
        var sp = ((CraftPlayer) player).getHandle();
        if (!player.isOnline()) {
            return;
        }
        sp.connection.send(renderPacketForChunk(x, z));
    }

    public ClientboundLevelChunkWithLightPacket renderPacketForChunk(int x, int z) {
        var cWorld = (CraftWorld) world;
        var sWorld = cWorld.getHandle();
        var processed = renderChunk(x, z);
//        var vLightEngine = new VirtualLightEngine(processed.getB(), sWorld, (ThreadedLevelLightEngine) sWorld.getLightEngine(), processed.getA());
        var packet = new ClientboundLevelChunkWithLightPacket(processed.getA(), sWorld.getLightEngine(), null, null, true, false);
        var light = packet.getLightData();
        // reprocess lighting
        var hdMask = light.getSkyYMask();
        var ieMask = light.getEmptySkyYMask();
        var su = light.getSkyUpdates();
        var blList = new ArrayList<byte[]>();
        int idx = 0;
        var chunkMask = processed.getB();
        var chunk = renderMask.get();
        var totalSections = processed.getA().getSectionsCount();
        for (int sec = 0; sec <= totalSections; sec++) {
            boolean isInWorld = sec != 0 && sec != totalSections;
            boolean hasData = hdMask.get(sec);
            if(isInWorld && chunkMask.get(sec - 1)){
                // section is modified
                byte[] cb;
                if(!hasData){
                    hdMask.set(sec, true);
                    cb = new byte[2048];
                }else{
                    cb = su.get(idx++);
                }
                ieMask.set(sec, false);
                blList.add(cb);
                for(int i = 0; i < 16; i++){
                    for(int j = 0; j < 16; j++){
                        for(int k = 0; k < 16; k++){
                            if(chunk[sec - 1][i][j][k]){
                                set(i, j, k, 15, cb);
                            }
                        }
                    }
                }
            }else{
                if(hasData){
                    blList.add(su.get(idx++));
                }
            }
        }
        su.clear();
        su.addAll(blList);
        return packet;
    }

    void set(int x, int y, int z, int value, byte[] arr) {
        set(x & 0xF | (z & 0xF) << 4 | (y & 0xF) << 8, value, arr);
    }


    public void set(int index, int value, byte[] arr) {
        int shift = (index & 0x1) << 2;
        int i = index >>> 1;

        arr[i] = (byte) (arr[i] & 240 >>> shift | value << shift);
    }

    private ThreadLocal<boolean[][][][]> renderMask = ThreadLocal.withInitial(() -> new boolean[24][16][16][16]);
    private ThreadLocal<BlockState[][][][]> sectionData = ThreadLocal.withInitial(() -> new BlockState[24][16][16][16]);

    Pair<LevelChunk, BitSet> renderChunk(int x, int z) {
        var level = ((CraftWorld) world).getHandle();
        var chunk = level.getChunk(x, z);
        var rendered = renderSections(chunk, level);
        var lChunk = new LevelChunk(chunk.level, chunk.getPos(), chunk.getUpgradeData(), new LevelChunkTicks<>(), new LevelChunkTicks<>(),
                chunk.getInhabitedTime(), rendered.getA(), levelChunk -> {
        }, chunk.getBlendingData());
        for(var v : chunk.getBlockEntities().values()){
            if(lChunk.getBlockState(v.getBlockPos()).is(chunk.getBlockState(v.getBlockPos()).getBlock())){
                var meta = v.saveWithFullMetadata();
                lChunk.setBlockEntity(BlockEntity.loadStatic(v.getBlockPos(),
                        lChunk.getBlockState(v.getBlockPos()), meta));
            }
        }
        return new Pair<>(lChunk, rendered.getB());
    }

    private Pair<LevelChunkSection[], BitSet> renderSections(LevelChunk chunk, Level world) {
        var sec = chunk.getSections();
        LevelChunkSection[] arr = new LevelChunkSection[world.getSectionsCount()];
        BlockState[][][][] sections = sectionData.get();
        boolean[][][][] relightBlocks = renderMask.get();
        assert world.getSectionsCount() == sec.length;
        var cache = new ResourceCache();
        var transformedSections = new BitSet();
        for (int section = 0; section < sec.length; section++) {
            readSection(chunk.getPos(), section, world.getMinBuildHeight(), cache, sections[section]);
            boolean isSectionTransformed = false;
            if (isTransformRequired(sections[section])) {
                var x = sec[section];
                arr[section] = applySectionTransformations(x, sections[section]);
                isSectionTransformed = true;
            } else {
                arr[section] = sec[section];
            }
            boolean hasAppliedLightUpdates = false;
            for(int i = 0; i < 16; i++){
                for(int j = 0; j < 16; j++){
                    for(int k = 0; k < 16; k++){
                        relightBlocks[section][i][j][k] =
                                        isVirtualBlock(chunk.getPos(), world.getMinBuildHeight(), section, i, j, k, sections[section], isSectionTransformed) ||
                                        isVirtualBlock(chunk.getPos(), world.getMinBuildHeight(), section, i+1, j, k, sections[section], isSectionTransformed) ||
                                        isVirtualBlock(chunk.getPos(), world.getMinBuildHeight(), section, i, j+1, k, sections[section], isSectionTransformed) ||
                                        isVirtualBlock(chunk.getPos(), world.getMinBuildHeight(), section, i, j, k+1, sections[section], isSectionTransformed) ||
                                        isVirtualBlock(chunk.getPos(), world.getMinBuildHeight(), section, i-1, j, k, sections[section], isSectionTransformed) ||
                                        isVirtualBlock(chunk.getPos(), world.getMinBuildHeight(), section, i, j-1, k, sections[section], isSectionTransformed) ||
                                        isVirtualBlock(chunk.getPos(), world.getMinBuildHeight(), section, i, j, k-1, sections[section], isSectionTransformed)
                        ;
                        hasAppliedLightUpdates |= relightBlocks[section][i][j][k];
                    }
                }
            }
            if(hasAppliedLightUpdates){
                // mark this section to be sent
                transformedSections.set(section, true);
            }
        }
        return new Pair<>(arr, transformedSections);
    }
    private static final int mask = ~0b1111;
    private boolean isVirtualBlock(ChunkPos pos, int minH, int sec, int x, int y, int z, BlockState[][][] section, boolean transformed){
        // check if queried block is within the cached section
        if(((x & mask) | (y & mask) | (z & mask)) != 0){
            return isVirtual(x + (pos.x << 4), minH + (sec << 4) + y, z + (pos.z << 4));
        }
        return transformed && section[x][y][z] != null;
    }
//    private boolean isVirtualBlock(ChunkPos pos, int minH, int sec, int x, int y, int z, BlockState[][][] section, boolean transformed){
//        if(x < 0 || y < 0 || z < 0 || x > 15 || y > 15 || z > 15){
//            return isVirtual(x + (pos.x << 4), minH + (sec << 4) + y, z + (pos.z << 4));
//        }
//        return transformed && section[x][y][z] != null;
//    }

    void readSection(ChunkPos pos, int secId, int mh, ResourceCache cache, BlockState[][][] cached) {
        var chunkX = pos.x;
        var chunkZ = pos.z;
        int mx = chunkX << 4, my = mh + (secId << 4), mz = chunkZ << 4;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    cached[i][j][k] = renderAt(i + mx, j + my, k + mz, cache);
                }
            }
        }
    }

    boolean isTransformRequired(BlockState[][][] view) {
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

    LevelChunkSection applySectionTransformations(LevelChunkSection section, BlockState[][][] view) {
        boolean isSingleValuePalette = true;
        BlockState svpv = null;
        svpCheck:
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    var state = view[i][j][k];
                    if (i == j && j == k && k == 0) {
                        svpv = state;
                    } else if (!Objects.equals(svpv, state)) {
                        isSingleValuePalette = false;
                        if (state != null) {
                            svpv = state;
                            break svpCheck;
                        }
                    }
                }
            }
        }
        var pc = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, svpv, PalettedContainer.Strategy.SECTION_STATES);
        if (isSingleValuePalette) {
            var svp = new LevelChunkSection(section.bottomBlockY() >> 4, pc, (PalettedContainer<Holder<Biome>>) section.getBiomes());
            svp.recalcBlockCounts();
            return svp;
        }
        var biomes = (PalettedContainer<Holder<Biome>>) section.getBiomes();
        var newState = new LevelChunkSection(section.bottomBlockY() >> 4, pc, biomes.copy());
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    var state = view[i][j][k];
                    if (state != null) {
                        newState.states.getAndSetUnchecked(i, j, k, state);
                    } else {
                        newState.states.getAndSetUnchecked(i, j, k, section.states.get(i, j, k));
                    }
                }
            }
        }
        newState.recalcBlockCounts();
        return newState;
    }
}
