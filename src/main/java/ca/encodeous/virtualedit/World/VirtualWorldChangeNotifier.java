package ca.encodeous.virtualedit.World;

import ca.encodeous.virtualedit.Utils.DataUtils;
import ca.encodeous.virtualedit.Utils.Vector2;

public abstract class VirtualWorldChangeNotifier {
    public abstract Vector2 getViewCenter();
    public abstract int getViewDistance();
    public boolean CheckOverlap(Vector2 v1, Vector2 v2){
        Vector2 vc = getViewCenter();
        int d = getViewDistance();
        Vector2 v3 = new Vector2(vc.x - d, vc.y - d);
        Vector2 v4 = new Vector2(vc.x + d, vc.y + d);
        return DataUtils.CheckOverlap(v1, v2, v3, v4);
    }
    public abstract void updateViewport(VirtualWorldLayer layer);
    public abstract void markForChange(int x1, int x2, int z1, int z2);
}
