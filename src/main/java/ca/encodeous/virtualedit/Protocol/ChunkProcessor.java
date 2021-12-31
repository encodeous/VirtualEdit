package ca.encodeous.virtualedit.Protocol;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.Data.IntervalTree2D;
import ca.encodeous.virtualedit.Utils.DataUtils;
import ca.encodeous.virtualedit.World.VirtualChunk;
import ca.encodeous.virtualedit.World.VirtualWorldView;
import net.imprex.orebfuscator.NmsInstance;
import net.imprex.orebfuscator.chunk.Chunk;
import net.imprex.orebfuscator.chunk.ChunkSection;
import net.imprex.orebfuscator.chunk.ChunkStruct;
import net.imprex.orebfuscator.util.HeightAccessor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class ChunkProcessor {
    public void process(ChunkStruct chunkStruct, VirtualWorldView view) {
        view.CullChunks();
        World world = chunkStruct.world;
        HeightAccessor heightAccessor = HeightAccessor.get(world);

        int baseX = chunkStruct.chunkX << 4;
        int baseZ = chunkStruct.chunkZ << 4;

        try (VirtualChunk chunk = VirtualChunk.fromChunkStruct(chunkStruct)) {
            for (int sectionIndex = 0; sectionIndex < chunk.getSectionCount(); sectionIndex++) {
                ChunkSection chunkSection = chunk.createSection(sectionIndex);
                if (chunkSection == null) {
                    continue;
                }
                final int baseY = heightAccessor.getMinBuildHeight() + (sectionIndex << 4);
                for (int index = 0; index < 4096; index++) {
                    int y = baseY + (index >> 8 & 15);
                    int x = baseX + (index & 15);
                    int z = baseZ + (index >> 4 & 15);
                    Vector vec = new Vector(x, y, z);
                    int nid = view.ProcessWorldViewId(vec);
                    if (nid != Constants.DS_NULL_VALUE) {
                        chunkSection.setBlock(index, nid);
                    }
                }
            }
            chunkStruct.setDataBuffer(chunk.finalizeOutput());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }
}
