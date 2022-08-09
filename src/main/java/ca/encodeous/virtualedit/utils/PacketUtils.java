package ca.encodeous.virtualedit.utils;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class PacketUtils {

    public static Vector getChunkModified(PacketContainer pc) {
        var value = pc.getSectionPositions().read(0);
        return new Vector(value.getX() << 4, value.getY() << 4, value.getZ() << 4);
    }

    public static Vector getShortLocation(short v) {
        return new Vector(v >> 8, v & 0xF, (v >> 4) & 0xF);
    }

    public static void setChangeData(WrappedBlockData[] blockDat, PacketContainer pc) {
        pc.getBlockDataArrays().writeSafely(0, blockDat);
    }
}
