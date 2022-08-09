package ca.encodeous.virtualedit.data;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.utils.DataUtils;

public class IntervalTree2D {
    private static long NEUTRAL_VALUE = DataUtils.GetIntTuple(-1, Constants.DS_NULL_VALUE);
    public IntervalTree1D data;
    public IntervalTree1D order;
    public int x1, x2, ym1, ym2;
    protected IntervalTree2D left = null, right = null;
    private boolean hasInitialized = false;
    public IntervalTree2D(int x1, int x2, int y1, int y2){
        this.x1 = x1;
        this.x2 = x2;
        this.ym1 = y1;
        this.ym2 = y2;
        data = new IntervalTree1D(ym1, ym2);
        order = new IntervalTree1D(ym1, ym2);
    }
    public void Extend(){
        if(!hasInitialized && x1 != x2){
            hasInitialized = true;
            int mid = (x1 + x2) / 2;
            left = new IntervalTree2D(x1, mid, ym1, ym2);
            right = new IntervalTree2D(mid+1, x2, ym1, ym2);
        }
    }
    public long Query(int x, int y){
        if(x < x1 || x > x2) return NEUTRAL_VALUE;
        long val = NEUTRAL_VALUE;
        if(x1 != x2 && hasInitialized){
            Extend();
            long mid = (x1 + x2) / 2;
            if(x <= mid){
                val = left.Query(x, y);
            }else{
                val = right.Query(x, y);
            }
        }
        int a = DataUtils.TGa(val), b = DataUtils.TGb(val);
        int ca = order.QueryY(y);
        if(ca > a){
            a = ca;
            b = data.QueryY(y);
        }
        return DataUtils.GetIntTuple(a, b);
    }
    public void Update(int newBlockData, int lbx, int ubx, int lby, int uby, int updateId){
        if(x1 > ubx || x2 < lbx) return;
        if(lbx <= x1 && x2 <= ubx){
            order.Update(updateId, lby, uby);
            data.Update(newBlockData, lby, uby);
        }
        else{
            Extend();
            left.Update(newBlockData, lbx, ubx, lby, uby, updateId);
            right.Update(newBlockData, lbx, ubx, lby, uby, updateId);
        }
    }
}
