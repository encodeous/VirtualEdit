package ca.encodeous.virtualedit.world;

public abstract class VirtualWorldChangeNotifier {
    public abstract void updateViewport(VirtualWorldLayer layer);
    public abstract void markForChange(int x1, int x2, int z1, int z2);
}
