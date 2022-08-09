package ca.encodeous.virtualedit.data;

import ca.encodeous.virtualedit.Constants;
import ca.encodeous.virtualedit.utils.DataUtils;

public class IntervalTree2D {
    private static long NEUTRAL_VALUE = DataUtils.getIntTuple(-1, Constants.DS_NULL_VALUE);
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
    public void extend(){
        if(!hasInitialized && x1 != x2){
            hasInitialized = true;
            int mid = (x1 + x2) / 2;
            left = new IntervalTree2D(x1, mid, ym1, ym2);
            right = new IntervalTree2D(mid+1, x2, ym1, ym2);
        }
    }
    public long query(int x, int y){
        if(x < x1 || x > x2) return NEUTRAL_VALUE;
        long val = NEUTRAL_VALUE;
        if(x1 != x2 && hasInitialized){
            extend();
            long mid = (x1 + x2) / 2;
            if(x <= mid){
                val = left.query(x, y);
            }else{
                val = right.query(x, y);
            }
        }
        int a = DataUtils.tGa(val), b = DataUtils.tGb(val);
        int ca = order.queryY(y);
        if(ca > a){
            a = ca;
            b = data.queryY(y);
        }
        return DataUtils.getIntTuple(a, b);
    }
    public void update(int newBlockData, int lbx, int ubx, int lby, int uby, int updateId){
        if(x1 > ubx || x2 < lbx) return;
        if(lbx <= x1 && x2 <= ubx){
            order.update(updateId, lby, uby);
            data.update(newBlockData, lby, uby);
        }
        else{
            extend();
            left.update(newBlockData, lbx, ubx, lby, uby, updateId);
            right.update(newBlockData, lbx, ubx, lby, uby, updateId);
        }
    }
}
