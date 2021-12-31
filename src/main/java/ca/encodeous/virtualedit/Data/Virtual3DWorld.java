package ca.encodeous.virtualedit.Data;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.Utils.DataUtils;
import ca.encodeous.virtualedit.Utils.MaterialUtils;

public class Virtual3DWorld {
    final IntervalTree2D[] worldLayers;
    private int cnt = 1;
    public Virtual3DWorld(){
        worldLayers = new IntervalTree2D[400];
    }
    IntervalTree2D GetYLayer(int y){
        int q = y + 70;
        if(worldLayers[q] == null){
            worldLayers[q] = new IntervalTree2D(0, Constants.MAX_WORLD_SIZE * 2, 0, Constants.MAX_WORLD_SIZE * 2);
        }
        return worldLayers[q];
    }

    public int Query(int x, int y, int z){
        try{
            x += Constants.MAX_WORLD_SIZE / 2;
            z += Constants.MAX_WORLD_SIZE / 2;
            return DataUtils.TGb(GetYLayer(y).Query(x, z));
        }catch (Exception e){
            e.printStackTrace();
        }
        return Constants.DS_NULL_VALUE;
    }

    public void Update(int x1, int x2, int y1, int y2, int z1, int z2, int val){
        x1 += Constants.MAX_WORLD_SIZE / 2;
        x2 += Constants.MAX_WORLD_SIZE / 2;
        z1 += Constants.MAX_WORLD_SIZE / 2;
        z2 += Constants.MAX_WORLD_SIZE / 2;
        for(int i = y1; i <= y2; i++){
            GetYLayer(i).Update(val, x1, x2, z1, z2, cnt);
        }
        cnt++;
    }
}
