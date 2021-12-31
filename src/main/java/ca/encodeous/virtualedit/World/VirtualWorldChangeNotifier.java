package ca.encodeous.virtualedit.World;

import ca.encodeous.virtualedit.Utils.DataUtils;
import ca.encodeous.virtualedit.Utils.Vector2;

public abstract class VirtualWorldChangeNotifier {
    public abstract Vector2 GetViewCenter();
    public abstract int GetViewDistance();
    public boolean CheckOverlap(Vector2 v1, Vector2 v2){
        Vector2 vc = GetViewCenter();
        int d = GetViewDistance();
        Vector2 v3 = new Vector2(vc.x - d, vc.y - d);
        Vector2 v4 = new Vector2(vc.x + d, vc.y + d);
        return DataUtils.CheckOverlap(v1, v2, v3, v4);
    }
    public abstract void UpdateViewport(VirtualWorldLayer layer);
}
