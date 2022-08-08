package ca.encodeous.virtualedit.Utils;

import org.bukkit.util.Vector;

public class DataUtils {
    public static long GetIntTuple(int a, int b){

        return (((long)a) << 32) | (b & 0xFFFFFFFFL);
    }
    public static int TGa(long val){
        return (int) (val >> 32);
    }
    public static int TGb(long val){
        return (int) val;
    }

    public static boolean CheckOverlap(Vector2 l1, Vector2 r1, Vector2 l2, Vector2 r2){
        return IsOverlapping(l1, r1, l2, r2) || IsOverlapping(l2, r2, l1, r1);
    }

    private static boolean IsOverlapping(Vector2 l1, Vector2 r1, Vector2 l2, Vector2 r2) {
        if(l1.x > r2.x || l1.y > r2.y || r1.x < l2.x || r1.y < l2.y){
            return false;
        }
        return true;
    }

    public static long GetSChunkHash(int x, short y, int z){
        return ((long) x << 22) | ((long) z << 2) | y;
    }

    public static Vector GetSChunkFromHash(long hash){
        int x = (int) (hash >> 22);
        int z = (int) (hash & (0b1111111111111111111100));
        int y = (int) (hash & (0b11));
        return new Vector(x, y, z);
    }
}
