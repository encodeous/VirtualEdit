package ca.encodeous.virtualedit.World;

import ca.encodeous.virtualedit.Utils.Vector2;
import org.bukkit.Material;

public class VirtualWorldModification {
    public VirtualWorldModification(int x1, int x2, int y1, int y2, int z1, int z2, Material material) {
        this.x1 = Math.min(x1, x2);
        this.x2 = Math.max(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.y2 = Math.max(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.z2 = Math.max(z1, z2);
        this.mat = material;
    }

    public Vector2 GetXZ1(){
        return new Vector2(x1, z1);
    }
    public Vector2 GetXZ2(){
        return new Vector2(x2, z2);
    }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    public int getZ1() {
        return z1;
    }

    public int getZ2() {
        return z2;
    }

    public long getVolume(){
        return (long) (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1);
    }

    public Material getMat() {
        return mat;
    }

    int x1, x2, y1, y2, z1, z2;
    Material mat;
}
