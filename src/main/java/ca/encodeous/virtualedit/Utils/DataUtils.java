package ca.encodeous.virtualedit.Utils;

public class DataUtils {
    public static long GetIntTuple(int a, int b){
        return (((long)a) << 32) | (b & 0xffffffffL);
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
}
