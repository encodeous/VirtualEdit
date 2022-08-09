package ca.encodeous.virtualedit.data;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.utils.DataUtils;

public class Virtual3DWorld {
    final IntervalTree2D[] worldLayers;
    private int cnt = 1;
    private final int xSize, ySize, zSize;
    public Virtual3DWorld(int xs, int ys, int zs){
        worldLayers = new IntervalTree2D[ys];
        xSize = xs;
        zSize = zs;
        ySize = ys;
    }
    IntervalTree2D getYLayer(int q){
        if(q < 0 || q >= ySize) return null;
        if(worldLayers[q] == null){
            worldLayers[q] = new IntervalTree2D(0, xSize, 0, zSize);
        }
        return worldLayers[q];
    }

    public int query(int x, int y, int z){
        if(x < 0 || x >= xSize) return Constants.DS_NULL_VALUE;
        if(z < 0 || z >= zSize) return Constants.DS_NULL_VALUE;
        var layer = getYLayer(y);
        if(layer == null) return Constants.DS_NULL_VALUE;
        return DataUtils.tGb(layer.query(x, z));
    }

    public void update(int x1, int x2, int y1, int y2, int z1, int z2, int val){
        synchronized (worldLayers){
            for(int i = y1; i <= y2; i++){
                getYLayer(i).update(val, x1, x2, z1, z2, cnt);
            }
            cnt++;
        }
    }
}
