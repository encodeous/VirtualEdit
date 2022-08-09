package ca.encodeous.virtualedit.utils;

import ca.encodeous.virtualedit.Constants;
import org.bukkit.util.Vector;

public class DataUtils {
    public static long getIntTuple(int a, int b){

        return (((long)a) << 32) | (b & 0xFFFFFFFFL);
    }
    public static int tGa(long val){
        return (int) (val >> 32);
    }
    public static int tGb(long val){
        return (int) val;
    }
    public static int convertToSafeVal(int x){
        return ((x + Constants.MAX_WORLD_SIZE / 2) >> 4);
    }
    public static int convertToSafeValChunk(int x){
        return x + Constants.MAX_CHUNKS_AXIS / 2;
    }
}
