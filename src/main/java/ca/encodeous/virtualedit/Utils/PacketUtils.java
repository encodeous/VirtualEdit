package ca.encodeous.virtualedit.Utils;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class PacketUtils {
    public static void setChunkModified(Vector location, PacketContainer pc) { //Takes a *real world coordinate*.
        //Converts to chunk coordinate
        int x = location.getBlockX() >> 4;
        int y = location.getBlockY() >> 4;
        int z = location.getBlockZ() >> 4;
        //Creates a new BlockPosition at chunk coordinate
        BlockPosition value = new BlockPosition(x, y, z);
        //Writes the chunk position
        pc.getSectionPositions().write(0, value); //blChPack is our PacketContainer from before
    }

    public static Vector getChunkModified(PacketContainer pc) { //Takes a *real world coordinate*.
        BlockPosition value = pc.getSectionPositions().read(0);
        return new Vector(value.getX() << 4, value.getY() << 4, value.getZ() << 4);
    }

    public static short setShortLocation(Location loc) { //Takes a real-world location.
        return (setShortLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    public static short setShortLocation(int x, int y, int z) {
        //Convert to location within chunk.
        x = x & 0xF;
        y = y & 0xF;
        z = z & 0xF;
        //Creates position from location within chunk
        return (short) (x << 8 | z << 4 | y << 0);
    }

    public static Vector getShortLocation(short v) {
        return new Vector(v >> 8, (v >> 4) & 0xF, v & 0xF);
    }

    public static void setChangeData(WrappedBlockData[] blockDat, short[] blockPositions, PacketContainer pc) {
        pc.getBlockDataArrays().writeSafely(0, blockDat);
        pc.getShortArrays().writeSafely(0, blockPositions);
    }
}
