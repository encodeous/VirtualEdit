package ca.encodeous.virtualedit.data;

import ca.encodeous.virtualedit.Constants;

public class IntervalTree1D {
    public int nodeBlockData = Constants.DS_NULL_VALUE;
    public int min, max;
    protected IntervalTree1D left = null, right = null;
    private boolean hasInitialized = false;
    public IntervalTree1D(int l, int r){
        min = l;
        max = r;
    }
    public void propagate(){
        extend();
        if(nodeBlockData != Constants.DS_NULL_VALUE){
            left.nodeBlockData = nodeBlockData;
            right.nodeBlockData = nodeBlockData;
            nodeBlockData = Constants.DS_NULL_VALUE;
        }
    }
    public void extend(){
        if(min == max) return;
        int mid = (min + max) / 2;
        hasInitialized = true;
        if(left == null){
            left = new IntervalTree1D(min, mid);
        }
        if(right == null){
            right = new IntervalTree1D(mid+1, max);
        }
    }
    public int queryY(int y){
        if(min <= y && y <= max){
            if(nodeBlockData != Constants.DS_NULL_VALUE){
                // lazy / fast return
                return nodeBlockData;
            }
            if(min == max){
                return nodeBlockData;
            }
            if(!hasInitialized) return Constants.DS_NULL_VALUE;
            propagate();
            int mid = (min + max) / 2;
            if(y <= mid){
                return left.queryY(y);
            }
            else return right.queryY(y);
        }
        return Constants.DS_NULL_VALUE;
    }
    public void update(int newBlockData, long l, long r){
        if(min > r || max < l) return;
        if(l <= min && max <= r){
            nodeBlockData = newBlockData;
        }
        else{
            propagate();
            left.update(newBlockData, l, r);
            right.update(newBlockData, l, r);
        }
    }
}
